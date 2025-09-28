package com.fabricmodest.client.gui;

import com.fabricmodest.client.ModestClient;
import com.fabricmodest.client.gui.widget.CategoryButton;
import com.fabricmodest.client.gui.widget.FeatureToggleButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ModestScreen extends Screen {
    private static final int PANEL_WIDTH = 340;
    private static final int PANEL_HEIGHT = 220;
    private static final int CATEGORY_WIDTH = 110;

    private int panelX;
    private int panelY;

    private Category selectedCategory = Category.COMBAT;
    private final List<ButtonWidget> featureButtons = new ArrayList<>();

    public ModestScreen() {
        super(new TranslatableText("screen.fabricmodest.title"));
    }

    @Override
    protected void init() {
        super.init();
        this.panelX = (this.width - PANEL_WIDTH) / 2;
        this.panelY = (this.height - PANEL_HEIGHT) / 2;

        this.buttons.clear();
        this.children.clear();

        int categoryStartY = panelY + 60;
        int categorySpacing = 32;
        int index = 0;
        for (Category category : EnumSet.allOf(Category.class)) {
            Text name = new TranslatableText(category.translationKey);
            int y = categoryStartY + index * categorySpacing;
            this.addButton(new CategoryButton(panelX + 18, y, CATEGORY_WIDTH - 30, 24, name, () -> selectedCategory == category, () -> selectCategory(category)));
            index++;
        }

        refreshFeatureButtons();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        renderGlassPanel(matrices);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Text title = this.getTitle();
        textRenderer.draw(matrices, title, panelX + 24, panelY + 24, 0xFFFFFFFF);

        Text searchHint = new LiteralText("visual/utility client preview");
        textRenderer.draw(matrices, searchHint, panelX + 24, panelY + PANEL_HEIGHT - 28, 0x80FFFFFF);

        super.render(matrices, mouseX, mouseY, delta);

        if (selectedCategory == Category.VISUAL && featureButtons.isEmpty()) {
            drawCenteredText(matrices, textRenderer, new LiteralText("More visuals coming soon"), panelX + PANEL_WIDTH - 110, panelY + PANEL_HEIGHT / 2, 0x80FFFFFF);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
    }

    private void renderGlassPanel(MatrixStack matrices) {
        int backgroundColor = 0xB0101014;
        fill(matrices, panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, backgroundColor);

        int glowPadding = 4;
        int glowTop = panelY - glowPadding;
        int glowBottom = panelY + PANEL_HEIGHT + glowPadding;
        int glowLeft = panelX - glowPadding;
        int glowRight = panelX + PANEL_WIDTH + glowPadding;

        int outerColor = getAnimatedColor(0.0F);
        int innerColor = getAnimatedColor(0.2F);
        fillGradient(matrices, glowLeft, glowTop, glowRight, panelY, outerColor, innerColor);
        fillGradient(matrices, glowLeft, panelY, glowRight, glowBottom, innerColor, outerColor);

        int dividerX = panelX + CATEGORY_WIDTH;
        fill(matrices, dividerX, panelY + 40, dividerX + 1, panelY + PANEL_HEIGHT - 20, 0x50FFFFFF);
    }

    private void selectCategory(Category category) {
        if (this.selectedCategory != category) {
            this.selectedCategory = category;
            refreshFeatureButtons();
        }
    }

    private void refreshFeatureButtons() {
        this.buttons.removeAll(featureButtons);
        this.children.removeAll(featureButtons);
        this.featureButtons.clear();

        int contentX = panelX + CATEGORY_WIDTH + 20;
        int contentY = panelY + 60;
        int featureWidth = PANEL_WIDTH - CATEGORY_WIDTH - 40;
        int featureHeight = 30;

        if (selectedCategory == Category.COMBAT) {
            FeatureToggleButton triggerBotButton = new FeatureToggleButton(contentX, contentY, featureWidth, featureHeight,
                    new TranslatableText("screen.fabricmodest.feature.trigger_bot"),
                    ModestClient.getTriggerBot()::isEnabled,
                    ModestClient.getTriggerBot()::setEnabled);
            this.featureButtons.add(this.addButton(triggerBotButton));
        }
    }

    public static int getAnimatedColor(float offset) {
        long time = Util.getMeasuringTimeMs();
        double progress = ((time % 6000L) / 6000.0D + offset) % 1.0D;
        float blend = (float) (0.5D + 0.5D * Math.sin(progress * Math.PI * 2));
        return lerpColor(0xFFFFFFFF, 0xFFFF7ACC, blend);
    }

    private static int lerpColor(int colorA, int colorB, float t) {
        int aA = (colorA >>> 24) & 0xFF;
        int rA = (colorA >>> 16) & 0xFF;
        int gA = (colorA >>> 8) & 0xFF;
        int bA = colorA & 0xFF;

        int aB = (colorB >>> 24) & 0xFF;
        int rB = (colorB >>> 16) & 0xFF;
        int gB = (colorB >>> 8) & 0xFF;
        int bB = colorB & 0xFF;

        int a = MathHelper.clamp((int) MathHelper.lerp(t, aA, aB), 0, 255);
        int r = MathHelper.clamp((int) MathHelper.lerp(t, rA, rB), 0, 255);
        int g = MathHelper.clamp((int) MathHelper.lerp(t, gA, gB), 0, 255);
        int b = MathHelper.clamp((int) MathHelper.lerp(t, bA, bB), 0, 255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private enum Category {
        COMBAT("screen.fabricmodest.category.combat"),
        VISUAL("screen.fabricmodest.category.visual");

        private final String translationKey;

        Category(String translationKey) {
            this.translationKey = translationKey;
        }
    }
}
