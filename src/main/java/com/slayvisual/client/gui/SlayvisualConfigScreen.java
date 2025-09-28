package com.slayvisual.client.gui;

import com.slayvisual.client.config.SlayvisualConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class SlayvisualConfigScreen extends Screen {
    private final Screen parent;

    public SlayvisualConfigScreen(Screen parent) {
        super(new TranslatableText("screen.slayvisual.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        final int centerX = this.width / 2;
        final int startY = this.height / 4;

        this.addButton(new ButtonWidget(centerX - 100, startY, 200, 20, getTriggerBotText(), button -> {
            SlayvisualConfig.setTriggerBotEnabled(!SlayvisualConfig.isTriggerBotEnabled());
            button.setMessage(getTriggerBotText());
        }));

        this.addButton(new ButtonWidget(centerX - 100, startY + 24, 200, 20, getDelayText(), button -> {
            int currentDelay = SlayvisualConfig.getTriggerBotDelayTicks();
            int nextDelay = currentDelay >= 20 ? 0 : currentDelay + 1;
            SlayvisualConfig.setTriggerBotDelayTicks(nextDelay);
            button.setMessage(getDelayText());
        }));

        this.addButton(new ButtonWidget(centerX - 100, startY + 48, 200, 20, getHudText(), button -> {
            SlayvisualConfig.setHudEnabled(!SlayvisualConfig.isHudEnabled());
            button.setMessage(getHudText());
        }));

        this.addButton(new ButtonWidget(centerX - 100, this.height - 40, 200, 20, new TranslatableText("screen.slayvisual.done"), button -> onClose()));
    }

    private Text getTriggerBotText() {
        Text status = new TranslatableText(SlayvisualConfig.isTriggerBotEnabled() ? "options.on" : "options.off");
        return new TranslatableText("screen.slayvisual.trigger_bot", status);
    }

    private Text getDelayText() {
        return new TranslatableText("screen.slayvisual.attack_delay", SlayvisualConfig.getTriggerBotDelayTicks());
    }

    private Text getHudText() {
        Text status = new TranslatableText(SlayvisualConfig.isHudEnabled() ? "options.on" : "options.off");
        return new TranslatableText("screen.slayvisual.hud", status);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.client.setScreen(this.parent);
    }
}
