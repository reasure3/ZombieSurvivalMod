package com.reasure.zomsurvival.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.reasure.zomsurvival.util.MathUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class DaytimeCommand {
    public DaytimeCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("daytime")
                .then(Commands.literal("set")
                        .then(Commands.literal("time")
                                .then(Commands.literal("day")
                                        .executes(command -> setTime(command.getSource(), 1000))
                                ).then(Commands.literal("noon")
                                        .executes(command -> setTime(command.getSource(), 6000))
                                ).then(Commands.literal("night")
                                        .executes(command -> setTime(command.getSource(), 13000))
                                ).then(Commands.literal("midnight")
                                        .executes(command -> setTime(command.getSource(), 18000))
                                ).then(Commands.argument("time", IntegerArgumentType.integer(0, 23999))
                                        .executes(command -> setTime(command.getSource(), IntegerArgumentType.getInteger(command, "time")))
                                )
                        ).then(Commands.literal("day")
                                .then(Commands.argument("day", IntegerArgumentType.integer(0))
                                        .executes(command -> setDay(command.getSource(), IntegerArgumentType.getInteger(command, "day")))
                                )
                        )
                ).then(Commands.literal("get")
                        .then(Commands.literal("time")
                                .executes(command -> getTime(command.getSource()))
                        )
                        .then(Commands.literal("day")
                                .executes(command -> getDay(command.getSource()))
                        )
                )
        );
    }

    // 현재 일수를 유지하면서 시간대만 바꿈
    private static int setTime(CommandSourceStack source, int time) {
        ServerLevel level = source.getLevel();
        int day = MathUtil.getDay(level);
        long dayTime = MathUtil.dayToDaytime(day, time);
        level.setDayTime(dayTime);
        source.sendSuccess(Component.literal("Set ").append(timeComponent(day, MathUtil.getTime(level), dayTime)), true);
        return 1;
    }

    // 현재 시간대를 유지하면서 일수만 바꿈
    private static int setDay(CommandSourceStack source, int day) {
        ServerLevel level = source.getLevel();
        int time = MathUtil.getTime(level);
        long dayTime = MathUtil.dayToDaytime(day, time);
        level.setDayTime(dayTime);
        source.sendSuccess(Component.literal("Set ").append(timeComponent(day, time, dayTime)), true);
        return 1;
    }

    private static int getTime(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        int time = MathUtil.getTime(level);
        source.sendSuccess(timeComponent(MathUtil.getDay(level), time, level.getDayTime()), false);
        return time;
    }

    private static int getDay(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        int day = MathUtil.getDay(level);
        source.sendSuccess(timeComponent(day, MathUtil.getTime(level), level.getDayTime()), false);
        return day;
    }

    private static Component timeComponent(int day, int time, long daytime) {
        return Component.literal("day: " + day + " time: " + time + " (fullTime: " + daytime + ")");
    }
}
