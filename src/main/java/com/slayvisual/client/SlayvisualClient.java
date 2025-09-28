package com.slayvisual.client;

import com.slayvisual.Slayvisual;
import com.slayvisual.client.config.SlayvisualConfig;
import com.slayvisual.client.gui.SlayvisualConfigScreen;
import com.slayvisual.client.hud.SlayvisualHudRenderer;
import com.slayvisual.client.module.TriggerBot;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SlayvisualClient implements ClientModInitializer {
    private static final KeyBinding OPEN_CONFIG_KEY = KeyBindingHelper.registerKeyBinding(
        new KeyBinding(
            "key.slayvisual.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.slayvisual"
        )
    );

    public static KeyBinding getOpenConfigKey() {
        return OPEN_CONFIG_KEY;
    }

    @Override
    public void onInitializeClient() {
        Slayvisual.LOGGER.info("Initializing SlayVisual client components");
        SlayvisualConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CONFIG_KEY.wasPressed()) {
                client.setScreen(new SlayvisualConfigScreen(client.currentScreen));
            }

            TriggerBot.onEndTick(client);
        });

        HudRenderCallback.EVENT.register((matrices, tickDelta) -> SlayvisualHudRenderer.render(matrices));
    }
}
