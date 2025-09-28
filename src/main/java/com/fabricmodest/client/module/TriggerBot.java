package com.fabricmodest.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TriggerBot {
    private boolean enabled;
    private int jumpTicks;
    private int attackCooldownTicks;
    private int settleTicks;

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (!enabled) {
            jumpTicks = 0;
            attackCooldownTicks = 0;
            settleTicks = 0;
        }
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void tick(MinecraftClient client) {
        if (!enabled || client == null) {
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null || client.world == null) {
            return;
        }

        if (attackCooldownTicks > 0) {
            attackCooldownTicks--;
        }
        if (jumpTicks > 0) {
            jumpTicks--;
        }
        if (settleTicks > 0) {
            settleTicks--;
        }

        HitResult crosshair = client.crosshairTarget;
        if (!(crosshair instanceof EntityHitResult)) {
            settleTicks = 0;
            return;
        }

        EntityHitResult entityHitResult = (EntityHitResult) crosshair;
        if (!(entityHitResult.getEntity() instanceof LivingEntity)) {
            settleTicks = 0;
            return;
        }

        LivingEntity target = (LivingEntity) entityHitResult.getEntity();
        if (!target.isAlive() || target.isRemoved()) {
            settleTicks = 0;
            return;
        }

        double distanceSquared = player.squaredDistanceTo(target);
        if (distanceSquared > 25.0D) {
            settleTicks = 0;
            return;
        }

        if (player.isSprinting()) {
            player.setSprinting(false);
        }

        if (player.isTouchingWater() || player.isClimbing() || player.isInLava()) {
            return;
        }

        if (player.getAttackCooldownProgress(0.5F) < 1.0F) {
            return;
        }

        if (player.isOnGround() && player.fallDistance <= 0.0F && jumpTicks == 0) {
            player.jump();
            jumpTicks = 6;
            settleTicks = 4;
            return;
        }

        if (player.fallDistance <= 0.1F && player.isOnGround()) {
            return;
        }

        if (settleTicks > 0) {
            return;
        }

        if (player.fallDistance > 0.1F && player.getVelocity().y < 0.0D && attackCooldownTicks == 0) {
            client.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
            attackCooldownTicks = 6;
        }
    }
}
