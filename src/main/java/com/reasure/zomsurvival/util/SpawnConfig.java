package com.reasure.zomsurvival.util;

import net.minecraftforge.common.ForgeConfigSpec;


public class SpawnConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.EnumValue<ModEnums.SpawnCountType> SPAWN_COUNT_TYPE;
    public static final ForgeConfigSpec.ConfigValue<Integer> SPAWN_INCREASE_AMOUNT;
    public static final ForgeConfigSpec.EnumValue<ModEnums.RandomCoordType> RANDOM_COORD_TYPE;

    public static final ForgeConfigSpec.ConfigValue<Integer> ZOMBIE_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> ZOMBIE_VILLAGER_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> HUSK_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> DROWNED_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> SKELETON_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> STRAY_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> CREEPER_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> SPIDER_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> WITCH_SPAWN_WEIGHT;

    public static final ForgeConfigSpec.ConfigValue<Double> ZOMBIE_FOLLOWING_RANGE_MODIFIER;
    public static final ForgeConfigSpec.ConfigValue<Integer> ZOMBIE_ADD_FOLLOWING_RANGE_DAY;

    public static final ForgeConfigSpec.ConfigValue<Double> ZOMBIE_SPEED_ADDER;
    public static final ForgeConfigSpec.ConfigValue<Integer> ZOMBIE_SPEED_UP_PER_DAY;

    public static final ForgeConfigSpec.ConfigValue<Double> ZOMBIE_MAX_SPEED_ADDER;

    static {
        BUILDER.push("Configs for Zombie Survivor mod");

        SPAWN_COUNT_TYPE = BUILDER.comment("기준 좌표 별 좀비 스폰 시도 횟수 설정 방법")
                .comment("DEFAULT: 3 * spawn increase amount * 일수")
                .comment("RANDOM: rand(0 ~ spawn increase amount * 일수)")
                .defineEnum("Spawn Count Type", ModEnums.SpawnCountType.DEFAULT);

        SPAWN_INCREASE_AMOUNT = BUILDER.comment("날이 지날 때, 증가되는 좀비 스폰 량")
                .defineInRange("Spawn Increase Amount", 50, 0, 10000);

        RANDOM_COORD_TYPE = BUILDER.comment("각 기준 좌표에서 랜덤 좌표를 설정 방법. 기준 좌표의 x, z 좌표에 해당 수식으로 나온 값을 더함.")
                .comment("TRIANGLE: rand(0 ~ 5) - rand(0 ~ 5): -5 ~ 5 중에서 0이 나올 확률이 높음. 마크 바닐라 방식")
                .comment("UNIFORM: rand(-5 ~ 5): -5 ~ 5가 골고루 나옴.")
                .defineEnum("Random Coord Type", ModEnums.RandomCoordType.TRIANGLE);

        ZOMBIE_SPAWN_WEIGHT = BUILDER.comment("좀비 스폰 가중치")
                .define("Zombie Spawn Weight", 1000);

        ZOMBIE_VILLAGER_SPAWN_WEIGHT = BUILDER.comment("주민 좀비 스폰 가중치")
                .define("Zombie Villager Spawn Weight", 100);

        HUSK_SPAWN_WEIGHT = BUILDER.comment("허스크 스폰 가중치")
                .define("Husk Spawn Weight", 250);

        DROWNED_SPAWN_WEIGHT = BUILDER.comment("드라운드 스폰 가중치")
                .define("Drowned Spawn Weidht", 350);

        SKELETON_SPAWN_WEIGHT = BUILDER.comment("스켈레톤 스폰 가중치")
                .define("Skeleton Spawn Weight", 0);

        STRAY_SPAWN_WEIGHT = BUILDER.comment("스트레이 스폰 가중치")
                .define("Stray Spawn Weight", 0);

        CREEPER_SPAWN_WEIGHT = BUILDER.comment("크리퍼 스폰 가중치")
                .define("Creeper Spawn Weight", 0);

        SPIDER_SPAWN_WEIGHT = BUILDER.comment("거미 스폰 가중치")
                .define("Spider Spawn Weight", 0);

        WITCH_SPAWN_WEIGHT = BUILDER.comment("마녀 스폰 가중치")
                .define("Witch Spawn Weight", 0);

        ZOMBIE_FOLLOWING_RANGE_MODIFIER = BUILDER.comment("강화할 좀비 인식 사거리")
                .define("Zombie Following Range Adder", 200.0D);

        ZOMBIE_ADD_FOLLOWING_RANGE_DAY = BUILDER.comment("좀비 인식 사거리 강화를 시작할 일 수")
                .define("Zombie Add Following Range Day", 0);

        ZOMBIE_SPEED_ADDER = BUILDER.comment("Zombie Speed Up Per Day 마다 강화될 속도 (바닐라에서 좀비의 기본 속도는 0.23)")
                .comment("Ex) Zombie Speed Up Per Day가 5일 이고, Zombie Speed Adder가 0.1이고, 현재 15일 일경우, 0.23 + 0.23 * 0.1배 * 3일")
                .comment("즉, 기존 속도의 0.1배, 0.2배, 0.3배, ... 가 더해지는 식으로 강화됨.")
                .defineInRange("Zombie Speed Adder", 0.3D, 0.0D, 10.0D);

        ZOMBIE_SPEED_UP_PER_DAY = BUILDER.comment("해당 일 수마다 좀비 속도가 강화됨.")
                .define("Zombie Speed Up Per Day", 5);

        ZOMBIE_MAX_SPEED_ADDER = BUILDER.comment("좀비의 기본 속도에서 최대 몇배까지 더할지 설정")
                .defineInRange("Zombie Max Speed Adder", 10.D, 0.0D, 100.0D);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
