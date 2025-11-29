package me.brynview.navidrohim.jmws.client.commands;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
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
        PlayerHelper.sendUserAlert(Component.translatable("warning.jmws.world_is_local_no_commands"), true, false, JMWSMessageType.WARNING);
    }

    /**
     * Manual sync command.
     * @return int -- If the command was successful. Will always be 1
     */
    public static int sync()
    {
        if (isNotInSingleplayer()) {
            JMWSPlugin.updateWaypoints(true);
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
        PlayerHelper.sendUserAlert(Component.translatable("message.jmws.sync_frequency", CommonClass.syncCounter.getTickCounterUpdateThreshold() / 20), true, false, JMWSMessageType.NEUTRAL);
        return 1;
    }

    /**
     * Deletes all groups on server and client, also deletes all waypoints inside of those groups.
     * @return int -- If the command was successful. Will always be 1
     */
    public static int clearAllGroups()
    {
        if (isNotInSingleplayer()) {
            JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(CommandFactory.makeDeleteGroupRequestJson(
                    "*",
                    "*",
                    false,
                    false,
                    true,
                    true,
                    true
            ));
            Dispatcher.sendToServer(deleteServerObjectPayload); // Deletes waypoints on the server
            JMWSPlugin.updateWaypoints(false);
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
            JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(CommandFactory.makeDeleteRequestJson("*", false, true)); // * = all
            Dispatcher.sendToServer(deleteServerObjectPayload);
            JMWSPlugin.updateWaypoints(false);
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
            if (CommonClass.config.autoSync.get())
            {
                PlayerHelper.sendUserAlert(Component.translatable("message.jmws.next_sync", (CommonClass.syncCounter.getTickCounterUpdateThreshold() - CommonClass.syncCounter.getCurrentTickCount()) / 20), true, false, JMWSMessageType.NEUTRAL);
            } else {
                PlayerHelper.sendUserAlert(Component.translatable("message.jmws.auto_sync_disabled"), true, false, JMWSMessageType.WARNING);
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
            PlayerHelper.sendUserAlert(Component.translatable("sharing.jmws.sharing_child"), true, false, JMWSMessageType.NEUTRAL);
        } else {
            PlayerHelper.sendUserAlert(Component.translatable("sharing.jmws.no_requests"), true, false, JMWSMessageType.NEUTRAL);
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
            PlayerHelper.sendUserAlert(Component.translatable("sharing.jmws.decline"), true, false, JMWSMessageType.NEUTRAL);
        } else {
            PlayerHelper.sendUserAlert(Component.translatable("sharing.jmws.no_requests"), true, false, JMWSMessageType.NEUTRAL);
        }
        return 1;
    }

    public static int decline(@Nullable String from)
    {

        @Nullable ShareRequest request = IncomingShareRequests.getAllUserKey().get(from);
        return decline(request);
    }
}
