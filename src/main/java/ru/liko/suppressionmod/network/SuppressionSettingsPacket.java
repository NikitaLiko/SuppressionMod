package ru.liko.suppressionmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SuppressionSettingsPacket(
        double maxDetectionRange,
        int maxSuppressionLevel,
        int singleBulletImpact,
        double accumulationMultiplier,
        int recoveryRate,
        int targetDecayRate,
        double shakeIntensity,
        double vignetteMaxAlpha,
        boolean enableVelocityScaling,
        boolean enableNonlinearAccumulation,
        int explosionBaseImpact,
        double explosionMaxRange,
        int nearImpactBaseImpact,
        double nearImpactMaxRange,
        double uiTintStrength,
        double uiVignetteStrength,
        double uiFlashStrength,
        double uiFadeInSpeed,
        double uiFadeOutSpeed
) {

    public static void encode(SuppressionSettingsPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.maxDetectionRange);
        buf.writeInt(packet.maxSuppressionLevel);
        buf.writeInt(packet.singleBulletImpact);
        buf.writeDouble(packet.accumulationMultiplier);
        buf.writeInt(packet.recoveryRate);
        buf.writeInt(packet.targetDecayRate);
        buf.writeDouble(packet.shakeIntensity);
        buf.writeDouble(packet.vignetteMaxAlpha);
        buf.writeBoolean(packet.enableVelocityScaling);
        buf.writeBoolean(packet.enableNonlinearAccumulation);
        buf.writeInt(packet.explosionBaseImpact);
        buf.writeDouble(packet.explosionMaxRange);
        buf.writeInt(packet.nearImpactBaseImpact);
        buf.writeDouble(packet.nearImpactMaxRange);
        buf.writeDouble(packet.uiTintStrength);
        buf.writeDouble(packet.uiVignetteStrength);
        buf.writeDouble(packet.uiFlashStrength);
        buf.writeDouble(packet.uiFadeInSpeed);
        buf.writeDouble(packet.uiFadeOutSpeed);
    }

    public static SuppressionSettingsPacket decode(FriendlyByteBuf buf) {
        return new SuppressionSettingsPacket(
                buf.readDouble(),
                buf.readInt(),
                buf.readInt(),
                buf.readDouble(),
                buf.readInt(),
                buf.readInt(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readInt(),
                buf.readDouble(),
                buf.readInt(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public static void handle(SuppressionSettingsPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ru.liko.suppressionmod.client.ClientHooks.handleSettingsPacket(packet)));
        ctx.setPacketHandled(true);
    }
}
