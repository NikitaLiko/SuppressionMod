package ru.liko.suppressionmod;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.liko.suppressionmod.client.ClientConfigState;
import ru.liko.suppressionmod.network.NetworkHandler;

@Mod(SuppressionMod.MOD_ID)
public class SuppressionMod {
    public static final String MOD_ID = "suppressionmod";

    @OnlyIn(Dist.CLIENT)
    private static BulletSuppressionHandler CLIENT_HANDLER;

    public SuppressionMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC, "suppressionmod-server.toml");
        NetworkHandler.init();
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> NetworkHandler.PROTOCOL_VERSION,
                        (remoteVersion, isServer) -> NetworkHandler.PROTOCOL_VERSION.equals(remoteVersion)
                ));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientConfigState.reloadFromLocalConfig();
            BulletSuppressionHandler handler = new BulletSuppressionHandler();
            CLIENT_HANDLER = handler;
            MinecraftForge.EVENT_BUS.register(handler);
            MinecraftForge.EVENT_BUS.register(new BulletTracker(handler));
            MinecraftForge.EVENT_BUS.register(new EnvironmentalSuppressionTracker(handler));
            SuppressionOverlay.setHandler(handler);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static BulletSuppressionHandler getClientHandler() {
        return CLIENT_HANDLER;
    }
}
