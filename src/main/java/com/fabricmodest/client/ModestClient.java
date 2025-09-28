package com.fabricmodest.client;

import com.fabricmodest.client.gui.ModestScreen;
import com.fabricmodest.client.module.TriggerBot;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ModestClient implements ClientModInitializer {
    public static final String MOD_ID = "fabricmodest";

    private static KeyBinding openGuiKey;
    private static final TriggerBot TRIGGER_BOT = new TriggerBot();

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fabricmodest.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.misc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::handleClientTick);
    }

    private void handleClientTick(MinecraftClient client) {
        if (client == null) {
            return;
        }

        while (openGuiKey.wasPressed()) {
            if (client.currentScreen instanceof ModestScreen) {
                client.setScreen(null);
            } else {
                client.setScreen(new ModestScreen());
            }
        }

        TRIGGER_BOT.tick(client);
    }

    public static TriggerBot getTriggerBot() {
        return TRIGGER_BOT;
    }
}
