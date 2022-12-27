package com.reasure.zomsurvival.util;

import com.mojang.logging.LogUtils;
import com.reasure.zomsurvival.entity.goal.target.NearestAttackableTargetWithRangeGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Event;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class SpawnUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int monsterDespawnDistanceSqr = MobCategory.MONSTER.getDespawnDistance() * MobCategory.MONSTER.getDespawnDistance();

    public static void spawnMonster(ServerLevel level, ChunkAccess chunk, int day) {
        int minY = level.getMinBuildHeight();
        BlockPos pos = getRandomPosWithin(level, chunk, minY);
        if (pos.getY() > minY) {
            spawnMonster(level, chunk, pos, day);
        }
    }

    public static void spawnMonster(ServerLevel level, ChunkAccess chunk, BlockPos pos, int day) {
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        BlockState block = chunk.getBlockState(pos);
        if (block.isRedstoneConductor(chunk, pos)) return;

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        Player player = level.getNearestPlayer((double) posX + 0.5D, posY, (double) posZ + 0.5D, -1, false);
        if (player == null) return;

        int spawnCount = MathUtil.getSpawnCount(day, level.random);
        for (int i = 0; i < spawnCount; i++) {
            SpawnGroupData spawnGroupData = null;

            if (level.random.nextInt(10) < 3) {
                posX = pos.getX();
                posZ = pos.getZ();
            }

            posX += MathUtil.nextCoord(level.random);
            posZ += MathUtil.nextCoord(level.random);
            mPos.set(posX, posY, posZ);
            double centerX = (double) posX + 0.5d;
            double centerZ = (double) posZ + 0.5d;

            double distanceToPlayer = player.distanceToSqr(centerX, posY, centerZ);
            if (!isRightDistanceToPlayerAndSpawnPoint(level, chunk, mPos, distanceToPlayer)) continue;

            EntityType<? extends Monster> type = MathUtil.nextMonster(level.random);
            if (!isValidSpawnPositionForMonster(level, mPos, distanceToPlayer, type)) continue;

            Monster monster = getMonsterForSpawn(level, type);
            if (monster == null) continue;
            reinforceMonster(level, monster, day);
            monster.addEffect(new MobEffectInstance(MobEffects.GLOWING, 99999));

            monster.moveTo(centerX, posY, centerZ, level.random.nextFloat() * 360.0f, 0.0f);
            Event.Result res = ForgeEventFactory.canEntitySpawn(monster, level, centerX, posY, centerZ, null, MobSpawnType.NATURAL);
            if (res == Event.Result.DENY) continue;
            if (res == Event.Result.ALLOW || (monster.checkSpawnRules(level, MobSpawnType.NATURAL) && monster.checkSpawnObstruction(level))) {
                if (!ForgeEventFactory.doSpecialSpawn(monster, level, (float) centerX, (float) posY, (float) centerZ, null, MobSpawnType.NATURAL)) {
                    spawnGroupData = monster.finalizeSpawn(level, level.getCurrentDifficultyAt(monster.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, null);
                }
                level.addFreshEntityWithPassengers(monster);
            }
        }
    }

    private static void reinforceMonster(ServerLevel level, Monster monster, int day) {
        if (monster instanceof Zombie zombie) {
            if (day >= SpawnConfig.ZOMBIE_ADD_FOLLOWING_RANGE_DAY.get()) {
                zombie.targetSelector.removeAllGoals(goal ->
                        goal instanceof NearestAttackableTargetGoal<?> targetGoal
                                && targetGoal.targetType == Player.class);
                zombie.targetSelector.addGoal(2,
                        new NearestAttackableTargetWithRangeGoal<>(zombie, Player.class, SpawnConfig.ZOMBIE_FOLLOWING_RANGE_MODIFIER.get()));
            }
        }
    }

    private static BlockPos getRandomPosWithin(ServerLevel level, ChunkAccess chunk, int minY) {
        ChunkPos chunkpos = chunk.getPos();
        int posX = chunkpos.getMinBlockX() + level.random.nextInt(16);
        int posZ = chunkpos.getMinBlockZ() + level.random.nextInt(16);
        int maxY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, posX, posZ) + 1;
        int posY = Mth.randomBetweenInclusive(level.random, minY, maxY);
        return new BlockPos(posX, posY, posZ);
    }

    private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel level, ChunkAccess chunk, BlockPos.MutableBlockPos pos, double distanceSqr) {
        if (distanceSqr <= 576.0D) return false;
        if (level.getSharedSpawnPos().closerToCenterThan(new Vec3((double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D), 24.0D))
            return false;
        return chunk.getPos().equals(new ChunkPos(pos)) || level.isNaturalSpawningAllowed(pos);
    }

    private static boolean isValidSpawnPositionForMonster(ServerLevel level, BlockPos.MutableBlockPos pos, double distanceSqr, EntityType<? extends Monster> type) {
        if (distanceSqr > monsterDespawnDistanceSqr)
            return false;

        SpawnPlacements.Type placementType = SpawnPlacements.getPlacementType(type);
        if (!NaturalSpawner.isSpawnPositionOk(placementType, level, pos, type)) return false;
        if (!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, level.random)) return false;
        return level.noCollision(type.getAABB((double) pos.getX() + 0.5d, pos.getY(), (double) pos.getZ() + 0.5d));
    }

    @Nullable
    private static Monster getMonsterForSpawn(ServerLevel level, EntityType<? extends Monster> type) {
        try {
            Entity entity = type.create(level);
            if (entity instanceof Monster monster) {
                return monster;
            }
            LOGGER.warn("Can't spawn {}", type);
        } catch (Exception exception) {
            LOGGER.warn("Failed to create " + type, exception);
        }
        return null;
    }
}
