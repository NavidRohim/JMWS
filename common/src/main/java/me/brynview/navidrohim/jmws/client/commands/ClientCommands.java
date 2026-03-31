package me.brynview.navidrohim.jmws.client.commands;

import me.brynview.navidrohim.jmws.client.ClientCommonClass;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Static class that holds methods which commands use.
 */
public class ClientCommands {

    /**
     * Returns if the player is in singleplayer.
     * @return boolean -- If the player is in singleplayer.
     */
    private static boolean isNotInSingleplayer() {
        return !CommonClass.isInternalServer();
    }

    /**
     * Convenience method for sending a warning that commands may not work in singleplayer.
     */
    private static void sendUserSinglePlayerWarning() {
        PlayerUtils.sendUserAlert(Component.translatable("warning.jmws.world_is_local_no_commands"), true, false, MessageType.WARNING);
    }

    /**
     * Manual sync command.
     * @return int -- If the command was successful. Will always be 1
     */
    public static int sync()
    {
        if (isNotInSingleplayer()) {
            JMWSPlugin.sync(true);
        } else {
            sendUserSinglePlayerWarning();
        }
        return 1;
    }

    /**
     * Displays auto-sync interval
     * @return int -- If the command was successful. Will always be 1
     */
    public static int getSyncInterval()
    {
        PlayerUtils.sendUserAlert(Component.translatable("message.jmws.sync_frequency", ClientCommonClass.syncCounter.getTickCounterUpdateThreshold() / 20), true, false, MessageType.NEUTRAL);
        return 1;
    }

    /**
     * Deletes all groups on server and client, also deletes all waypoints inside of those groups.
     * @return int -- If the command was successful. Will always be 1
     */
    public static int clearAllGroups()
    {
        if (isNotInSingleplayer()) {
            ClientNetworkDispatcher.deleteGroup(
                    "*",
                    "*",
                    false,
                    false,
                    true,
                    true,
                    true
            );
            JMWSPlugin.sync(false);
            JMWSPlugin.deleteAllGroups(); // Deletes local copies.
        } else {
            sendUserSinglePlayerWarning();
        }

        return 1;
    }

    /**
     * Deletes all waypoints on server and client.
     * @return int -- If the command was successful. Will always be 1
     */
    public static int clearAllWaypoints()
    {
        if (isNotInSingleplayer()) {
            ClientNetworkDispatcher.deleteWaypoint("*", false, true);
            JMWSPlugin.sync(false);
        } else {
            sendUserSinglePlayerWarning();
        }
        return 1;
    }

    /**
     * Displays how many seconds until next auto-sync.
     * @return int -- If the command was successful. Will always be 1
     */
    public static int nextSync()
    {
        if (isNotInSingleplayer()) {
            if (ClientCommonClass.config.autoSync.get())
            {
                PlayerUtils.sendUserAlert(Component.translatable("message.jmws.next_sync", (ClientCommonClass.syncCounter.getTickCounterUpdateThreshold() - ClientCommonClass.syncCounter.getCurrentTickCount()) / 20), true, false, MessageType.NEUTRAL);
            } else {
                PlayerUtils.sendUserAlert(Component.translatable("message.jmws.auto_sync_disabled"), true, false, MessageType.WARNING);
            }
        } else {
            sendUserSinglePlayerWarning();
        }
        return 1;
    }

    public static int accept(@Nullable ShareRequest specifiedShare)
    {
        if (specifiedShare != null)
        {
            specifiedShare.accept();
            PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.sharing_child"), true, false, MessageType.NEUTRAL);
        } else {
            PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.no_requests"), true, false, MessageType.NEUTRAL);
        }
        return 1;
    }

    public static int accept(@Nullable String name)
    {
        @Nullable ShareRequest request = IncomingShareRequests.getAllUserKey().get(name);
        return accept(request);
    }

    public static int decline(@Nullable ShareRequest request)
    {
        if (request != null)
        {
            request.decline();
            PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.decline"), true, false, MessageType.NEUTRAL);
        } else {
            PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.no_requests"), true, false, MessageType.NEUTRAL);
        }
        return 1;
    }

    public static int decline(@Nullable String from)
    {

        @Nullable ShareRequest request = IncomingShareRequests.getAllUserKey().get(from);
        return decline(request);
    }
}
