package me.navidrohim.jmws.helper;

import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class PlayerHelper {
    public static void sendUserAlert(String text, boolean overlayText, boolean ignoreConfig, JMWSMessageType messageType) {
        String finalText = text;

        Constants.LOGGER.info(finalText);
        if (CommonClass.config.colouredText) {
            finalText = messageType.toString() + finalText;
        }

        if ((CommonClass.config.showAlerts || ignoreConfig) && CommonClass.minecraftClientInstance.player != null) {
            CommonClass.minecraftClientInstance.player.sendStatusMessage(new TextComponentString(finalText), overlayText);

        }
    }

    public static void sendUserSoundAlert(SoundEvent sound) {

        if (CommonClass.config.playEffects && CommonClass.minecraftClientInstance.player != null) {
            CommonClass.minecraftClientInstance.player.playSound(sound, 0.09f, 1f);
        }
    }
}
