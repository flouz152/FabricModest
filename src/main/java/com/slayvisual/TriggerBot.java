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
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class TriggerBot {
        private static final float MAX_ATTACK_DISTANCE = 4.5f;

        private static KeyBinding toggleKey;
        private static boolean enabled;

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

                if (player.isOnGround()) {
                        player.jump();
                        return;
                }

                if (!isCriticalState(player)) {
                        return;
                }

                resetSprint(client, player);

                client.interactionManager.attackEntity(player, targetEntity);
                player.swingHand(Hand.MAIN_HAND);
        }

        private static void resetSprint(MinecraftClient client, ClientPlayerEntity player) {
                if (player.isSprinting()) {
                        player.setSprinting(false);
                }

                KeyBinding sprintKey = client.options.keySprint;
                if (sprintKey != null) {
                        sprintKey.setPressed(false);
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
}
