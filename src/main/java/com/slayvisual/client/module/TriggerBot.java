package com.slayvisual.client.module;

import com.slayvisual.client.config.SlayvisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public final class TriggerBot {
    private static int ticksSinceLastAttack = 0;

    private TriggerBot() {
    }

    public static void onEndTick(MinecraftClient client) {
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;

        if (!SlayvisualConfig.isTriggerBotEnabled() || world == null || player == null) {
            return;
        }

        if (client.currentScreen != null || player.isSpectator()) {
            return;
        }

        if (ticksSinceLastAttack < Integer.MAX_VALUE) {
            ticksSinceLastAttack++;
        }

        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof EntityHitResult)) {
            return;
        }

        Entity entity = ((EntityHitResult) hitResult).getEntity();
        if (!(entity instanceof LivingEntity) || !entity.isAlive() || entity == player) {
            return;
        }

        if (player.getAttackCooldownProgress(0.5F) < 1.0F) {
            return;
        }

        int delay = SlayvisualConfig.getTriggerBotDelayTicks();
        if (ticksSinceLastAttack < delay) {
            return;
        }

        if (client.interactionManager != null) {
            client.interactionManager.attackEntity(player, entity);
            player.swingHand(Hand.MAIN_HAND);
            ticksSinceLastAttack = 0;
        }
    }
}
