package com.slayvisual.client.hud;

import com.slayvisual.client.SlayvisualClient;
import com.slayvisual.client.config.SlayvisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class SlayvisualHudRenderer {
    private SlayvisualHudRenderer() {
    }

    public static void render(MatrixStack matrices) {
        if (!SlayvisualConfig.isHudEnabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.textRenderer == null) {
            return;
        }

        boolean enabled = SlayvisualConfig.isTriggerBotEnabled();
        int color = enabled ? 0x55FF55 : 0xFF5555;
        Text statusText = new TranslatableText(
            "hud.slayvisual.trigger_bot",
            new TranslatableText(enabled ? "options.on" : "options.off")
        );

        client.textRenderer.drawWithShadow(matrices, statusText, 6, 6, color);
        Text hintText = new TranslatableText("hud.slayvisual.open_gui", SlayvisualClient.getOpenConfigKey().getBoundKeyLocalizedText());
        client.textRenderer.drawWithShadow(matrices, hintText, 6, 18, 0xFFFFFF);
    }
}
