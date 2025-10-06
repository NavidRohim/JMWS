package me.brynview.navidrohim.jmws.client;

import commonnetwork.api.Dispatcher;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.common.helper.CommandHelper;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import net.minecraft.network.chat.Component;

public class ClientCommands {

    private static boolean isInSingleplayer() {
        return CommonClass.minecraftClientInstance.isSingleplayer();
    }

    private static void sendUserSinglePlayerWarning() {
        PlayerHelper.sendUserAlert(Component.translatable("warning.jmws.world_is_local_no_commands"), true, false, JMWSMessageType.WARNING);
    }

    public static int sync()
    {
        if (!isInSingleplayer()) {
            JMWSPlugin.updateWaypoints(true);
        } else {
            sendUserSinglePlayerWarning();
        }
        return 1;
    }

    public static int getSyncInterval()
    {
        PlayerHelper.sendUserAlert(Component.translatable("message.jmws.sync_frequency", CommonClass.syncCounter.getTickCounterUpdateThreshold() / 20), true, false, JMWSMessageType.NEUTRAL);
        return 1;
    }

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
            Dispatcher.sendToServer(deleteServerObjectPayload);
            JMWSPlugin.updateWaypoints(false);
            JMWSPlugin.removeAllGroups();
        } else {
            sendUserSinglePlayerWarning();
        }

        return 1;
    }

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
}
