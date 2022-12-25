package com.reasure.zomsurvival.util;

import net.minecraftforge.common.ForgeConfigSpec;


public class SpawnConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.EnumValue<ModEnums.SpawnCountType> SPAWN_COUNT_TYPE;
    public static final ForgeConfigSpec.ConfigValue<Integer> SPAWN_INCREASE_AMOUNT;
    public static final ForgeConfigSpec.EnumValue<ModEnums.RandomCoordType> RANDOM_COORD_TYPE;

    static {
        BUILDER.push("Configs for Zombie Survivor mod");

        SPAWN_COUNT_TYPE = BUILDER.comment("기준 좌표 별 좀비 스폰 시도 횟수 설정 방법")
                .comment("DEFAULT: 3 * spawn increase amount * 일수")
                .comment("RANDOM: rand(0 ~ 3 * spawn increase amount * 일수)")
                .defineEnum("Spawn Count Type", ModEnums.SpawnCountType.DEFAULT);

        SPAWN_INCREASE_AMOUNT = BUILDER.comment("날이 지날 때, 증가되는 좀비 스폰 량")
                .defineInRange("Spawn Increase Amount", 10, 0, 10000);

        RANDOM_COORD_TYPE = BUILDER.comment("각 기준 좌표에서 랜덤 좌표를 설정 방법. 기준 좌표의 x, z 좌표에 해당 수식으로 나온 값을 더함.")
                .comment("TRIANGLE: rand(0 ~ 5) - rand(0 ~ 5): -5 ~ 5 중에서 0이 나올 확률이 높음. 마크 바닐라 방식")
                .comment("UNIFORM: rand(-5 ~ 5): -5 ~ 5가 골고루 나옴.")
                .defineEnum("Random Coord Type", ModEnums.RandomCoordType.TRIANGLE);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
