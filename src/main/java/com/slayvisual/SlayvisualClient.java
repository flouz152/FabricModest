package com.slayvisual;

import com.slayvisual.config.SlayvisualConfig;
import com.slayvisual.render.TargetVisuals;
import com.slayvisual.ui.SlayvisualNotifications;
import com.slayvisual.ui.screen.SlayvisualScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SlayvisualClient implements ClientModInitializer {
        private static KeyBinding openGuiKey;

        @Override
        public void onInitializeClient() {
                SlayvisualNotifications.init();
                TargetVisuals.init();

                TriggerBot.init();

                openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                                "key.slayvisual.gui",
                                InputUtil.Type.KEYSYM,
                                GLFW.GLFW_KEY_INSERT,
                                "category.slayvisual"
                ));

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        while (openGuiKey.wasPressed()) {
                                openConfigScreen(client);
                        }
                });

                TriggerBot.setToggleKey(SlayvisualConfig.COMBAT.getTriggerBotKeyCode());
                TriggerBot.setEnabled(SlayvisualConfig.COMBAT.isTriggerBotEnabled());
        }

        private void openConfigScreen(MinecraftClient client) {
                if (client == null) {
                        return;
                }

                Screen current = client.currentScreen;
                if (current instanceof SlayvisualScreen) {
                        client.openScreen(null);
                } else {
                        client.openScreen(new SlayvisualScreen());
                }
        }
}
