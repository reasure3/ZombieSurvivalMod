package com.reasure.zomsurvival.util;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import org.slf4j.Logger;

import java.util.UUID;

public class AttributeUtil {
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("CC9CB0C4-D1CD-4734-B726-3B2653A46EC8");
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void reinforceZombieSpeed(Zombie zombie, int day) {
        if (day < SpawnConfig.ZOMBIE_SPEED_UP_PER_DAY.get()) return;
        AttributeInstance speedAttribute = zombie.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;
        speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
        int upDay = day / SpawnConfig.ZOMBIE_SPEED_UP_PER_DAY.get();
        double speedUp = Math.min(SpawnConfig.ZOMBIE_SPEED_ADDER.get() * upDay, SpawnConfig.ZOMBIE_MAX_SPEED_ADDER.get());
        speedAttribute.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_UUID, "Zomvival speed boost", speedUp, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
