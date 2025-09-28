package com.slayvisual;

import com.slayvisual.config.SlayvisualConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Queue;

public final class TriggerBot {
        private static final float MAX_ATTACK_DISTANCE = 4.5f;
        private static final Queue<SprintRestore> PENDING_SPRINT_RESTORES = new ArrayDeque<>();

        private static KeyBinding toggleKey;
        private static boolean enabled;
        private static long lastAttackTick = -1L;
        private static long lastAttackTimeMs = 0L;
        private static int lastTargetEntityId = -1;

        private TriggerBot() {
        }

        public static void init() {
                enabled = SlayvisualConfig.COMBAT.isTriggerBotEnabled();

                toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                                "key.slayvisual.triggerbot",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_R,
                                "category.slayvisual"
                ));

                setToggleKey(SlayvisualConfig.COMBAT.getTriggerBotKeyCode());

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        if (client.player == null || client.world == null) {
                                return;
                        }

                        processSprintRestores(client);

                        while (toggleKey.wasPressed()) {
                                SlayvisualConfig.COMBAT.setTriggerBotEnabled(!enabled);
                        }

                        if (enabled) {
                                handleTriggerBot(client);
                        }
                });
        }

        public static void setEnabled(boolean newState) {
                enabled = newState;
        }

        public static void setToggleKey(int keyCode) {
                if (toggleKey == null) {
                        return;
                }

                InputUtil.Key key = InputUtil.fromKeyCode(keyCode, 0);
                toggleKey.setBoundKey(key);
                KeyBinding.updateKeysByCode();
        }

        private static void handleTriggerBot(MinecraftClient client) {
                if (!(client.crosshairTarget instanceof EntityHitResult)) {
                        return;
                }

                Entity targetEntity = ((EntityHitResult) client.crosshairTarget).getEntity();
                if (!(targetEntity instanceof PlayerEntity) || !targetEntity.isAlive()) {
                        return;
                }

                ClientPlayerEntity player = client.player;
                if (player == null || targetEntity == player) {
                        return;
                }

                if (player.isSpectator() || player.isBlocking() || player.isUsingItem()) {
                        return;
                }

                if (!isValidWeapon(player.getMainHandStack())) {
                        return;
                }

                if (player.squaredDistanceTo(targetEntity) > MAX_ATTACK_DISTANCE * MAX_ATTACK_DISTANCE) {
                        return;
                }

                if (client.interactionManager == null) {
                        return;
                }

                float cooldownProgress = player.getAttackCooldownProgress(0.5f);
                if (cooldownProgress < SlayvisualConfig.COMBAT.getAttackCooldownThreshold()) {
                        return;
                }

                long now = Util.getMeasuringTimeMs();
                if (now - lastAttackTimeMs < SlayvisualConfig.COMBAT.getMinimumAttackIntervalMs()) {
                        return;
                }

                long currentTick = client.world.getTime();
                if (currentTick == lastAttackTick && targetEntity.getEntityId() == lastTargetEntityId) {
                        return;
                }

                SprintRestore sprintRestore = temporarilyResetSprint(client, player);

                client.interactionManager.attackEntity(player, targetEntity);
                player.swingHand(Hand.MAIN_HAND);

                scheduleSprintRestore(sprintRestore);

                lastAttackTick = currentTick;
                lastAttackTimeMs = now;
                lastTargetEntityId = targetEntity.getEntityId();
        }

        private static boolean isValidWeapon(ItemStack stack) {
                return !stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem);
        }

        private static SprintRestore temporarilyResetSprint(MinecraftClient client, ClientPlayerEntity player) {
                KeyBinding sprintKey = client.options.keySprint;
                boolean wasSprinting = player.isSprinting();
                boolean keyWasPressed = sprintKey != null && sprintKey.isPressed();

                if (!wasSprinting && !keyWasPressed) {
                        return SprintRestore.NONE;
                }

                if (wasSprinting) {
                        player.setSprinting(false);
                        sendSprintPacket(player, ClientCommandC2SPacket.Mode.STOP_SPRINTING);
                }

                if (keyWasPressed && sprintKey != null) {
                        sprintKey.setPressed(false);
                }

                long executeTick = player.world.getTime() + 1L;
                return new SprintRestore(executeTick, wasSprinting, keyWasPressed);
        }

        private static void scheduleSprintRestore(SprintRestore sprintRestore) {
                if (sprintRestore == SprintRestore.NONE) {
                        return;
                }

                PENDING_SPRINT_RESTORES.add(sprintRestore);
        }

        private static void processSprintRestores(MinecraftClient client) {
                if (PENDING_SPRINT_RESTORES.isEmpty() || client.world == null) {
                        return;
                }

                long currentTick = client.world.getTime();
                SprintRestore restore = PENDING_SPRINT_RESTORES.peek();
                if (restore != null && currentTick >= restore.executeTick) {
                        PENDING_SPRINT_RESTORES.poll();
                        restore.apply(client, client.player);
                }
        }

        private static void sendSprintPacket(ClientPlayerEntity player, ClientCommandC2SPacket.Mode mode) {
                if (player != null && player.networkHandler != null) {
                        player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, mode));
                }
        }

        private static final class SprintRestore {
                private static final SprintRestore NONE = new SprintRestore(-1L, false, false);

                private final long executeTick;
                private final boolean restoreSprint;
                private final boolean restoreKey;

                private SprintRestore(long executeTick, boolean restoreSprint, boolean restoreKey) {
                        this.executeTick = executeTick;
                        this.restoreSprint = restoreSprint;
                        this.restoreKey = restoreKey;
                }

                private void apply(MinecraftClient client, ClientPlayerEntity player) {
                        if (player == null) {
                                return;
                        }

                        if (restoreSprint) {
                                player.setSprinting(true);
                                sendSprintPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING);
                        }

                        if (restoreKey) {
                                KeyBinding sprintKey = client.options.keySprint;
                                if (sprintKey != null) {
                                        sprintKey.setPressed(true);
                                }
                        }
                }
        }
}
