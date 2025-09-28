package com.fabricmodest.client.gui.widget;

import com.fabricmodest.client.gui.ModestScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FeatureToggleButton extends ButtonWidget {
    private final Supplier<Boolean> valueSupplier;
    private final Consumer<Boolean> valueConsumer;
    private final Text baseLabel;

    public FeatureToggleButton(int x, int y, int width, int height, Text baseLabel, Supplier<Boolean> valueSupplier, Consumer<Boolean> valueConsumer) {
        super(x, y, width, height, baseLabel, button -> {});
        this.baseLabel = baseLabel;
        this.valueSupplier = valueSupplier;
        this.valueConsumer = valueConsumer;
    }

    @Override
    public void onPress() {
        valueConsumer.accept(!valueSupplier.get());
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        boolean enabled = valueSupplier.get();
        int leftColor = ModestScreen.getAnimatedColor(0.0F);
        int rightColor = ModestScreen.getAnimatedColor(0.35F);

        DrawableHelper.fillGradient(matrices, this.x, this.y, this.x + this.width, this.y + this.height, leftColor, rightColor);

        int overlayColor = enabled ? 0x30FFFFFF : 0x40101010;
        DrawableHelper.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, overlayColor);

        MutableText display = createDisplayText(enabled);
        int textColor = enabled ? 0xFF090913 : 0xFF1A1A27;
        drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, display, this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);

        int indicatorColor = enabled ? 0xFF36FF8E : 0xFFFF7AAE;
        int indicatorSize = 8;
        int indicatorX = this.x + this.width - indicatorSize - 10;
        int indicatorY = this.y + (this.height - indicatorSize) / 2;
        DrawableHelper.fill(matrices, indicatorX, indicatorY, indicatorX + indicatorSize, indicatorY + indicatorSize, 0xA015151C);
        DrawableHelper.fill(matrices, indicatorX + 1, indicatorY + 1, indicatorX + indicatorSize - 1, indicatorY + indicatorSize - 1, indicatorColor);
    }

    private MutableText createDisplayText(boolean enabled) {
        MutableText text;
        if (baseLabel instanceof MutableText) {
            text = ((MutableText) baseLabel).shallowCopy();
        } else {
            text = new LiteralText(baseLabel.getString());
        }
        text.append(new LiteralText(enabled ? "  ON" : "  OFF"));
        return text;
    }
}
