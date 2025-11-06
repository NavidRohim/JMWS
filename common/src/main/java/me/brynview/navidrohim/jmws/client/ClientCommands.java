package me.brynview.navidrohim.jmws.client;

import com.mojang.brigadier.context.CommandContext;
import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.plugin.ObjectIdentifierMap;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.common.helper.CommandHelper;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Static class that holds methods which commands use.
 */
public class ClientCommands {

    /**
     * Returns if the player is in singleplayer.
     * @return boolean -- If the player is in singleplayer.
     */
    private static boolean isInSingleplayer() {
        return CommonClass.minecraftClientInstance.isSingleplayer();
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
        if (!isInSingleplayer()) {
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
        if (!isInSingleplayer()) {
            JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(CommandHelper.makeDeleteGroupRequestJson(
                    CommonClass.minecraftClientInstance.player.getUUID(),
                    "*",
                    "*",
                    false,
                    false,
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
        if (!isInSingleplayer()) {
            JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(CommandHelper.makeDeleteRequestJson("*", false, true)); // * = all
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
        if (!isInSingleplayer()) {
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

    /**
     * this function is a WIP and has not been tested and is likely not functional.
     * @param player wip
     * @param waypointUUID wip
     * @return int -- If the command was successful. Will always be 1
     */
    public static int sendObjectShareRequest(ServerPlayer player, String waypointUUID)
    {
        Constants.getLogger().info("test");
        Dispatcher.sendToClient(new JMWSActionPayload(CommandHelper.makeObjectShareRequestForUser(ObjectIdentifierMap.getOldWaypoint(waypointUUID))), player);
        return 1;
    }
}
