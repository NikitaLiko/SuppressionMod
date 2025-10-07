package ru.liko.suppressionmod;

import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.liko.suppressionmod.network.NetworkHandler;
import ru.liko.suppressionmod.network.SuppressionImpactPacket;

@Mod.EventBusSubscriber(modid = SuppressionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerSuppressionListener {

    private ServerSuppressionListener() {}

    @SubscribeEvent
    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        EntityKineticBullet ammo = event.getAmmo();
        if (ammo == null) return;

        BlockHitResult hit = event.getHitResult();
        Vec3 hitPos = hit.getLocation();
        double maxRange = Config.NEAR_IMPACT_MAX_RANGE.get();
        float speed = (float)ammo.getDeltaMovement().length();

        Entity owner = ammo.getOwner();

        for (Player player : level.players()) {
            if (!(player instanceof ServerPlayer serverPlayer)) continue;
            if (player.isSpectator()) continue;
            if (owner != null && player.getUUID().equals(owner.getUUID())) continue;

            double distance = Math.sqrt(player.distanceToSqr(hitPos));
            if (distance > maxRange) continue;

            NetworkHandler.sendTo(serverPlayer, SuppressionImpactPacket.impact(hitPos.x, hitPos.y, hitPos.z, speed));
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        Explosion explosion = event.getExplosion();
        Vec3 pos = explosion.getPosition();
        double maxRange = Config.EXPLOSION_MAX_RANGE.get();
        int affectedBlocks = explosion.getToBlow().size();
        float power = 1.0f + Math.min(3.5f, affectedBlocks / 30.0f);

        for (Player player : level.players()) {
            if (!(player instanceof ServerPlayer serverPlayer)) continue;
            if (player.isSpectator()) continue;

            double distance = Math.sqrt(player.distanceToSqr(pos));
            if (distance > maxRange) continue;

            NetworkHandler.sendTo(serverPlayer, SuppressionImpactPacket.explosion(pos.x, pos.y, pos.z, power));
        }
    }
}
