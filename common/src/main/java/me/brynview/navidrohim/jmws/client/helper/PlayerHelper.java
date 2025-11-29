package me.brynview.navidrohim.jmws.client.helper;

import com.mojang.authlib.GameProfile;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Random player functions that play sounds or sends action bar alerts.
 */
public class PlayerHelper {
    private static final List<String> sentWarningsInServer = new ArrayList<>();

    /**
     * Send an alert to the user.
     * @param text What text to send the user. Make it an i18n key if possible.
     * @param overlayText If to overlay text on to the action bar, if `false` it will be put in the users chat.
     * @param ignoreConfig If to ignore the users set config value. If they have alerts turned off but ignoreConfig is true, the alert will be sent regardless.
     * @param messageType What colour the message will be. Named with importance instead of colour.
     */
    public static void sendUserAlert(Component text, boolean overlayText, boolean ignoreConfig, JMWSMessageType messageType) {

        // Check if player allows alerts, check if player exists, and make sure it has not been sent before if one-time message
        if (!sentWarningsInServer.contains(text.getString()) && (CommonClass.config.showAlerts.get() || ignoreConfig) && CommonClass.minecraftClientInstance.player != null)
        {
            String finalText = text.getString();

            // Check if user allows coloured text, add colour tag if so
            if (CommonClass.config.colouredText.get()) {
                finalText = messageType.toString() + text.getString();
            }

            if (overlayText) {
                CommonClass.minecraftClientInstance.gui.setOverlayMessage(Component.literal(finalText), false); // Action bar
            } else {
                CommonClass.minecraftClientInstance.gui.getChat().addMessage(Component.literal(finalText)); // Chat
            }

            // Add alert to cache if it's one-time
            if (messageType.equals(JMWSMessageType.ONE_TIME_WARNING))
            {
                sentWarningsInServer.add(text.getString());
            }

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

    /**
     * Play a sound effect to the user.
     * @param sound SoundEvent -- Find a list of mod-defined sound events in JMWSSounds or SoundEvents class for vanilla sounds.
     */
    public static void sendUserSoundAlert(SoundEvent sound) {

        if (CommonClass.config.playEffects.get() && CommonClass.minecraftClientInstance.player != null) {
            CommonClass.minecraftClientInstance.player.playSound(sound, 0.09f, 1f); // Lower volume so it becomes background noise
        }
    }

    public static Optional<GameProfile> getUserFromUUID(UUID user)
    {
        ClientPacketListener clientPacketListener = Objects.requireNonNull(CommonClass.minecraftClientInstance.getConnection());
        @Nullable PlayerInfo playerInfo = clientPacketListener.getPlayerInfo(user);

        if (playerInfo != null)
        {
            return Optional.of(playerInfo.getProfile());
        }
        return Optional.empty();
    }

    public static String getUsernameFromUUID(UUID user)
    {
        Optional<GameProfile> profile = getUserFromUUID(user);
        return profile.isPresent() ? profile.get().name() : CommonHelper.unknownUser;
    }

    public static String getUsernameFromUUID(UUID user, boolean withTag)
    {
        Optional<GameProfile> profile = getUserFromUUID(user);
        return profile.isPresent() ? profile.get().name() : "S";
    }

    public static UUID ourUUID()
    {
        return CommonClass.minecraftClientInstance.player.getUUID();
    }
}
