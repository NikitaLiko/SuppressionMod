package ru.liko.suppressionmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.liko.suppressionmod.BulletSuppressionHandler;
import ru.liko.suppressionmod.SuppressionMod;
import ru.liko.suppressionmod.network.SuppressionImpactPacket;

@OnlyIn(Dist.CLIENT)
public final class ClientHooks {

    private ClientHooks() {}

    public static void handleSuppressionPacket(SuppressionImpactPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        Player player = mc.player;
        BulletSuppressionHandler handler = SuppressionMod.getClientHandler();
        if (player == null || handler == null) return;

        Vec3 target = new Vec3(packet.x(), packet.y(), packet.z());
        double distance = Math.sqrt(player.distanceToSqr(target));
        if (packet.kind() == SuppressionImpactPacket.Kind.IMPACT) {
            handler.addProjectileImpact(distance, packet.magnitude());
        } else if (packet.kind() == SuppressionImpactPacket.Kind.EXPLOSION) {
            handler.addExplosionShock(distance, packet.magnitude());
        }
    }
}
