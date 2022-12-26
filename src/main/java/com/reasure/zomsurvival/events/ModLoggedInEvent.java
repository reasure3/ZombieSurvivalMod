package com.reasure.zomsurvival.events;

import com.mojang.logging.LogUtils;
import com.reasure.zomsurvival.ZomSurvival;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
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
            String changes = result.changes().get(result.target());
            if (changes == null) changes = "Fail to load changelog";
            LOGGER.info("status: {}", result.status());
            LOGGER.info("changes: {}", result.changes());
            LOGGER.info("target: {}", result.target());
            LOGGER.info("url: {}", result.url());

            if (result.status() == VersionChecker.Status.OUTDATED || result.status() == VersionChecker.Status.BETA_OUTDATED) {
                Component downloadComponent = Component.literal(" [download]").withStyle(Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result.url()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(result.url())))
                );

                Component changelogComponent = Component.literal(" [changes]").withStyle(Style.EMPTY
                        .withItalic(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellraw @s \"[Zombie Survivor "
                                + result.target() + " Changelogs]\\n" + changes.replace("\n", "\\n") + "\""))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to view changelog")))
                );

                player.displayClientMessage(
                        Component.literal("[Zombie Survivor] There is new version: " + result.target())
                                .append(downloadComponent)
                                .append(changelogComponent),
                        false);
            }
        }
    }
}
