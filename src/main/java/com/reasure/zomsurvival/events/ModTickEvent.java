package com.reasure.zomsurvival.events;

import com.mojang.logging.LogUtils;
import com.reasure.zomsurvival.ZomSurvival;
import com.reasure.zomsurvival.util.SpawnUtil;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.common.world.ForgeChunkManager;
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
            if (server.isDebug()) return;;

            boolean isRaining = server.isRaining();
            int day = (int) (server.getDayTime() / 24000L);
            int time = (int) (server.getDayTime() % 24000L);
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
}
