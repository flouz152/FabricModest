package com.slayvisual;

import com.slayvisual.config.SlayvisualConfig;
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
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public final class KillAura {
        private static KeyBinding toggleKey;
        private static boolean enabled;
        private static LivingEntity currentTarget;
        private static long lastAttackTimeMs;
        private static long lastAttackTick = -1L;
        private static int lastTargetEntityId = -1;
        private static long lastSwitchTimeMs;
        private static float serverYaw = Float.NaN;
        private static float serverPitch = Float.NaN;

        private KillAura() {
        }

        public static void init() {
                enabled = SlayvisualConfig.COMBAT.isKillAuraEnabled();

                toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                                "key.slayvisual.killaura",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_R,
                                "category.slayvisual"
                ));

                setToggleKey(SlayvisualConfig.COMBAT.getKillAuraKeyCode());

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        if (client.player == null || client.world == null) {
                                currentTarget = null;
                                return;
                        }

                        processToggle();

                        if (enabled) {
                                handleKillAura(client);
                        } else {
                                currentTarget = null;
                        }
                });
        }

        public static void setEnabled(boolean newState) {
                enabled = newState;
                if (!newState) {
                        currentTarget = null;
                        lastTargetEntityId = -1;
                        lastAttackTick = -1L;
                        lastAttackTimeMs = 0L;
                        serverYaw = Float.NaN;
                        serverPitch = Float.NaN;
                }
        }

        public static void setToggleKey(int keyCode) {
                if (toggleKey == null) {
                        return;
                }

                InputUtil.Key key = InputUtil.fromKeyCode(keyCode, 0);
                toggleKey.setBoundKey(key);
                KeyBinding.updateKeysByCode();
        }

        private static void processToggle() {
                if (toggleKey == null) {
                        return;
                }

                while (toggleKey.wasPressed()) {
                        SlayvisualConfig.COMBAT.setKillAuraEnabled(!enabled);
                }
        }

        private static void handleKillAura(MinecraftClient client) {
                ClientPlayerEntity player = client.player;
                if (player == null || !player.isAlive() || player.isSpectator()) {
                        currentTarget = null;
                        return;
                }

                LivingEntity target = selectTarget(player);
                currentTarget = target;

                if (target == null) {
                        serverYaw = Float.NaN;
                        serverPitch = Float.NaN;
                        return;
                }

                rotateTowards(player, target);
                attemptAttack(client, player, target);
        }

        private static LivingEntity selectTarget(ClientPlayerEntity player) {
                float effectiveRange = Math.min(3.0f, SlayvisualConfig.COMBAT.getKillAuraRange());
                float fov = SlayvisualConfig.COMBAT.getKillAuraFov();
                long switchDelay = SlayvisualConfig.COMBAT.getKillAuraSwitchDelayMs();

                if (currentTarget != null && isValidTarget(player, currentTarget, effectiveRange, fov)) {
                        return currentTarget;
                }

                Box searchBox = player.getBoundingBox().expand(effectiveRange);
                List<LivingEntity> nearby = player.world.getEntitiesByClass(LivingEntity.class, searchBox,
                                entity -> entity != player && entity.isAlive());

                nearby.sort(Comparator.comparingDouble(player::squaredDistanceTo));

                long now = Util.getMeasuringTimeMs();
                LivingEntity candidate = null;
                for (LivingEntity entity : nearby) {
                        if (!isValidTarget(player, entity, effectiveRange, fov)) {
                                continue;
                        }
                        candidate = entity;
                        break;
                }

                if (candidate == null) {
                        return null;
                }

                if (currentTarget != null && candidate != currentTarget && now - lastSwitchTimeMs < switchDelay) {
                        return currentTarget;
                }

                if (candidate != currentTarget) {
                        lastSwitchTimeMs = now;
                }

                return candidate;
        }

        private static boolean isValidTarget(ClientPlayerEntity player, LivingEntity entity, float range, float fov) {
                        if (entity == null || !entity.isAlive()) {
                                return false;
                        }

                        if (entity == player) {
                                return false;
                        }

                        if (player.squaredDistanceTo(entity) > range * range) {
                                return false;
                        }

                        if (!player.canSee(entity)) {
                                return false;
                        }

                        if (entity.isInvisibleTo(player)) {
                                return false;
                        }

                        if (entity instanceof PlayerEntity) {
                                PlayerEntity other = (PlayerEntity) entity;
                                if (other.isSpectator() || other.abilities.creativeMode) {
                                        return false;
                                }
                                if (player.isTeammate(other)) {
                                        return false;
                                }
                        }

                        if (!isWithinFov(player, entity, fov)) {
                                return false;
                        }

                        return true;
        }

        private static boolean isWithinFov(ClientPlayerEntity player, LivingEntity entity, float fov) {
                if (fov >= 180.0f) {
                        return true;
                }

                Vec3d eyes = player.getCameraPosVec(1.0f);
                Vec3d look = player.getRotationVec(1.0f).normalize();
                Vec3d toTarget = getAimPoint(entity).subtract(eyes).normalize();

                double dot = look.dotProduct(toTarget);
                dot = MathHelper.clamp(dot, -1.0, 1.0);
                double angle = Math.toDegrees(Math.acos(dot));
                return angle <= fov * 0.5f;
        }

        private static void rotateTowards(ClientPlayerEntity player, LivingEntity target) {
                Vec3d eyes = player.getCameraPosVec(1.0f);
                Vec3d aimPoint = getAimPoint(target);
                double dx = aimPoint.x - eyes.x;
                double dy = aimPoint.y - eyes.y;
                double dz = aimPoint.z - eyes.z;

                float desiredYaw = (float) (MathHelper.atan2(dz, dx) * (180.0f / Math.PI)) - 90.0f;
                float desiredPitch = (float) (-(MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0f / Math.PI)));
                desiredYaw = MathHelper.wrapDegrees(desiredYaw);
                desiredPitch = MathHelper.clamp(desiredPitch, -90.0f, 90.0f);

                SlayvisualConfig.KillAuraRotationMode mode = SlayvisualConfig.COMBAT.getKillAuraRotationMode();
                if (Float.isNaN(serverYaw) || Float.isNaN(serverPitch)) {
                        serverYaw = player.yaw;
                        serverPitch = player.pitch;
                }
                if (mode == SlayvisualConfig.KillAuraRotationMode.SNAP) {
                        applyRotation(player, desiredYaw, desiredPitch);
                        return;
                }

                float speed = SlayvisualConfig.COMBAT.getKillAuraRotationSpeed();
                float yawDiff = MathHelper.wrapDegrees(desiredYaw - serverYaw);
                float pitchDiff = desiredPitch - serverPitch;
                float yawStep = MathHelper.clamp(yawDiff, -speed, speed);
                float pitchStep = MathHelper.clamp(pitchDiff, -speed, speed);

                float newYaw = MathHelper.wrapDegrees(serverYaw + yawStep);
                float newPitch = MathHelper.clamp(serverPitch + pitchStep, -90.0f, 90.0f);
                applyRotation(player, newYaw, newPitch);
        }

        private static void applyRotation(ClientPlayerEntity player, float yaw, float pitch) {
                serverYaw = yaw;
                serverPitch = pitch;

                if (player.networkHandler != null) {
                        player.networkHandler
                                        .sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, player.isOnGround()));
                }
        }

        private static Vec3d getAimPoint(LivingEntity entity) {
                double yOffset = Math.max(0.1, entity.getStandingEyeHeight() * 0.6);
                return entity.getPos().add(0.0, yOffset, 0.0);
        }

        private static void attemptAttack(MinecraftClient client, ClientPlayerEntity player, LivingEntity target) {
                if (client.interactionManager == null) {
                        return;
                }

                if (!isValidWeapon(player.getMainHandStack())) {
                        return;
                }

                if (player.isBlocking() || player.isUsingItem()) {
                        return;
                }

                float cooldownThreshold = SlayvisualConfig.COMBAT.getKillAuraCooldownThreshold();
                if (player.getAttackCooldownProgress(0.5f) < cooldownThreshold) {
                        return;
                }

                if (player.squaredDistanceTo(target) > 9.0) {
                        return;
                }

                long now = Util.getMeasuringTimeMs();
                long interval = SlayvisualConfig.COMBAT.getKillAuraAttackIntervalMs();
                if (now - lastAttackTimeMs < interval) {
                        return;
                }

                long worldTick = player.world.getTime();
                int targetId = target.getEntityId();
                if (worldTick == lastAttackTick && targetId == lastTargetEntityId) {
                        return;
                }

                client.interactionManager.attackEntity(player, target);
                player.swingHand(Hand.MAIN_HAND);

                lastAttackTimeMs = now;
                lastAttackTick = worldTick;
                lastTargetEntityId = targetId;
        }

        private static boolean isValidWeapon(ItemStack stack) {
                return !stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem);
        }
}
