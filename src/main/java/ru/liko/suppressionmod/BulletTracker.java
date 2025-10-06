package ru.liko.suppressionmod;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class BulletTracker {

    private static final double NEAR_MISS_THRESHOLD = 3.5;
    private static final double MIN_MOVE_PER_TICK = 0.02;

    private static final Set<ResourceLocation> ALLOWED_TYPES = new HashSet<>();
    private final Map<UUID, Tracked> tracked = new HashMap<>();
    private final BulletSuppressionHandler suppression;

    public BulletTracker(BulletSuppressionHandler suppression) {
        this.suppression = suppression;
    }

    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent e) {
        if (!e.getLevel().isClientSide()) return;
        Entity ent = e.getEntity();
        if (isTaczBullet(ent.getType())) {
            tracked.put(ent.getUUID(), new Tracked(ent, ent.position()));
        }
    }

    @SubscribeEvent
    public void onLeave(EntityLeaveLevelEvent e) {
        if (!e.getLevel().isClientSide()) return;
        tracked.remove(e.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.isPaused() || mc.player == null || mc.level == null) return;

        Player pl = mc.player;
        UUID local = pl.getUUID();
        Vec3 eye = pl.getEyePosition(1.0f);
        double maxRange = Config.MAX_DETECTION_RANGE.get();

        Iterator<Map.Entry<UUID, Tracked>> it = tracked.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Tracked> en = it.next();
            Tracked t = en.getValue();
            Entity ent = t.entity;

            if (ent == null || ent.isRemoved() || ent.level() != mc.level) {
                it.remove();
                continue;
            }

            t.age++;

            if (ent instanceof Projectile proj) {
                Entity owner = proj.getOwner();
                if (owner != null && owner.getUUID().equals(local)) {
                    t.prev = ent.position();
                    continue;
                }
            }

            Vec3 curr = ent.position();
            Vec3 prev = t.prev;
            double velocity = curr.distanceTo(prev);
            double camDist = curr.distanceTo(eye);

            if (camDist > maxRange || velocity < MIN_MOVE_PER_TICK) {
                t.prev = curr;
                continue;
            }

            double d = segmentPointDistance(prev, curr, eye);

            if (t.cooldown <= 0 && d <= NEAR_MISS_THRESHOLD) {
                int baseImpact = Config.SINGLE_BULLET_IMPACT.get();
                suppression.addSuppressionImpact(baseImpact, d, velocity);
                t.cooldown = 4;
            }

            if (t.cooldown > 0) t.cooldown--;
            t.prev = curr;
        }
    }

    private static boolean isTaczBullet(EntityType<?> type) {
        ResourceLocation rl = EntityType.getKey(type);
        if (rl == null) return false;
        if (!ALLOWED_TYPES.isEmpty()) return ALLOWED_TYPES.contains(rl);
        String ns = rl.getNamespace();
        String path = rl.getPath();
        return ("tacz".equals(ns)) ||
                path.contains("bullet") ||
                path.contains("ammo") ||
                path.contains("projectile");
    }

    private static double segmentPointDistance(Vec3 a, Vec3 b, Vec3 p) {
        Vec3 ab = b.subtract(a);
        double ab2 = ab.lengthSqr();
        if (ab2 < 1e-8) return p.distanceTo(a);
        double t = (p.subtract(a)).dot(ab) / ab2;
        t = Math.max(0.0, Math.min(1.0, t));
        Vec3 c = a.add(ab.scale(t));
        return p.distanceTo(c);
    }

    private static class Tracked {
        final UUID uuid;
        final Entity entity;
        Vec3 prev;
        int cooldown = 0;
        int age = 0;
        Tracked(Entity e, Vec3 p) {
            this.uuid = e.getUUID();
            this.entity = e;
            this.prev = p;
        }
    }
}
