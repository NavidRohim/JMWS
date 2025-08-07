package me.navidrohim.jmws.helper;

import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class PlayerHelper {
    public static void sendUserAlert(TextComponentTranslation text, boolean overlayText, boolean ignoreConfig, JMWSMessageType messageType) {
        String finalText = text.toString();

        if (CommonClass.config.colouredText) {
            finalText = messageType.toString() + text.toString();
        }

        if ((CommonClass.config.showAlerts || ignoreConfig) && CommonClass.minecraftClientInstance.player != null) {
            if (overlayText) {
                CommonClass.minecraftClientInstance.player.sendStatusMessage(text, false);
            } else {
                CommonClass.minecraftClientInstance.player.sendChatMessage(text.toString());
            }
        }
    }

    public static void sendUserSoundAlert(SoundEvent sound) {

        if (CommonClass.config.playEffects && CommonClass.minecraftClientInstance.player != null) {
            CommonClass.minecraftClientInstance.player.playSound(sound, 0.09f, 1f);
        }
    }
}
