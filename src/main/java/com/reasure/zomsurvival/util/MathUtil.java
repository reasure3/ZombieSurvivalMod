package com.reasure.zomsurvival.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;

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
}
