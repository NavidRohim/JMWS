package me.brynview.navidrohim.jmws.helper;

import me.brynview.navidrohim.jmws.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class PlayerHelper {
    public static void sendUserAlert(Component text, boolean overlayText, boolean ignoreConfig, JMWSMessageType messageType) {
        String finalText = text.getString();

        if (CommonClass.config.colouredText.get()) {
            finalText = messageType.toString() + text.getString();
        }

        if ((CommonClass.config.showAlerts.get() || ignoreConfig) && CommonClass.minecraftClientInstance.player != null) {
            if (overlayText) {
                CommonClass.minecraftClientInstance.gui.setOverlayMessage(Component.literal(finalText), false);
            } else {
                CommonClass.minecraftClientInstance.gui.getChat().addMessage(Component.literal(finalText));
            }
        }
    }

    public static void sendUserSoundAlert(SoundEvent sound) {

        if (CommonClass.config.playEffects.get() && CommonClass.minecraftClientInstance.player != null) {
            CommonClass.minecraftClientInstance.player.playSound(sound, 0.09f, 1f);
        }
    }
}
