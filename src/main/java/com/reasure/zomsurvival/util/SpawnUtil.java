package com.reasure.zomsurvival.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
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

    public static void spawnZombie(ServerLevel level, ChunkAccess chunk) {
        int minY = level.getMinBuildHeight();
        BlockPos pos = getRandomPosWithin(level, chunk, minY);
        if (pos.getY() > minY) {
            spawnZombie(level, chunk, pos);
        }
    }

    public static void spawnZombie(ServerLevel level, ChunkAccess chunk, BlockPos pos) {
        int posY = pos.getY();
        BlockState block = chunk.getBlockState(pos);
        if (block.isRedstoneConductor(chunk, pos)) return;

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        int spawnPackSize = 0;

        for (int i = 0; i < 3; i++) {
            int posX = pos.getX();
            int posZ = pos.getZ();
            SpawnGroupData spawnGroupData = null;
            int spawnCount = ((int) (level.getDayTime() / 24000L)) * 10;
            int spawnGroupSize = 0;

            for (int j = 0; j < spawnCount; j++) {
                posX += level.random.nextIntBetweenInclusive(-5, 5);
                posZ += level.random.nextIntBetweenInclusive(-5, 5);
                mPos.set(posX, posY, posZ);
                double centerX = (double) posX + 0.5d;
                double centerZ = (double) posZ + 0.5d;
                Player player = level.getNearestPlayer(centerX, posY, centerZ, -1, false);
                if (player == null) return;

                double distanceToPlayer = player.distanceToSqr(centerX, posY, centerZ);
                if (!isRightDistanceToPlayerAndSpawnPoint(level, chunk, mPos, distanceToPlayer)) continue;
                if (!isValidSpawnPositionForZombie(level, mPos, distanceToPlayer)) continue;

                Zombie zombie = getZombieForSpawn(level);
                if (zombie == null) continue;

                zombie.moveTo(centerX, posY, centerZ, level.random.nextFloat() * 360.0f, 0.0f);
                Event.Result res = ForgeEventFactory.canEntitySpawn(zombie, level, centerX, posY, centerZ, null, MobSpawnType.NATURAL);
                if (res == Event.Result.DENY) continue;
                if (res == Event.Result.ALLOW || isValidPositionForZombie(level, zombie, distanceToPlayer)) {
                    if (!ForgeEventFactory.doSpecialSpawn(zombie, level, (float) centerX, (float) posY, (float) centerZ, null, MobSpawnType.NATURAL)) {
                        spawnGroupData = zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, null);
                    }
                    ++spawnPackSize;
                    ++spawnGroupSize;
                    level.addFreshEntityWithPassengers(zombie);
                    LOGGER.info("spawn zombie");
                    if (spawnPackSize >= ForgeEventFactory.getMaxSpawnPackSize(zombie)) return;
                    if (zombie.isMaxGroupSizeReached(spawnGroupSize)) break;
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

    private static boolean isValidSpawnPositionForZombie(ServerLevel level, BlockPos.MutableBlockPos pos, double distance) {
        EntityType<Zombie> zombie = EntityType.ZOMBIE;
        double despawnDistance = zombie.getCategory().getDespawnDistance();
        if (!zombie.canSpawnFarFromPlayer() && distance > despawnDistance * despawnDistance)
            return false;

        if (zombie.canSummon() && canSpawnZombieAt(level, pos)) {
            SpawnPlacements.Type placementType = SpawnPlacements.getPlacementType(zombie);
            if (!NaturalSpawner.isSpawnPositionOk(placementType, level, pos, zombie)) return false;
            if (!SpawnPlacements.checkSpawnRules(zombie, level, MobSpawnType.NATURAL, pos, level.random)) return false;
            return level.noCollision(zombie.getAABB((double) pos.getX() + 0.5d, pos.getY(), (double) pos.getZ() + 0.5d));
        }
        return false;
    }

    private static boolean isValidPositionForZombie(ServerLevel level, Zombie zombie, double distance) {
        double despawnDistance = MobCategory.MONSTER.getDespawnDistance();
        if (distance > despawnDistance * despawnDistance && zombie.removeWhenFarAway(distance))
            return false;

        return zombie.checkSpawnRules(level, MobSpawnType.NATURAL) && zombie.checkSpawnObstruction(level);
    }

    private static boolean canSpawnZombieAt(ServerLevel level, BlockPos pos) {
        StructureManager structureManager = level.structureManager();
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();

        WeightedRandomList<MobSpawnSettings.SpawnerData> data = isInNetherFortressBounds(pos, level, structureManager) ?
                structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.FORTRESS).spawnOverrides().get(MobCategory.MONSTER).spawns()
                : chunkGenerator.getMobsAt(level.getBiome(pos), structureManager, MobCategory.MONSTER, pos);

        WeightedRandomList<MobSpawnSettings.SpawnerData> possibleData =
                ForgeEventFactory.getPotentialSpawns(level, MobCategory.MONSTER, pos, data);

        for (MobSpawnSettings.SpawnerData d : possibleData.unwrap()) {
            if (d.type == EntityType.ZOMBIE) return true;
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
    private static Zombie getZombieForSpawn(ServerLevel level) {
        try {
            Entity entity = EntityType.ZOMBIE.create(level);
            if (entity instanceof Zombie zombie) {
                return zombie;
            }
            LOGGER.warn("Can't spawn zombie");
        } catch (Exception exception) {
            LOGGER.warn("Failed to create zombie", exception);
        }
        return null;
    }
}
