package com.slayvisual;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Slayvisual implements ModInitializer {
    public static final String MOD_ID = "slayvisual";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("SlayVisual core initialized");
    }
}
