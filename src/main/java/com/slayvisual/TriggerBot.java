package com.slayvisual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class TriggerBot {
        private static final float MAX_ATTACK_DISTANCE = 4.5f;

        private static KeyBinding toggleKey;
        private static boolean enabled;
        private static long lastAttackTick = -1L;
        private static int lastTargetEntityId = -1;

        private TriggerBot() {
        }

        public static void init() {
                toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                                "key.slayvisual.triggerbot",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_R,
                                "category.slayvisual"
                ));

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        if (client.player == null || client.world == null) {
                                return;
                        }

                        while (toggleKey.wasPressed()) {
                                enabled = !enabled;
                                Slayvisual.LOGGER.info("Trigger Bot {}", enabled ? "enabled" : "disabled");
                        }

                        if (enabled) {
                                handleTriggerBot(client);
                        }
                });
        }

        private static void handleTriggerBot(MinecraftClient client) {
                if (!(client.crosshairTarget instanceof EntityHitResult)) {
                        return;
                }

                Entity targetEntity = ((EntityHitResult) client.crosshairTarget).getEntity();
                if (!(targetEntity instanceof PlayerEntity)) {
                        return;
                }

                ClientPlayerEntity player = client.player;
                if (player == null || targetEntity == player) {
                        return;
                }

                if (player.squaredDistanceTo(targetEntity) > MAX_ATTACK_DISTANCE * MAX_ATTACK_DISTANCE) {
                        return;
                }

                if (client.interactionManager == null) {
                        return;
                }

                if (player.getAttackCooldownProgress(0.0f) < 1.0f) {
                        return;
                }

                if (!isCriticalState(player)) {
                        return;
                }

                long currentTick = client.world.getTime();
                if (currentTick == lastAttackTick && targetEntity.getEntityId() == lastTargetEntityId) {
                        return;
                }

                SprintState sprintState = temporarilyResetSprint(client, player);

                client.interactionManager.attackEntity(player, targetEntity);
                player.swingHand(Hand.MAIN_HAND);

                restoreSprint(client, player, sprintState);

                lastAttackTick = currentTick;
                lastTargetEntityId = targetEntity.getEntityId();
        }

        private static SprintState temporarilyResetSprint(MinecraftClient client, ClientPlayerEntity player) {
                KeyBinding sprintKey = client.options.keySprint;
                boolean wasSprinting = player.isSprinting();
                boolean keyWasPressed = sprintKey != null && sprintKey.isPressed();

                if (!wasSprinting && !keyWasPressed) {
                        return SprintState.NONE;
                }

                if (wasSprinting) {
                        player.setSprinting(false);
                        sendSprintPacket(player, ClientCommandC2SPacket.Mode.STOP_SPRINTING);
                }

                if (keyWasPressed) {
                        sprintKey.setPressed(false);
                }

                return new SprintState(wasSprinting, keyWasPressed);
        }

        private static void restoreSprint(MinecraftClient client, ClientPlayerEntity player, SprintState sprintState) {
                if (!sprintState.shouldRestoreSprint && !sprintState.shouldRestoreKey) {
                        return;
                }

                if (sprintState.shouldRestoreSprint) {
                        player.setSprinting(true);
                        sendSprintPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING);
                }

                if (sprintState.shouldRestoreKey) {
                        KeyBinding sprintKey = client.options.keySprint;
                        if (sprintKey != null) {
                                sprintKey.setPressed(true);
                        }
                }
        }

        private static void sendSprintPacket(ClientPlayerEntity player, ClientCommandC2SPacket.Mode mode) {
                if (player.networkHandler != null) {
                        player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, mode));
                }
        }

        private static boolean isCriticalState(ClientPlayerEntity player) {
                if (player.isOnGround()) {
                        return false;
                }

                if (player.isClimbing() || player.isTouchingWater() || player.hasVehicle()) {
                        return false;
                }

                if (player.hasStatusEffect(StatusEffects.BLINDNESS)) {
                        return false;
                }

                if (player.fallDistance <= 0.0f) {
                        return false;
                }

                return player.getVelocity().y < 0.0d;
        }

        private static final class SprintState {
                private static final SprintState NONE = new SprintState(false, false);

                private final boolean shouldRestoreSprint;
                private final boolean shouldRestoreKey;

                private SprintState(boolean shouldRestoreSprint, boolean shouldRestoreKey) {
                        this.shouldRestoreSprint = shouldRestoreSprint;
                        this.shouldRestoreKey = shouldRestoreKey;
                }
        }
}
