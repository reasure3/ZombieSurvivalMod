package com.reasure.zomsurvival;

import com.reasure.zomsurvival.util.SpawnConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ZomSurvival.MODID)
public class ZomSurvival {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "zomsurvival";

    public ZomSurvival() {
//        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpawnConfig.SPEC, "Zombie Survivor-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }
}
