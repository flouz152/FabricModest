package com.slayvisual.config;

import com.slayvisual.KillAura;
import com.slayvisual.TriggerBot;
import com.slayvisual.ui.SlayvisualNotifications;
import org.lwjgl.glfw.GLFW;

public final class SlayvisualConfig {
        public static final VisualConfig VISUAL = new VisualConfig();
        public static final CombatConfig COMBAT = new CombatConfig();

        private SlayvisualConfig() {
        }

        public static final class VisualConfig {
                private final HitColor hitColor = new HitColor(220, 65, 255, 200);
                private boolean viewModelEnabled = false;
                private float viewModelOffsetX = 0.0f;
                private float viewModelOffsetY = 0.0f;
                private float viewModelOffsetZ = 0.0f;
                private float viewModelScale = 1.0f;
                private SwordAnimation swordAnimation = SwordAnimation.CELESTIAL;
                private boolean targetHudEnabled = true;
                private boolean targetEspEnabled = true;

                public HitColor getHitColor() {
                        return hitColor;
                }

                public boolean isViewModelEnabled() {
                        return viewModelEnabled;
                }

                public void setViewModelEnabled(boolean viewModelEnabled) {
                        this.viewModelEnabled = viewModelEnabled;
                        SlayvisualNotifications.notifyToggle("View Model", viewModelEnabled);
                }

                public float getViewModelOffsetX() {
                        return viewModelOffsetX;
                }

                public void setViewModelOffsetX(float viewModelOffsetX) {
                        this.viewModelOffsetX = viewModelOffsetX;
                }

                public float getViewModelOffsetY() {
                        return viewModelOffsetY;
                }

                public void setViewModelOffsetY(float viewModelOffsetY) {
                        this.viewModelOffsetY = viewModelOffsetY;
                }

                public float getViewModelOffsetZ() {
                        return viewModelOffsetZ;
                }

                public void setViewModelOffsetZ(float viewModelOffsetZ) {
                        this.viewModelOffsetZ = viewModelOffsetZ;
                }

                public float getViewModelScale() {
                        return viewModelScale;
                }

                public void setViewModelScale(float viewModelScale) {
                        this.viewModelScale = viewModelScale;
                }

                public SwordAnimation getSwordAnimation() {
                        return swordAnimation;
                }

                public void setSwordAnimation(SwordAnimation swordAnimation) {
                        this.swordAnimation = swordAnimation;
                        SlayvisualNotifications.notifyToggle("Sword Animation: " + swordAnimation.getDisplayName(), true);
                }

                public boolean isTargetHudEnabled() {
                        return targetHudEnabled;
                }

                public void setTargetHudEnabled(boolean targetHudEnabled) {
                        this.targetHudEnabled = targetHudEnabled;
                        SlayvisualNotifications.notifyToggle("Target HUD", targetHudEnabled);
                }

                public boolean isTargetEspEnabled() {
                        return targetEspEnabled;
                }

                public void setTargetEspEnabled(boolean targetEspEnabled) {
                        this.targetEspEnabled = targetEspEnabled;
                        SlayvisualNotifications.notifyToggle("Target ESP", targetEspEnabled);
                }
        }

        public static final class CombatConfig {
                private boolean triggerBotEnabled = true;
                private int triggerBotKeyCode = GLFW.GLFW_KEY_R;
                private float attackCooldownThreshold = 0.92f;
                private long minimumAttackIntervalMs = 85L;

                private boolean killAuraEnabled = false;
                private int killAuraKeyCode = GLFW.GLFW_KEY_R;
                private float killAuraRange = 3.0f;
                private float killAuraFov = 120.0f;
                private float killAuraRotationSpeed = 55.0f;
                private float killAuraCooldownThreshold = 0.88f;
                private long killAuraAttackIntervalMs = 110L;
                private long killAuraSwitchDelayMs = 350L;
                private KillAuraRotationMode killAuraRotationMode = KillAuraRotationMode.SNAP;

                public boolean isTriggerBotEnabled() {
                        return triggerBotEnabled;
                }

                public void setTriggerBotEnabled(boolean triggerBotEnabled) {
                        this.triggerBotEnabled = triggerBotEnabled;
                        SlayvisualNotifications.notifyToggle("Trigger Bot", triggerBotEnabled);
                        TriggerBot.setEnabled(triggerBotEnabled);
                }

                public int getTriggerBotKeyCode() {
                        return triggerBotKeyCode;
                }

                public void setTriggerBotKeyCode(int triggerBotKeyCode) {
                        this.triggerBotKeyCode = triggerBotKeyCode;
                        String keyName = GLFW.glfwGetKeyName(triggerBotKeyCode, 0);
                        if (keyName == null) {
                                keyName = "key " + triggerBotKeyCode;
                        }
                        SlayvisualNotifications.notify("Trigger Bot key bound to " + keyName.toUpperCase());
                        TriggerBot.setToggleKey(triggerBotKeyCode);
                }

                public float getAttackCooldownThreshold() {
                        return attackCooldownThreshold;
                }

                public void setAttackCooldownThreshold(float attackCooldownThreshold) {
                        this.attackCooldownThreshold = attackCooldownThreshold;
                }

                public long getMinimumAttackIntervalMs() {
                        return minimumAttackIntervalMs;
                }

                public void setMinimumAttackIntervalMs(long minimumAttackIntervalMs) {
                        this.minimumAttackIntervalMs = minimumAttackIntervalMs;
                }

                public boolean isKillAuraEnabled() {
                        return killAuraEnabled;
                }

                public void setKillAuraEnabled(boolean killAuraEnabled) {
                        this.killAuraEnabled = killAuraEnabled;
                        SlayvisualNotifications.notifyToggle("Kill Aura", killAuraEnabled);
                        KillAura.setEnabled(killAuraEnabled);
                }

                public int getKillAuraKeyCode() {
                        return killAuraKeyCode;
                }

                public void setKillAuraKeyCode(int killAuraKeyCode) {
                        this.killAuraKeyCode = killAuraKeyCode;
                        String keyName = GLFW.glfwGetKeyName(killAuraKeyCode, 0);
                        if (keyName == null) {
                                keyName = "key " + killAuraKeyCode;
                        }
                        SlayvisualNotifications.notify("Kill Aura key bound to " + keyName.toUpperCase());
                        KillAura.setToggleKey(killAuraKeyCode);
                }

                public float getKillAuraRange() {
                        return killAuraRange;
                }

                public void setKillAuraRange(float killAuraRange) {
                        this.killAuraRange = clamp(killAuraRange, 0.5f, 3.0f);
                }

                public float getKillAuraFov() {
                        return killAuraFov;
                }

                public void setKillAuraFov(float killAuraFov) {
                        this.killAuraFov = clamp(killAuraFov, 30.0f, 180.0f);
                }

                public float getKillAuraRotationSpeed() {
                        return killAuraRotationSpeed;
                }

                public void setKillAuraRotationSpeed(float killAuraRotationSpeed) {
                        this.killAuraRotationSpeed = clamp(killAuraRotationSpeed, 5.0f, 180.0f);
                }

                public float getKillAuraCooldownThreshold() {
                        return killAuraCooldownThreshold;
                }

                public void setKillAuraCooldownThreshold(float killAuraCooldownThreshold) {
                        this.killAuraCooldownThreshold = clamp(killAuraCooldownThreshold, 0.4f, 1.0f);
                }

                public long getKillAuraAttackIntervalMs() {
                        return killAuraAttackIntervalMs;
                }

                public void setKillAuraAttackIntervalMs(long killAuraAttackIntervalMs) {
                        this.killAuraAttackIntervalMs = Math.max(40L, Math.min(400L, killAuraAttackIntervalMs));
                }

                public long getKillAuraSwitchDelayMs() {
                        return killAuraSwitchDelayMs;
                }

                public void setKillAuraSwitchDelayMs(long killAuraSwitchDelayMs) {
                        this.killAuraSwitchDelayMs = Math.max(0L, Math.min(600L, killAuraSwitchDelayMs));
                }

                public KillAuraRotationMode getKillAuraRotationMode() {
                        return killAuraRotationMode;
                }

                public void setKillAuraRotationMode(KillAuraRotationMode killAuraRotationMode) {
                        this.killAuraRotationMode = killAuraRotationMode;
                        SlayvisualNotifications.notify("Kill Aura rotation: " + killAuraRotationMode.getDisplayName());
                }

                private float clamp(float value, float min, float max) {
                        if (value < min) {
                                return min;
                        }
                        if (value > max) {
                                return max;
                        }
                        return value;
                }
        }

        public static final class HitColor {
                private int red;
                private int green;
                private int blue;
                private int alpha;

                public HitColor(int red, int green, int blue, int alpha) {
                        this.red = red;
                        this.green = green;
                        this.blue = blue;
                        this.alpha = alpha;
                }

                public int getRed() {
                        return red;
                }

                public void setRed(int red) {
                        this.red = clamp(red);
                }

                public int getGreen() {
                        return green;
                }

                public void setGreen(int green) {
                        this.green = clamp(green);
                }

                public int getBlue() {
                        return blue;
                }

                public void setBlue(int blue) {
                        this.blue = clamp(blue);
                }

                public int getAlpha() {
                        return alpha;
                }

                public void setAlpha(int alpha) {
                        this.alpha = clamp(alpha);
                }

                public float getRedFloat() {
                        return red / 255.0f;
                }

                public float getGreenFloat() {
                        return green / 255.0f;
                }

                public float getBlueFloat() {
                        return blue / 255.0f;
                }

                public float getAlphaFloat() {
                        return alpha / 255.0f;
                }

                private int clamp(int value) {
                        return Math.max(0, Math.min(255, value));
                }
        }

        public enum KillAuraRotationMode {
                SNAP("Snap"),
                SMOOTH("Smooth");

                private final String displayName;

                KillAuraRotationMode(String displayName) {
                        this.displayName = displayName;
                }

                public String getDisplayName() {
                        return displayName;
                }
        }

        public enum SwordAnimation {
                CELESTIAL("Celestial"),
                CASCADE("Cascade"),
                NOVA("Nova"),
                VORTEX("Vortex"),
                HORIZON("Horizon");

                private final String displayName;

                SwordAnimation(String displayName) {
                        this.displayName = displayName;
                }

                public String getDisplayName() {
                        return displayName;
                }
        }
}
