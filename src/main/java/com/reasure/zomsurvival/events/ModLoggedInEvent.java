package com.reasure.zomsurvival.events;

import com.mojang.logging.LogUtils;
import com.reasure.zomsurvival.ZomSurvival;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = ZomSurvival.MODID)
public class ModLoggedInEvent {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onWorldJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        ModContainer container = ModList.get().getModContainerById(ZomSurvival.MODID).orElse(null);
        if (container != null) {
            VersionChecker.CheckResult result = VersionChecker.getResult(container.getModInfo());
            LOGGER.info("status: {}", result.status());
            LOGGER.info("changes: {}", result.changes());
            LOGGER.info("target: {}", result.target());
            LOGGER.info("url: {}", result.url());
            if (result.status() == VersionChecker.Status.OUTDATED || result.status() == VersionChecker.Status.BETA_OUTDATED) {
                player.displayClientMessage(Component.literal("[Zombie Survivor] There is new version."), false);
                player.displayClientMessage(Component.literal("[Zombie Survivor] version " + result.target() + ": " + result.url()), false);
            }
        }
    }
}
