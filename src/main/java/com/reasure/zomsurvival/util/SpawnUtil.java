package com.reasure.zomsurvival.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Event;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class SpawnUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void spawnMonster(ServerLevel level, ChunkAccess chunk, int day) {
        int minY = level.getMinBuildHeight();
        BlockPos pos = getRandomPosWithin(level, chunk, minY);
        if (pos.getY() > minY) {
            spawnMonster(level, chunk, pos, day);
        }
    }

    public static void spawnMonster(ServerLevel level, ChunkAccess chunk, BlockPos pos, int day) {
        int posY = pos.getY();
        BlockState block = chunk.getBlockState(pos);
        if (block.isRedstoneConductor(chunk, pos)) return;

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        int spawnPackSize = 0;

        for (int i = 0; i < 3; i++) {
            int posX = pos.getX();
            int posZ = pos.getZ();
            SpawnGroupData spawnGroupData = null;
            int spawnCount = MathUtil.getSpawnCount(day, level.random);

            for (int j = 0; j < spawnCount; j++) {
                posX += MathUtil.nextCoord(level.random);
                posZ += MathUtil.nextCoord(level.random);
                mPos.set(posX, posY, posZ);
                double centerX = (double) posX + 0.5d;
                double centerZ = (double) posZ + 0.5d;
                Player player = level.getNearestPlayer(centerX, posY, centerZ, -1, false);
                if (player == null) return;

                double distanceToPlayer = player.distanceToSqr(centerX, posY, centerZ);
                if (!isRightDistanceToPlayerAndSpawnPoint(level, chunk, mPos, distanceToPlayer)) continue;

                EntityType<? extends Monster> type = MathUtil.nextMonster(level.random);

                if (!isValidSpawnPositionForMonster(level, mPos, distanceToPlayer, type)) continue;

                Monster monster = getMonsterForSpawn(level, type);
                if (monster == null) continue;

                monster.moveTo(centerX, posY, centerZ, level.random.nextFloat() * 360.0f, 0.0f);
                Event.Result res = ForgeEventFactory.canEntitySpawn(monster, level, centerX, posY, centerZ, null, MobSpawnType.NATURAL);
                if (res == Event.Result.DENY) continue;
                if (res == Event.Result.ALLOW || isValidPositionForMonster(level, monster, distanceToPlayer)) {
                    if (!ForgeEventFactory.doSpecialSpawn(monster, level, (float) centerX, (float) posY, (float) centerZ, null, MobSpawnType.NATURAL)) {
                        spawnGroupData = monster.finalizeSpawn(level, level.getCurrentDifficultyAt(monster.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, null);
                    }
                    ++spawnPackSize;
                    level.addFreshEntityWithPassengers(monster);
                    if (spawnPackSize >= ForgeEventFactory.getMaxSpawnPackSize(monster)) continue;
                }
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

    private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel level, ChunkAccess chunk, BlockPos.MutableBlockPos pos, double distance) {
        if (distance <= 576.0d) return false;
        if (level.getSharedSpawnPos().closerToCenterThan(new Vec3((double) pos.getX() + 0.5d, pos.getY(), (double) pos.getZ() + 0.5d), 24.0d))
            return false;
        return chunk.getPos().equals(new ChunkPos(pos)) || level.isNaturalSpawningAllowed(pos);
    }

    private static boolean isValidSpawnPositionForMonster(ServerLevel level, BlockPos.MutableBlockPos pos, double distance, EntityType<? extends Monster> type) {
        double despawnDistance = type.getCategory().getDespawnDistance();
        if (!type.canSpawnFarFromPlayer() && distance > despawnDistance * despawnDistance)
            return false;

        if (type.canSummon() && canSpawnMonsterAt(level, pos, type)) {
            SpawnPlacements.Type placementType = SpawnPlacements.getPlacementType(type);
            if (!NaturalSpawner.isSpawnPositionOk(placementType, level, pos, type)) return false;
            if (!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, level.random)) return false;
            return level.noCollision(type.getAABB((double) pos.getX() + 0.5d, pos.getY(), (double) pos.getZ() + 0.5d));
        }
        return false;
    }

    private static boolean isValidPositionForMonster(ServerLevel level, Monster monster, double distance) {
        double despawnDistance = MobCategory.MONSTER.getDespawnDistance();
        if (distance > despawnDistance * despawnDistance && monster.removeWhenFarAway(distance))
            return false;

        return monster.checkSpawnRules(level, MobSpawnType.NATURAL) && monster.checkSpawnObstruction(level);
    }

    private static boolean canSpawnMonsterAt(ServerLevel level, BlockPos pos, EntityType<? extends Monster> type) {
        StructureManager structureManager = level.structureManager();
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();

        WeightedRandomList<MobSpawnSettings.SpawnerData> data = isInNetherFortressBounds(pos, level, structureManager) ?
                structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.FORTRESS).spawnOverrides().get(MobCategory.MONSTER).spawns()
                : chunkGenerator.getMobsAt(level.getBiome(pos), structureManager, MobCategory.MONSTER, pos);

        WeightedRandomList<MobSpawnSettings.SpawnerData> possibleData =
                ForgeEventFactory.getPotentialSpawns(level, MobCategory.MONSTER, pos, data);

        for (MobSpawnSettings.SpawnerData d : possibleData.unwrap()) {
            if (d.type == type) return true;
        }
        return false;
    }

    public static boolean isInNetherFortressBounds(BlockPos pos, ServerLevel level, StructureManager structureManager) {
        if (level.getBlockState(pos.below()).is(Blocks.NETHER_BRICKS)) {
            Structure structure = structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.FORTRESS);
            return structure != null && structureManager.getStructureAt(pos, structure).isValid();
        }
        return false;
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
