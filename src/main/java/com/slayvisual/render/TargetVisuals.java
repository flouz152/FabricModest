package com.slayvisual.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.slayvisual.config.SlayvisualConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class TargetVisuals {
        private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

        private TargetVisuals() {
        }

        public static void init() {
                HudRenderCallback.EVENT.register(TargetVisuals::renderHud);
                WorldRenderEvents.AFTER_ENTITIES.register(TargetVisuals::renderEsp);
        }

        private static void renderHud(MatrixStack matrices, float tickDelta) {
                if (!SlayvisualConfig.VISUAL.isTargetHudEnabled()) {
                        return;
                }

                LivingEntity living = getTargetedEntity();
                if (living == null) {
                        return;
                }

                TextRenderer textRenderer = CLIENT.textRenderer;
                int width = CLIENT.getWindow().getScaledWidth();

                int hudWidth = 150;
                int hudHeight = 46;
                int x = width / 2 - hudWidth / 2;
                int y = CLIENT.getWindow().getScaledHeight() - 70;

                SlayvisualConfig.HitColor hitColor = SlayvisualConfig.VISUAL.getHitColor();
                int accent = ((hitColor.getAlpha() & 0xFF) << 24) | ((hitColor.getRed() & 0xFF) << 16)
                        | ((hitColor.getGreen() & 0xFF) << 8) | (hitColor.getBlue() & 0xFF);

                RenderSystem.enableBlend();
                fillRect(matrices, x, y, hudWidth, hudHeight, 0xAA101010);
                fillRect(matrices, x, y, 6, hudHeight, accent);

                String name = living.getDisplayName().getString();
                float health = Math.max(0.0f, living.getHealth());
                float maxHealth = Math.max(health, living.getMaxHealth());
                float healthPercent = maxHealth == 0.0f ? 0.0f : health / maxHealth;

                textRenderer.drawWithShadow(matrices, new LiteralText(name), x + 12, y + 6, 0xFFFFFF);
                textRenderer.drawWithShadow(matrices, new LiteralText(String.format("HP %.1f", health)), x + 12, y + 20, 0xAAAAD1);

                int barWidth = hudWidth - 24;
                fillRect(matrices, x + 12, y + 32, barWidth, 8, 0x66111111);
                fillRect(matrices, x + 12, y + 32, (int) (barWidth * healthPercent), 8, accent);
                RenderSystem.disableBlend();
        }

        private static void renderEsp(WorldRenderContext context) {
                if (!SlayvisualConfig.VISUAL.isTargetEspEnabled()) {
                        return;
                }

                LivingEntity living = getTargetedEntity();
                if (living == null) {
                        return;
                }

                MatrixStack matrices = context.matrixStack();
                Vec3d cameraPos = context.camera().getPos();
                Box box = living.getBoundingBox().offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

                VertexConsumerProvider consumers = context.consumers();
                if (consumers == null) {
                        return;
                }

                VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());
                SlayvisualConfig.HitColor hitColor = SlayvisualConfig.VISUAL.getHitColor();

                float r = hitColor.getRedFloat();
                float g = hitColor.getGreenFloat();
                float b = hitColor.getBlueFloat();
                float a = Math.max(0.1f, hitColor.getAlphaFloat());

                matrices.push();
                WorldRenderer.drawBox(matrices, buffer, box, r, g, b, a);
                matrices.pop();
        }

        private static LivingEntity getTargetedEntity() {
                if (CLIENT == null || CLIENT.player == null) {
                        return null;
                }

                HitResult hitResult = CLIENT.crosshairTarget;
                if (!(hitResult instanceof EntityHitResult)) {
                        return null;
                }

                Entity entity = ((EntityHitResult) hitResult).getEntity();
                if (!(entity instanceof LivingEntity)) {
                        return null;
                }

                return (LivingEntity) entity;
        }

        private static void fillRect(MatrixStack matrices, int x, int y, int width, int height, int color) {
                if (color == 0) {
                        return;
                }
                net.minecraft.client.gui.DrawableHelper.fill(matrices, x, y, x + width, y + height, color);
        }
}
