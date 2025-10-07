package ru.liko.suppressionmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SuppressionImpactPacket(Kind kind, double x, double y, double z, float magnitude) {

    public enum Kind {
        IMPACT,
        EXPLOSION
    }

    public static SuppressionImpactPacket impact(double x, double y, double z, float velocity) {
        return new SuppressionImpactPacket(Kind.IMPACT, x, y, z, velocity);
    }

    public static SuppressionImpactPacket explosion(double x, double y, double z, float power) {
        return new SuppressionImpactPacket(Kind.EXPLOSION, x, y, z, power);
    }

    public static void encode(SuppressionImpactPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.kind);
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeFloat(packet.magnitude);
    }

    public static SuppressionImpactPacket decode(FriendlyByteBuf buf) {
        Kind kind = buf.readEnum(Kind.class);
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float magnitude = buf.readFloat();
        return new SuppressionImpactPacket(kind, x, y, z, magnitude);
    }

    public static void handle(SuppressionImpactPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ru.liko.suppressionmod.client.ClientHooks.handleSuppressionPacket(packet)));
        ctx.setPacketHandled(true);
    }
}
