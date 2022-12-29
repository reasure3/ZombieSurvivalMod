package com.reasure.zomsurvival.util;

import com.mojang.logging.LogUtils;
import com.reasure.zomsurvival.entity.goal.target.NearestAttackableTargetWithRangeGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.UUID;

public class MonsterUtil {
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("CC9CB0C4-D1CD-4734-B726-3B2653A46EC8");
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void reinforceMonster(ServerLevel level, Monster monster, int day) {
        if (monster instanceof Zombie zombie) {
            if (day >= SpawnConfig.ZOMBIE_ADD_FOLLOWING_RANGE_DAY.get()) {
                reinforceZombieRange(zombie);
            }
            if (day >= SpawnConfig.ZOMBIE_SPEED_UP_PER_DAY.get()) {
                reinforceZombieSpeed(zombie, day / SpawnConfig.ZOMBIE_SPEED_UP_PER_DAY.get());
            }
        }
    }

    public static void reinforceZombieRange(Zombie zombie) {
        zombie.targetSelector.removeAllGoals(goal ->
                goal instanceof NearestAttackableTargetGoal<?> targetGoal
                        && targetGoal.targetType == Player.class);
        zombie.targetSelector.addGoal(2,
                new NearestAttackableTargetWithRangeGoal<>(zombie, Player.class, SpawnConfig.ZOMBIE_FOLLOWING_RANGE_MODIFIER.get()));
    }

    public static void reinforceZombieSpeed(Zombie zombie, int speedLevel) {
        AttributeInstance speedAttribute = zombie.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;
        speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
        double speedUp = Math.min(SpawnConfig.ZOMBIE_SPEED_ADDER.get() * speedLevel, SpawnConfig.ZOMBIE_MAX_SPEED_ADDER.get());
        speedAttribute.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_UUID, "Zomvival speed boost", speedUp, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
