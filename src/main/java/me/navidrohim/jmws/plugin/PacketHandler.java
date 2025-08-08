package me.navidrohim.jmws.plugin;

import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import me.navidrohim.jmws.client.helpers.JMWSSounds;
import me.navidrohim.jmws.helper.CommonHelper;
import me.navidrohim.jmws.helper.PlayerHelper;
import me.navidrohim.jmws.payloads.JMWSActionMessage;
import me.navidrohim.jmws.payloads.JMWSActionPayload;
import me.navidrohim.jmws.payloads.JMWSHandshakePayload;
import me.navidrohim.jmws.payloads.JMWSHandshakeReplyMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


import java.util.Objects;

import static me.navidrohim.jmws.enums.WaypointPayloadCommand.*;
import static me.navidrohim.jmws.helper.PlayerHelper.sendUserAlert;


public class PacketHandler {
    public static void handlePacket(JMWSActionMessage waypointPayload) {

        if (CommonClass.getEnabledStatus()) {

            if (CommonClass.minecraftClientInstance.player == null)
            {
                return;
            }

            switch (waypointPayload.command()) {

                // Was creation_response
                // Sends no outbound data
                case SYNC:
                    JMWSPlugin.syncHandler(waypointPayload, CommonClass.minecraftClientInstance.player);
                    break;

                // was "update"
                // Sends "request" packet | New = "SYNC"
                case REQUEST_CLIENT_SYNC:
                    JMWSPlugin.updateWaypoints(true);
                    break;

                // was display_interval
                // No outbound data
                case COMMON_DISPLAY_INTERVAL:
                    sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.sync_frequency", CommonClass.getSyncFrequency()), true, false, JMWSMessageType.NEUTRAL);
                    break;

                // was "alert"
                // No outbound data
                case CLIENT_ALERT: {
                    String firstArgument = waypointPayload.arguments().get(0). getAsString();
                    boolean isError = waypointPayload.arguments().get(waypointPayload.arguments().size() - 1).getAsBoolean();
                    JMWSMessageType messageType = JMWSMessageType.NEUTRAL;

                    if (isError) {
                        messageType = JMWSMessageType.FAILURE;
                        PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                    }

                    sendUserAlert(CommonHelper.getTranslatableComponent(firstArgument), waypointPayload.arguments().get(1).getAsBoolean(), false, messageType);

                    break;
                }



                // was "deleteWaypoint"
                // No outbound data
                case COMMON_DELETE_WAYPOINT:
                {
                    String firstArgument1 = waypointPayload.arguments().get(0).getAsString();
                    JMWSPlugin.getInstance().deleteSavedObjects(
                            Objects.equals(firstArgument1, "*"),
                            firstArgument1
                    );
                    break;
                }

                // was "display_next_update"
                // No outbound data
                case COMMON_DISPLAY_NEXT_UPDATE:
                    sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.next_sync", (CommonClass.timeUntilNextSync())), true, false, JMWSMessageType.NEUTRAL);
                    break;

                default: Constants.getLogger().warn("Unknown packet command -> " + waypointPayload.command());
            }
        }
    }

    public static void  HandshakeHandler(JMWSHandshakeReplyMessage handshakePayload) {

        Constants.LOGGER.info("HPAYLOAD: " + handshakePayload);
        Constants.LOGGER.info("HPAYLOAD CONFIG: " + handshakePayload.serverConfigData.jmwsEnabled);

        if (!handshakePayload.serverConfigData.jmwsEnabled) {
            sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.server_disabled_jmws"), true, false, JMWSMessageType.WARNING);
        } else if (!handshakePayload.serverConfigData.waypointsEnabled) {
            sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.server_disabled_waypoint"), true, false, JMWSMessageType.WARNING);
        } else {
            sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.has_jmws"), true, false, JMWSMessageType.SUCCESS);
        }

        CommonClass.setServerModStatus(true);
    }
}
