package com.slayvisual.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayDeque;
import java.util.Deque;

public final class SlayvisualNotifications implements HudRenderCallback {
        private static final long DISPLAY_TIME_MS = 1800L;
        private static final Deque<Notification> NOTIFICATIONS = new ArrayDeque<>();

        private SlayvisualNotifications() {
        }

        public static void init() {
                HudRenderCallback.EVENT.register(new SlayvisualNotifications());
        }

        public static void notifyToggle(String label, boolean enabled) {
                notify(label + (enabled ? " enabled" : " disabled"));
        }

        public static void notify(String message) {
                NOTIFICATIONS.addFirst(new Notification(message, System.currentTimeMillis()));
        }

        @Override
        public void onHudRender(MatrixStack matrices, float tickDelta) {
                if (NOTIFICATIONS.isEmpty()) {
                        return;
                }

                MinecraftClient client = MinecraftClient.getInstance();
                TextRenderer textRenderer = client.textRenderer;
                int screenWidth = client.getWindow().getScaledWidth();

                int index = 0;
                long now = System.currentTimeMillis();

                Deque<Notification> toRemove = new ArrayDeque<>();
                for (Notification notification : NOTIFICATIONS) {
                        long elapsed = now - notification.createdAt;
                        if (elapsed > DISPLAY_TIME_MS) {
                                toRemove.add(notification);
                                continue;
                        }

                        float alpha = 1.0f - (float) elapsed / DISPLAY_TIME_MS;
                        int x = screenWidth - 10 - 180;
                        int y = 20 + index * 24;
                        int backgroundAlpha = (int) (180 * alpha);
                        int color = (backgroundAlpha << 24) | 0x111122;

                        RenderSystem.enableBlend();
                        DrawableHelper.fill(matrices, x, y, x + 180, y + 20, color);
                        int textAlpha = (int) (255 * alpha) << 24;
                        textRenderer.drawWithShadow(matrices, notification.message, x + 8, y + 6, 0xFFFFFF | textAlpha);
                        RenderSystem.disableBlend();
                        index++;
                }

                NOTIFICATIONS.removeAll(toRemove);
        }

        private static final class Notification {
                        private final String message;
                        private final long createdAt;

                        private Notification(String message, long createdAt) {
                                this.message = message;
                                this.createdAt = createdAt;
                        }
                }
}
