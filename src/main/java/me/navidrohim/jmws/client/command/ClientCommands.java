package me.navidrohim.jmws.client.command;

import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import me.navidrohim.jmws.common.helper.CommandHelper;
import me.navidrohim.jmws.common.helper.CommonHelper;
import me.navidrohim.jmws.common.helper.PlayerHelper;
import me.navidrohim.jmws.common.payloads.JMWSActionMessage;
import me.navidrohim.jmws.common.payloads.JMWSNetworkWrapper;
import me.navidrohim.jmws.client.plugin.JMWSPlugin;

public class ClientCommands {

    private static boolean isInSingleplayer() {
        return CommonClass.minecraftClientInstance.isSingleplayer();
    }

    private static void sendUserSinglePlayerWarning() {
        PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.world_is_local_no_commands"), true, false, JMWSMessageType.WARNING);
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
        PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.sync_frequency", CommonClass.syncCounter.getTickCounterUpdateThreshold() / 20), true, false, JMWSMessageType.NEUTRAL);
        return 1;
    }

    public static int clearAllWaypoints()
    {
        if (!isInSingleplayer()) {
            JMWSActionMessage deleteServerObjectPayload = new JMWSActionMessage(CommandHelper.makeDeleteRequestJson("", false, true)); // * = all
            JMWSNetworkWrapper.INSTANCE.sendToServer(deleteServerObjectPayload);
            //Dispatcher.sendToServer(deleteServerObjectPayload);
            JMWSPlugin.updateWaypoints(false);
        } else {
            sendUserSinglePlayerWarning();
        }
        return 1;
    }

    public static int nextSync()
    {
        if (!isInSingleplayer()) {
            if (CommonClass.config.autoSync)
            {
                PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.next_sync", (CommonClass.syncCounter.getTickCounterUpdateThreshold() - CommonClass.syncCounter.getCurrentTickCount()) / 20), true, false, JMWSMessageType.NEUTRAL);
            } else {
                PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.auto_sync_disabled"), true, false, JMWSMessageType.WARNING);
            }
        } else {
            sendUserSinglePlayerWarning();
        }
        return 1;
    }
}
