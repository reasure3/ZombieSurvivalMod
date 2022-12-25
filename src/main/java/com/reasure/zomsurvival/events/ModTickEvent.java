package com.reasure.zomsurvival.events;

import com.mojang.logging.LogUtils;
import com.reasure.zomsurvival.ZomSurvival;
import com.reasure.zomsurvival.util.SpawnUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(modid = ZomSurvival.MODID)
public class ModTickEvent {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onTick(final TickEvent.LevelTickEvent event) {
        if (event.level instanceof ServerLevel server) {
            boolean isRaining = server.isRaining();
            int day = ((int) (server.getDayTime() / 24000L)) * 10;
            int time = (int) (server.getDayTime() % 24000L);
            int startTime = isRaining ? 12969 : 13188;
            int endTime = isRaining ? 23031 : 22812;

            if (time >= startTime && time < endTime) {
                List<ServerPlayer> players = server.getPlayers((p -> (!p.isCreative() && !p.isSpectator()) && p.level == server));
                for (ServerPlayer player : players) {
                    SpawnUtil.spawnZombie(server, server.getChunk(player.blockPosition()), day);
                }
            }
        }
    }
}
