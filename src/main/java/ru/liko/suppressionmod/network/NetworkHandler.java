package ru.liko.suppressionmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.liko.suppressionmod.SuppressionMod;

public final class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SuppressionMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static boolean initialized = false;
    private static int packetId = 0;

    private NetworkHandler() {}

    public static void init() {
        if (initialized) return;
        CHANNEL.registerMessage(packetId++, SuppressionImpactPacket.class,
                SuppressionImpactPacket::encode,
                SuppressionImpactPacket::decode,
                SuppressionImpactPacket::handle);
        initialized = true;
    }

    public static <MSG> void sendTo(ServerPlayer player, MSG message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
