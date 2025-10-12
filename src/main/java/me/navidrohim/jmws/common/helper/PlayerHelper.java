package me.navidrohim.jmws.common.helper;

import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.ArrayList;
import java.util.List;

public class PlayerHelper {
    private static final List<String> sentWarningsInServer = new ArrayList<>();

    public static void sendUserAlert(String text, boolean overlayText, boolean ignoreConfig, JMWSMessageType messageType) {
        String finalText = text;

        if (CommonClass.config.colouredText) {
            finalText = messageType.toString() + finalText;
        }

        if ((CommonClass.config.showAlerts || ignoreConfig) && CommonClass.minecraftClientInstance.player != null) {
            CommonClass.minecraftClientInstance.player.sendStatusMessage(new TextComponentString(finalText), overlayText);

        }
    }

    public static void sendUserSoundAlert(SoundEvent sound) {

        if (CommonClass.config.playEffects && CommonClass.minecraftClientInstance.player != null) {
            FMLClientHandler.instance().getClientPlayerEntity().playSound(sound, 0.09f, 1f);
        }
    }

    /**
     * Clears the warning cache. Warnings being alerts with JMWSMessageType.ONE_TIME_WARNING as the messageType.
     * This is usually called every time the player leaves the server.
     */
    public static void clearWarningAlertCache()
    {
        sentWarningsInServer.clear();
    }
}
