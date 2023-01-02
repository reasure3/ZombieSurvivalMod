package com.reasure.zomsurvival.entity.goal.target;

import com.reasure.zomsurvival.util.MathUtil;
import com.reasure.zomsurvival.util.SpawnConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class NearestAttackableTargetWithRangeGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    protected double range;
    protected TargetingConditions reinforcedTargetingCondition;

    public NearestAttackableTargetWithRangeGoal(Mob mob, Class<T> targetType, double range) {
        super(mob, targetType, false);
        this.range = range;
        this.reinforcedTargetingCondition = TargetingConditions.forCombat().range(range);
    }

    @Override
    protected void findTarget() {
        if (mob.level instanceof ServerLevel level) {
            if (MathUtil.getDay(level) >= SpawnConfig.ZOMBIE_SET_OR_BREAK_BLOCLK_DAY.get()) {
                if (mob.level.random.nextInt(20) == 0) {
                    target = mob.level.getNearestPlayer(mob.getX(), mob.getEyeY(), mob.getZ(), range, false);
                } else {
                    target = mob.level.getNearestPlayer(reinforcedTargetingCondition, mob, mob.getX(), mob.getEyeY(), mob.getZ());
                }
                return;
            }
        }
        target = mob.level.getNearestPlayer(targetConditions, mob, mob.getX(), mob.getEyeY(), mob.getZ());
    }
}
