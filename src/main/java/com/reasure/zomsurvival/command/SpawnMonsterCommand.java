package com.reasure.zomsurvival.command;

import com.mojang.brigadier.CommandDispatcher;
import com.reasure.zomsurvival.util.MathUtil;
import com.reasure.zomsurvival.util.MonsterUtil;
import com.reasure.zomsurvival.util.SpawnUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

public class SpawnMonsterCommand {
    public SpawnMonsterCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawnMonster")
                .then(Commands.literal("zombie")
                        .executes(command -> spawnMonster(command.getSource(), EntityType.ZOMBIE))
                ).then(Commands.literal("zombieVillager")
                        .executes(command -> spawnMonster(command.getSource(), EntityType.ZOMBIE_VILLAGER))
                ).then(Commands.literal("husk")
                        .executes(command -> spawnMonster(command.getSource(), EntityType.HUSK))
                ).then(Commands.literal("drowned")
                        .executes(command -> spawnMonster(command.getSource(), EntityType.DROWNED))
                ).then(Commands.literal("skeleton")
                        .executes(command -> spawnMonster(command.getSource(), EntityType.SKELETON))
                ).then(Commands.literal("stray")
                        .executes(command -> spawnMonster(command.getSource(), EntityType.STRAY))
                ).then(Commands.literal("creeper")
                        .executes(command -> spawnMonster(command.getSource(), EntityType.CREEPER))
                ).then(Commands.literal("spider")
                        .executes(command -> spawnMonster(command.getSource(), EntityType.SPIDER))
                ).then(Commands.literal("witch")
                        .executes(command -> spawnMonster(command.getSource(), EntityType.WITCH))
                ).then(Commands.literal("random")
                        .executes(command -> spawnMonster(command.getSource(), MathUtil.nextMonster(command.getSource().getLevel().random)))
                )
        );
    }

    private int spawnMonster(CommandSourceStack source, EntityType<? extends Monster> monsterType) {
        Vec3 pos = source.getPosition();
        ServerLevel level = source.getLevel();
        Monster monster = SpawnUtil.getMonsterForSpawn(level, monsterType);
        if (monster == null) {
            source.sendFailure(Component.literal("Fail to spawn " + monsterType.getDescriptionId()));
            return -1;
        }
        MonsterUtil.reinforceMonster(level, monster, MathUtil.getDay(level));
        monster.moveTo(pos.x(), pos.y(), pos.z(), level.random.nextFloat() * 360.0f, 0.0f);
        monster.finalizeSpawn(level, level.getCurrentDifficultyAt(monster.blockPosition()), MobSpawnType.COMMAND, null, null);
        if (!level.tryAddFreshEntityWithPassengers(monster)) {
            source.sendFailure(Component.literal("Fail to spawn " + monsterType.getDescriptionId()));
            return 0;
        }
        source.sendSuccess(Component.literal("Successfully spawn " + monsterType.getDescriptionId()), true);
        return 1;
    }
}
