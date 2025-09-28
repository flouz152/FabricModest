package com.slayvisual;

import net.fabricmc.api.ClientModInitializer;

public class SlayvisualClient implements ClientModInitializer {
        @Override
        public void onInitializeClient() {
                TriggerBot.init();
        }
}
