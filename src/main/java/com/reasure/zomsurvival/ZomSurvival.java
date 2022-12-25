package com.reasure.zomsurvival;

import com.mojang.logging.LogUtils;
import com.reasure.zomsurvival.util.SpawnConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ZomSurvival.MODID)
public class ZomSurvival {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "zomsurvival";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public ZomSurvival() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpawnConfig.SPEC, "Zombie Survivor-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }
}
