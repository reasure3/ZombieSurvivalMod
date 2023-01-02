package com.reasure.zomsurvival.events;

import com.reasure.zomsurvival.ZomSurvival;
import com.reasure.zomsurvival.util.MathUtil;
import com.reasure.zomsurvival.util.MonsterUtil;
import com.reasure.zomsurvival.util.SpawnUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ZomSurvival.MODID)
public class SpawnEvents {
    @SubscribeEvent
    public static void onTick(final TickEvent.LevelTickEvent event) {
        if (event.level instanceof ServerLevel server) {
            if (server.isDebug()) return;

            int time = MathUtil.getTime(server);
            if (time % 5 != 0) return;

            boolean isRaining = server.isRaining();
            int day = MathUtil.getDay(server);
            int startTime = isRaining ? 12969 : 13188;
            int endTime = isRaining ? 23031 : 22812;

            if (time >= startTime && time < endTime) {
                List<ServerPlayer> players = server.getPlayers(p -> p.isAlive() && !p.isSpectator());
                for (ServerPlayer player : players) {
                    SpawnUtil.spawnMonster(server, server.getChunk(player.blockPosition()), day);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntitySpawn(final EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Monster monster) {
            MonsterUtil.reinforceMonster(monster, MathUtil.getDay(level));
        }
    }
}
