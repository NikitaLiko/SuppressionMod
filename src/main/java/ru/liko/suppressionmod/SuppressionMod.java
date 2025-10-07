package ru.liko.suppressionmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;

@Mod(SuppressionMod.MOD_ID)
public class SuppressionMod {
    public static final String MOD_ID = "suppressionmod";

    public SuppressionMod() {
        // Серверный конфиг - автоматически синхронизируется с клиентами
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC, "suppressionmod-server.toml");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            BulletSuppressionHandler handler = new BulletSuppressionHandler();
            MinecraftForge.EVENT_BUS.register(handler);
            MinecraftForge.EVENT_BUS.register(new BulletTracker(handler));
            MinecraftForge.EVENT_BUS.register(new EnvironmentalSuppressionTracker(handler));
            SuppressionOverlay.setHandler(handler);
        }
    }
}
