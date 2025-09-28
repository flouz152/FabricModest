package com.slayvisual.ui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.slayvisual.TriggerBot;
import com.slayvisual.config.SlayvisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SlayvisualScreen extends Screen {
        private static final int PANEL_WIDTH = 320;
        private static final int PANEL_HEIGHT = 220;

        private Category category = Category.VISUALS;
        private final List<AbstractButtonWidget> dynamicWidgets = new ArrayList<>();
        private boolean capturingTriggerKey;

        public SlayvisualScreen() {
                super(new LiteralText("Celestial"));
        }

        @Override
        protected void init() {
                super.init();
                this.dynamicWidgets.clear();

                int left = (this.width - PANEL_WIDTH) / 2;
                int top = (this.height - PANEL_HEIGHT) / 2;

                this.children.clear();
                this.buttons.clear();
                this.selectables.clear();
                this.addButton(new TabButton(left + 14, top + 16, Category.VISUALS));
                this.addButton(new TabButton(left + 114, top + 16, Category.COMBAT));

                rebuildCategory(left, top + 60);
        }

        @Override
        public void removed() {
                this.capturingTriggerKey = false;
                super.removed();
        }

        @Override
        public void tick() {
                super.tick();
                for (AbstractButtonWidget widget : this.dynamicWidgets) {
                        if (widget instanceof AnimatedWidgetHolder) {
                                ((AnimatedWidgetHolder) widget).tickAnimation();
                        }
                }
        }

        private void rebuildCategory(int left, int contentTop) {
                for (AbstractButtonWidget widget : this.dynamicWidgets) {
                        this.children.remove(widget);
                        this.buttons.remove(widget);
                        this.selectables.remove(widget);
                }
                this.dynamicWidgets.clear();

                if (this.category == Category.VISUALS) {
                        buildVisuals(left, contentTop);
                } else {
                        buildCombat(left, contentTop);
                }
        }

        private void buildVisuals(int left, int top) {
                int y = top;

                addDynamic(new ToggleButton(left + 20, y, 280, "Target HUD", SlayvisualConfig.VISUAL::isTargetHudEnabled, SlayvisualConfig.VISUAL::setTargetHudEnabled));
                y += 26;
                addDynamic(new ToggleButton(left + 20, y, 280, "Target ESP", SlayvisualConfig.VISUAL::isTargetEspEnabled, SlayvisualConfig.VISUAL::setTargetEspEnabled));
                y += 26;
                addDynamic(new ToggleButton(left + 20, y, 280, "View Model", SlayvisualConfig.VISUAL::isViewModelEnabled, SlayvisualConfig.VISUAL::setViewModelEnabled));
                y += 30;

                addDynamic(new ValueSlider(left + 20, y, 280, "View X", -0.8f, 0.8f,
                                () -> SlayvisualConfig.VISUAL.getViewModelOffsetX(),
                                SlayvisualConfig.VISUAL::setViewModelOffsetX));
                y += 24;
                addDynamic(new ValueSlider(left + 20, y, 280, "View Y", -0.8f, 0.8f,
                                () -> SlayvisualConfig.VISUAL.getViewModelOffsetY(),
                                SlayvisualConfig.VISUAL::setViewModelOffsetY));
                y += 24;
                addDynamic(new ValueSlider(left + 20, y, 280, "View Z", -0.8f, 0.8f,
                                () -> SlayvisualConfig.VISUAL.getViewModelOffsetZ(),
                                SlayvisualConfig.VISUAL::setViewModelOffsetZ));
                y += 24;
                addDynamic(new ValueSlider(left + 20, y, 280, "View Scale", 0.6f, 1.6f,
                                () -> SlayvisualConfig.VISUAL.getViewModelScale(),
                                SlayvisualConfig.VISUAL::setViewModelScale));
                y += 28;

                addDynamic(new EnumCycleButton<>(left + 20, y, 280, "Sword Animation",
                                SlayvisualConfig.SwordAnimation.values(),
                                SlayvisualConfig.VISUAL::getSwordAnimation,
                                SlayvisualConfig.VISUAL::setSwordAnimation,
                                SlayvisualConfig.SwordAnimation::getDisplayName));
                y += 28;

                addDynamic(new ColorSlider(left + 20, y, "Hit Color R", () -> SlayvisualConfig.VISUAL.getHitColor().getRed(), value -> SlayvisualConfig.VISUAL.getHitColor().setRed(value)));
                y += 24;
                addDynamic(new ColorSlider(left + 20, y, "Hit Color G", () -> SlayvisualConfig.VISUAL.getHitColor().getGreen(), value -> SlayvisualConfig.VISUAL.getHitColor().setGreen(value)));
                y += 24;
                addDynamic(new ColorSlider(left + 20, y, "Hit Color B", () -> SlayvisualConfig.VISUAL.getHitColor().getBlue(), value -> SlayvisualConfig.VISUAL.getHitColor().setBlue(value)));
                y += 24;
                addDynamic(new ColorSlider(left + 20, y, "Hit Alpha", () -> SlayvisualConfig.VISUAL.getHitColor().getAlpha(), value -> SlayvisualConfig.VISUAL.getHitColor().setAlpha(value)));
        }

        private void buildCombat(int left, int top) {
                int y = top;
                addDynamic(new ToggleButton(left + 20, y, 280, "Trigger Bot", SlayvisualConfig.COMBAT::isTriggerBotEnabled, SlayvisualConfig.COMBAT::setTriggerBotEnabled));
                y += 30;

                addDynamic(new ValueSlider(left + 20, y, 280, "Cooldown", 0.6f, 1.0f,
                                SlayvisualConfig.COMBAT::getAttackCooldownThreshold,
                                SlayvisualConfig.COMBAT::setAttackCooldownThreshold));
                y += 28;
                addDynamic(new ValueSlider(left + 20, y, 280, "Interval", 40f, 140f,
                                () -> (float) SlayvisualConfig.COMBAT.getMinimumAttackIntervalMs(),
                                value -> SlayvisualConfig.COMBAT.setMinimumAttackIntervalMs((long) value.floatValue())));
                y += 30;

                addDynamic(new AnimatedWidget(left + 20, y, 280, 20, new LiteralText("Bind Trigger Bot")) {
                        @Override
                        protected void handlePress() {
                                capturingTriggerKey = true;
                        }

                        @Override
                        protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                                String current = capturingTriggerKey ? "Press a key..." : keyName();
                                drawCenteredText(matrices, textRenderer, current, this.x + this.width / 2, this.y + 6, 0xFFFFFF);
                        }

                        private String keyName() {
                                String name = GLFW.glfwGetKeyName(SlayvisualConfig.COMBAT.getTriggerBotKeyCode(), 0);
                                if (name == null) {
                                        return "Key: " + SlayvisualConfig.COMBAT.getTriggerBotKeyCode();
                                }
                                return "Current: " + name.toUpperCase(Locale.ROOT);
                        }
                });
        }

        private void addDynamic(AbstractButtonWidget widget) {
                this.dynamicWidgets.add(widget);
                this.addButton(widget);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (capturingTriggerKey) {
                        capturingTriggerKey = false;
                        if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                                SlayvisualConfig.COMBAT.setTriggerBotKeyCode(keyCode);
                                TriggerBot.setToggleKey(keyCode);
                        }
                        return true;
                }

                if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_INSERT) {
                        this.onClose();
                        return true;
                }

                return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                renderBackground(matrices);

                int left = (this.width - PANEL_WIDTH) / 2;
                int top = (this.height - PANEL_HEIGHT) / 2;

                RenderSystem.enableBlend();
                fillGradient(matrices, left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xC0101010, 0xD0131321);
                RenderSystem.disableBlend();

                super.render(matrices, mouseX, mouseY, delta);

                drawCenteredText(matrices, this.textRenderer, "Celestial Config", left + PANEL_WIDTH / 2, top + 28, 0xFFFFFF);

                SlayvisualConfig.HitColor hit = SlayvisualConfig.VISUAL.getHitColor();
                int previewColor = ((hit.getAlpha() & 0xFF) << 24) | ((hit.getRed() & 0xFF) << 16) | ((hit.getGreen() & 0xFF) << 8) | (hit.getBlue() & 0xFF);
                fill(matrices, left + PANEL_WIDTH - 50, top + PANEL_HEIGHT - 30, left + PANEL_WIDTH - 20, top + PANEL_HEIGHT - 10, previewColor);
                this.textRenderer.drawWithShadow(matrices, "Hit", left + PANEL_WIDTH - 48, top + PANEL_HEIGHT - 40, 0xFFFFFF);
        }

        private final class TabButton extends AnimatedWidget {
                private final Category category;

                private TabButton(int x, int y, Category category) {
                        super(x, y, 80, 24, new LiteralText(category.displayName));
                        this.category = category;
                }

                @Override
                protected void handlePress() {
                        if (SlayvisualScreen.this.category != this.category) {
                                SlayvisualScreen.this.category = this.category;
                                rebuildCategory((SlayvisualScreen.this.width - PANEL_WIDTH) / 2, (SlayvisualScreen.this.height - PANEL_HEIGHT) / 2 + 60);
                        }
                }

                @Override
                protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        drawCenteredText(matrices, textRenderer, category.displayName, this.x + this.width / 2, this.y + 8, 0xFFFFFF);
                }

                @Override
                protected boolean isActive() {
                        return SlayvisualScreen.this.category == this.category;
                }
        }

        private class ToggleButton extends AnimatedWidget {
                private final Supplier<Boolean> getter;
                private final Consumer<Boolean> setter;

                private ToggleButton(int x, int y, int width, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
                        super(x, y, width, 22, new LiteralText(label));
                        this.getter = getter;
                        this.setter = setter;
                }

                @Override
                protected void handlePress() {
                        this.setter.accept(!this.getter.get());
                }

                @Override
                protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        boolean enabled = getter.get();
                        int color = enabled ? Formatting.LIGHT_PURPLE.getColorValue() : 0xAAAAAA;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + 6, color);
                }

                @Override
                protected boolean isActive() {
                        return getter.get();
                }
        }

        private final class EnumCycleButton<T extends Enum<T>> extends AnimatedWidget {
                private final T[] values;
                private final Supplier<T> getter;
                private final Consumer<T> setter;
                private final Function<T, String> formatter;
                private final String label;

                private EnumCycleButton(int x, int y, int width, String label, T[] values, Supplier<T> getter,
                                Consumer<T> setter, Function<T, String> formatter) {
                        super(x, y, width, 20, LiteralText.EMPTY);
                        this.label = label;
                        this.values = values;
                        this.getter = getter;
                        this.setter = setter;
                        this.formatter = formatter;
                }

                @Override
                protected void handlePress() {
                        T current = getter.get();
                        int index = 0;
                        for (int i = 0; i < values.length; i++) {
                                if (values[i] == current) {
                                        index = i;
                                        break;
                                }
                        }
                        setter.accept(values[(index + 1) % values.length]);
                }

                @Override
                protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        String text = label + ": " + formatter.apply(getter.get());
                        drawCenteredText(matrices, textRenderer, new LiteralText(text), this.x + this.width / 2, this.y + 6,
                                        0xFFFFFF);
                }

                @Override
                protected boolean isActive() {
                        return true;
                }
        }

        private final class ValueSlider extends SliderWidget implements AnimatedWidgetHolder {
                private final float min;
                private final float max;
                private final Supplier<Float> getter;
                private final Consumer<Float> setter;
                private final String label;
                private float animation;

                private ValueSlider(int x, int y, int width, String label, float min, float max, Supplier<Float> getter, Consumer<Float> setter) {
                        super(x, y, width, 20, LiteralText.EMPTY, 0.0);
                        this.min = min;
                        this.max = max;
                        this.getter = getter;
                        this.setter = setter;
                        this.label = label;
                        this.value = toSliderValue(getter.get());
                        setMessage(new LiteralText(labelText()));
                }

                private double toSliderValue(float input) {
                        return (input - min) / (max - min);
                }

                private float toRealValue(double slider) {
                        return (float) (slider * (max - min) + min);
                }

                private String labelText() {
                        return label + ": " + String.format(Locale.ROOT, "%.2f", toRealValue(this.value));
                }

                @Override
                protected void updateMessage() {
                        setMessage(new LiteralText(labelText()));
                }

                @Override
                protected void applyValue() {
                        float real = toRealValue(this.value);
                        setter.accept(real);
                }

                @Override
                public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        animation += (float) ((isHovered() ? 1.0 : 0.0) - animation) * 0.2f;
                        super.render(matrices, mouseX, mouseY, delta);
                }

                @Override
                public void tickAnimation() {
                        animation += (float) ((isHovered() ? 1.0 : 0.0) - animation) * 0.2f;
                }
        }

        private final class ColorSlider extends SliderWidget implements AnimatedWidgetHolder {
                private final Supplier<Integer> getter;
                private final Consumer<Integer> setter;
                private final String label;
                private float animation;

                private ColorSlider(int x, int y, String label, Supplier<Integer> getter, Consumer<Integer> setter) {
                        super(x, y, 280, 20, LiteralText.EMPTY, 0.0);
                        this.label = label;
                        this.getter = getter;
                        this.setter = setter;
                        this.value = getter.get() / 255.0;
                        setMessage(new LiteralText(label + ": " + getter.get()));
                }

                @Override
                protected void updateMessage() {
                        setMessage(new LiteralText(label + ": " + (int) Math.round(this.value * 255.0)));
                }

                @Override
                protected void applyValue() {
                        setter.accept((int) Math.round(this.value * 255.0));
                }

                @Override
                public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        animation += (float) ((isHovered() ? 1.0 : 0.0) - animation) * 0.2f;
                        super.render(matrices, mouseX, mouseY, delta);
                }

                @Override
                public void tickAnimation() {
                        animation += (float) ((isHovered() ? 1.0 : 0.0) - animation) * 0.2f;
                }
        }

        private enum Category {
                VISUALS("Visuals"),
                COMBAT("Combat");

                private final String displayName;

                Category(String displayName) {
                        this.displayName = displayName;
                }
        }

        private abstract class AnimatedWidget extends ButtonWidget implements AnimatedWidgetHolder {
                private float hoverAnimation = 0.0f;

                protected AnimatedWidget(int x, int y, int width, int height, Text text) {
                        super(x, y, width, height, text, button -> ((AnimatedWidget) button).handlePress());
                }

                @Override
                public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        this.hoverAnimation += ((isHovered() ? 1.0f : 0.0f) - this.hoverAnimation) * 0.25f;
                        int base = isActive() ? 0xFF7A40FF : 0xFF262626;
                        int color = mixColors(base, 0xFF383838, 1.0f - hoverAnimation);
                        SlayvisualScreen.this.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, color);
                        renderContent(matrices, mouseX, mouseY, delta);
                }

                @Override
                public void tickAnimation() {
                        this.hoverAnimation += ((isHovered() ? 1.0f : 0.0f) - this.hoverAnimation) * 0.25f;
                }

                protected void renderContent(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        drawCenteredText(matrices, SlayvisualScreen.this.textRenderer, getMessage(), this.x + this.width / 2, this.y + 6, 0xFFFFFF);
                }

                protected boolean isActive() {
                        return false;
                }

                protected abstract void handlePress();

        }

        private interface AnimatedWidgetHolder {
                void tickAnimation();
        }

        private static int mixColors(int first, int second, float progress) {
                int a = (int) (((first >> 24) & 0xFF) * progress + ((second >> 24) & 0xFF) * (1 - progress));
                int r = (int) (((first >> 16) & 0xFF) * progress + ((second >> 16) & 0xFF) * (1 - progress));
                int g = (int) (((first >> 8) & 0xFF) * progress + ((second >> 8) & 0xFF) * (1 - progress));
                int b = (int) ((first & 0xFF) * progress + (second & 0xFF) * (1 - progress));
                return (a << 24) | (r << 16) | (g << 8) | b;
        }
}
