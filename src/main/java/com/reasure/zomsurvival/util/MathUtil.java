package com.reasure.zomsurvival.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class MathUtil {
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

    public static boolean nextZombieVillager(RandomSource random) {
        return random.nextInt(10) == 0;
    }
}
