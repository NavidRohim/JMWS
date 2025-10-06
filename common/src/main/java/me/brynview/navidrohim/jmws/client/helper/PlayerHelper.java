package me.brynview.navidrohim.jmws.client.helper;

import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerHelper {
    private static List<String> sentWarningsInServer = new ArrayList<>();

    public static void sendUserAlert(Component text, boolean overlayText, boolean ignoreConfig, JMWSMessageType messageType) {
        if (!sentWarningsInServer.contains(text.getString()))
        {
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
                if (messageType.equals(JMWSMessageType.ONE_TIME_WARNING))
                {
                    sentWarningsInServer.add(text.getString());
                }
            }
        }
    }

    public static void clearWarningAlertCache()
    {
        sentWarningsInServer.clear();
    }

    public static void sendUserSoundAlert(SoundEvent sound) {

        if (CommonClass.config.playEffects.get() && CommonClass.minecraftClientInstance.player != null) {
            CommonClass.minecraftClientInstance.player.playSound(sound, 0.09f, 1f);
        }
    }

    public static void teleportPlayer() {

    }
}
