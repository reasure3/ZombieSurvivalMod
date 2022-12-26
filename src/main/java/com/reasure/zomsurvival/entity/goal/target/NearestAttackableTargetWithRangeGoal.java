package com.reasure.zomsurvival.entity.goal.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class NearestAttackableTargetWithRangeGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public NearestAttackableTargetWithRangeGoal(Mob mob, Class<T> targetType, double range) {
        super(mob, targetType, false);
        this.targetConditions = TargetingConditions.forCombat().range(range);
    }
}
