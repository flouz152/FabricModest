# SlayVisual

SlayVisual is a Fabric 1.16.5 client-side utility mod that provides an in-game configuration GUI and an automated trigger bot module.

## Features
- **Trigger Bot** – automatically attacks the entity under your crosshair once your weapon cooldown has reset. You can set a custom delay in ticks to fine-tune behaviour.
- **In-game GUI** – open the configuration screen with the default <kbd>G</kbd> key to toggle the trigger bot, adjust the delay, and enable or disable the HUD overlay.
- **HUD Overlay** – shows the current trigger bot status and the keybinding hint directly on the screen.

## Building
Run the following command from the project root to build the mod JAR:

```bash
./gradlew build
```

The compiled JAR will be located in `build/libs`.

> **Note for Windows users:** run `gradlew.bat build` if you are using the Command Prompt or PowerShell. Running the plain `gradle` command outside the project directory will result in the "Directory ... does not contain a Gradle build" error.
