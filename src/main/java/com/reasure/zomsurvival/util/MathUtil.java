package com.reasure.zomsurvival.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class MathUtil {
    private static final SimpleWeightedRandomList<EntityType<? extends Monster>> spawnMonsters;

    static {
        spawnMonsters = new SimpleWeightedRandomList.Builder<EntityType<? extends Monster>>()
                .add(EntityType.ZOMBIE, SpawnConfig.ZOMBIE_SPAWN_WEIGHT.get())
                .add(EntityType.ZOMBIE_VILLAGER, SpawnConfig.ZOMBIE_VILLAGER_SPAWN_WEIGHT.get())
                .add(EntityType.HUSK, SpawnConfig.HUSK_SPAWN_WEIGHT.get())
                .add(EntityType.STRAY, SpawnConfig.STRAY_SPAWN_WEIGHT.get())
                .add(EntityType.DROWNED, SpawnConfig.DROWNED_SPAWN_WEIGHT.get())
                .add(EntityType.SKELETON, SpawnConfig.SKELETON_SPAWN_WEIGHT.get())
                .add(EntityType.CREEPER, SpawnConfig.CREEPER_SPAWN_WEIGHT.get())
                .add(EntityType.SPIDER, SpawnConfig.SPIDER_SPAWN_WEIGHT.get())
                .add(EntityType.WITCH, SpawnConfig.WITCH_SPAWN_WEIGHT.get())
                .build();
    }

    public static BlockPos getRandomPosWithin(RandomSource random, ChunkAccess chunk, int minY) {
        ChunkPos chunkpos = chunk.getPos();
        int posX = chunkpos.getMinBlockX() + random.nextInt(16);
        int posZ = chunkpos.getMinBlockZ() + random.nextInt(16);
        int maxY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, posX, posZ) + 1;
        int posY = Mth.randomBetweenInclusive(random, minY, maxY);
        return new BlockPos(posX, posY, posZ);
    }

    public static int getSpawnCount(int day, RandomSource random) {
        return switch (SpawnConfig.SPAWN_COUNT_TYPE.get()) {
            case DEFAULT -> day * SpawnConfig.SPAWN_INCREASE_AMOUNT.get();
            case RANDOM -> Mth.ceil(random.nextFloat() * day * (float) SpawnConfig.SPAWN_INCREASE_AMOUNT.get());
        };
    }

    public static int nextCoord(RandomSource random) {
        return switch (SpawnConfig.RANDOM_COORD_TYPE.get()) {
            case TRIANGLE -> random.nextInt(6) - random.nextInt(6);
            case UNIFORM -> random.nextIntBetweenInclusive(-5, 5);
        };
    }

    public static EntityType<? extends Monster> nextMonster(RandomSource random) {
        return spawnMonsters.getRandomValue(random).orElse(null);
    }

    public static int getDay(ServerLevel level) {
        return (int) (level.getDayTime() / 24000L);
    }

    public static int getTime(ServerLevel level) {
        return (int) (level.getDayTime() % 24000L);
    }

    public static long dayToDaytime(int day, int time) {
        return (long) day * 24000L + (long) time;
    }
}
