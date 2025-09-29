package com.slayvisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class KillAura {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final Random RANDOM = new Random();
    private static final float TARGET_RANGE = 4.5f;
    private static final double TARGET_RANGE_SQ = TARGET_RANGE * TARGET_RANGE;
    private static final long POINT_REFRESH_INTERVAL = 6L;
    private static final double MIN_POINT_DISTANCE_SQ = 0.28 * 0.28;
    private static final long BASE_ATTACK_INTERVAL_MS = 120L;

    private static KeyBinding toggleKey;
    private static boolean enabled;
    private static LivingEntity currentTarget;
    private static long lastAttackTimeMs;
    private static long lastAttackTick = -1L;
    private static int lastTargetEntityId = -1;

    private static final AimPointController AIM_POINT = new AimPointController();

    private KillAura() {
    }

    public static void init() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.slayvisual.killaura",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.slayvisual"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                currentTarget = null;
                AIM_POINT.reset();
                return;
            }

            processToggle(client);

            if (!enabled) {
                currentTarget = null;
                AIM_POINT.reset();
                return;
            }

            handleKillAura(client.player);
        });
    }

    private static void processToggle(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            currentTarget = null;
            AIM_POINT.reset();
            if (client.player != null) {
                client.player.sendMessage(new LiteralText("KillAura " + (enabled ? "enabled" : "disabled")), true);
            }
        }
    }

    private static void handleKillAura(ClientPlayerEntity player) {
        LivingEntity target = selectTarget(player);
        currentTarget = target;

        if (target == null) {
            return;
        }

        AIM_POINT.update(target, player.world.getTime());
        rotateTowards(player, target);
        attemptAttack(player, target);
    }

    private static LivingEntity selectTarget(ClientPlayerEntity player) {
        if (currentTarget != null && isValidTarget(player, currentTarget)) {
            return currentTarget;
        }

        Box searchBox = player.getBoundingBox().expand(TARGET_RANGE);
        List<LivingEntity> nearby = player.world.getEntitiesByClass(LivingEntity.class, searchBox,
                entity -> entity != player && isValidTarget(player, entity));

        nearby.sort(Comparator.comparingDouble(player::squaredDistanceTo));
        return nearby.isEmpty() ? null : nearby.get(0);
    }

    private static boolean isValidTarget(ClientPlayerEntity player, LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity.isRemoved()) {
            return false;
        }

        if (!(entity instanceof PlayerEntity)) {
            return false;
        }

        PlayerEntity other = (PlayerEntity) entity;
        if (other.isSpectator() || other.abilities.creativeMode) {
            return false;
        }

        if (player.isTeammate(other)) {
            return false;
        }

        if (player.squaredDistanceTo(entity) > TARGET_RANGE_SQ) {
            return false;
        }

        if (!player.canSee(entity) || entity.isInvisibleTo(player)) {
            return false;
        }

        return true;
    }

    private static void rotateTowards(ClientPlayerEntity player, LivingEntity target) {
        Vec3d eyes = player.getCameraPosVec(1.0f);
        Vec3d aimPoint = AIM_POINT.getPoint(target);

        double dx = aimPoint.x - eyes.x;
        double dy = aimPoint.y - eyes.y;
        double dz = aimPoint.z - eyes.z;

        float desiredYaw = (float) (MathHelper.atan2(dz, dx) * (180.0f / Math.PI)) - 90.0f;
        float desiredPitch = (float) (-(MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0f / Math.PI)));
        desiredYaw = MathHelper.wrapDegrees(desiredYaw);
        desiredPitch = MathHelper.clamp(desiredPitch, -90.0f, 90.0f);

        float yawDiff = MathHelper.wrapDegrees(desiredYaw - player.yaw);
        float pitchDiff = desiredPitch - player.pitch;

        float yawLimit = 60.0f + MathHelper.nextFloat(RANDOM, 0.0f, 1.0329834f);
        float pitchLimit = MathHelper.nextFloat(RANDOM, 23.133f, 26.477f);
        yawDiff = MathHelper.clamp(yawDiff, -yawLimit, yawLimit);
        pitchDiff = MathHelper.clamp(pitchDiff, -pitchLimit, pitchLimit);

        if (Math.abs(yawDiff) < 0.01f && Math.abs(pitchDiff) > 0.01f) {
            float jitter = MathHelper.nextFloat(RANDOM, 0.1f, 0.5f) + 0.10313f;
            yawDiff += Math.copySign(jitter, pitchDiff);
        }

        if (Math.abs(pitchDiff) < 0.01f && Math.abs(yawDiff) > 0.01f) {
            float jitter = MathHelper.nextFloat(RANDOM, 0.1f, 0.5f) + 0.10313f;
            pitchDiff += Math.copySign(jitter, yawDiff);
        }

        float speedFactor = MathHelper.nextFloat(RANDOM, 0.65f, 0.75f);
        float divergence = MathHelper.nextFloat(RANDOM, 0.82f, 1.18f);

        float yawStep = yawDiff * speedFactor + MathHelper.nextFloat(RANDOM, -0.35f, 0.35f);
        float pitchStep = pitchDiff * speedFactor * divergence + MathHelper.nextFloat(RANDOM, -0.25f, 0.25f);

        float newYaw = MathHelper.wrapDegrees(player.yaw + yawStep);
        float newPitch = MathHelper.clamp(player.pitch + pitchStep, -90.0f, 90.0f);

        player.yaw = newYaw;
        player.headYaw = newYaw;
        player.bodyYaw = newYaw;
        player.pitch = newPitch;
    }

    private static void attemptAttack(ClientPlayerEntity player, LivingEntity target) {
        if (CLIENT.interactionManager == null) {
            return;
        }

        ItemStack stack = player.getMainHandStack();
        if (!isValidWeapon(stack)) {
            return;
        }

        if (player.isBlocking() || player.isUsingItem()) {
            return;
        }

        float cooldown = player.getAttackCooldownProgress(0.5f);
        if (cooldown < 0.92f) {
            return;
        }

        long now = System.currentTimeMillis();
        long interval = BASE_ATTACK_INTERVAL_MS + RANDOM.nextInt(40);
        if (now - lastAttackTimeMs < interval) {
            return;
        }

        long worldTick = player.world.getTime();
        int targetId = target.getEntityId();
        if (worldTick == lastAttackTick && targetId == lastTargetEntityId) {
            return;
        }

        CLIENT.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);

        lastAttackTimeMs = now;
        lastAttackTick = worldTick;
        lastTargetEntityId = targetId;
    }

    private static boolean isValidWeapon(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem);
    }

    private static final class AimPointController {
        private Vec3d currentPoint;
        private Vec3d targetPoint;
        private long lastRefreshTick;

        void update(LivingEntity target, long worldTick) {
            if (targetPoint == null || worldTick - lastRefreshTick >= POINT_REFRESH_INTERVAL
                    || currentPoint == null || currentPoint.squaredDistanceTo(targetPoint) < 0.01) {
                targetPoint = pickNewPoint(target, currentPoint);
                lastRefreshTick = worldTick;
            }

            if (currentPoint == null) {
                currentPoint = targetPoint;
                return;
            }

            Vec3d delta = targetPoint.subtract(currentPoint);
            double speed = 0.35 + RANDOM.nextDouble() * 0.25;
            currentPoint = currentPoint.add(delta.multiply(speed));
        }

        Vec3d getPoint(LivingEntity target) {
            if (currentPoint == null) {
                return defaultPoint(target);
            }
            return currentPoint;
        }

        void reset() {
            currentPoint = null;
            targetPoint = null;
            lastRefreshTick = 0L;
        }

        private Vec3d pickNewPoint(LivingEntity entity, Vec3d previous) {
            Box box = entity.getBoundingBox();
            Vec3d candidate = null;
            for (int attempts = 0; attempts < 10; attempts++) {
                double x = MathHelper.lerp(RANDOM.nextDouble(), box.minX, box.maxX);
                double y = MathHelper.lerp(RANDOM.nextDouble(), box.minY, box.maxY);
                double z = MathHelper.lerp(RANDOM.nextDouble(), box.minZ, box.maxZ);
                Vec3d point = new Vec3d(x, y, z);
                if (previous == null || point.squaredDistanceTo(previous) >= MIN_POINT_DISTANCE_SQ) {
                    candidate = point;
                    break;
                }
                candidate = point;
            }

            if (candidate == null) {
                candidate = defaultPoint(entity);
            }

            return candidate;
        }

        private Vec3d defaultPoint(LivingEntity entity) {
            double eyeHeight = entity.getStandingEyeHeight();
            return entity.getPos().add(0.0, eyeHeight * 0.6, 0.0);
        }
    }
}
