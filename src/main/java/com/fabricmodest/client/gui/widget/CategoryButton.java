package com.fabricmodest.client.gui.widget;

import com.fabricmodest.client.gui.ModestScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;

public class CategoryButton extends ButtonWidget {
    private final BooleanSupplier selectedSupplier;
    private final Runnable onSelect;

    public CategoryButton(int x, int y, int width, int height, Text message, BooleanSupplier selectedSupplier, Runnable onSelect) {
        super(x, y, width, height, message, button -> {});
        this.selectedSupplier = selectedSupplier;
        this.onSelect = onSelect;
    }

    @Override
    public void onPress() {
        onSelect.run();
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        boolean selected = selectedSupplier.getAsBoolean();
        int borderColor = selected ? ModestScreen.getAnimatedColor(0.25F) : 0x80FFFFFF;
        int backgroundColor = selected ? 0xD0101014 : 0x60101014;

        DrawableHelper.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, backgroundColor);
        DrawableHelper.fill(matrices, this.x, this.y, this.x + this.width, this.y + 1, borderColor);
        DrawableHelper.fill(matrices, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, borderColor);
        DrawableHelper.fill(matrices, this.x, this.y, this.x + 1, this.y + this.height, borderColor);
        DrawableHelper.fill(matrices, this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, borderColor);

        int textColor = selected ? 0xFFFFFFFF : 0xFFCACCDD;
        drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
    }
}
