package me.navidrohim.jmws.client;

import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import me.navidrohim.jmws.helper.CommandHelper;
import me.navidrohim.jmws.helper.CommonHelper;
import me.navidrohim.jmws.helper.PlayerHelper;
import me.navidrohim.jmws.payloads.JMWSActionPayload;
import me.navidrohim.jmws.plugin.JMWSPlugin;

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
            JMWSActionPayload deleteServerObjectPayload = new JMWSActionPayload(CommandHelper.makeDeleteRequestJson("", false, true)); // * = all
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
            PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.next_sync", (CommonClass.syncCounter.getTickCounterUpdateThreshold() - CommonClass.syncCounter.getCurrentTickCount()) / 20), true, false, JMWSMessageType.NEUTRAL);
        } else {
            sendUserSinglePlayerWarning();
        }
        return 1;
    }
}
