package me.navidrohim.jmws.client.network;

import me.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.common.Constants;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import me.navidrohim.jmws.client.helpers.JMWSSounds;
import me.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.navidrohim.jmws.common.helper.CommonHelper;
import me.navidrohim.jmws.common.helper.PlayerHelper;
import me.navidrohim.jmws.common.payloads.JMWSActionMessage;
import me.navidrohim.jmws.common.payloads.JMWSHandshakeReplyMessage;
import org.jetbrains.annotations.Nullable;


import java.util.Objects;

import static me.navidrohim.jmws.common.helper.PlayerHelper.sendUserAlert;


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

    /**
     * Send text alert for user join
     * @param serverVersion -- What server version to check against expected local version to give response.
     */
    private static void sendUserJoinAlert(@Nullable Double serverVersion)
    {
        // I hate all the following code :)
        if (serverVersion == null)
        {
            sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.server.no_version"), true, false, JMWSMessageType.FAILURE);
        } else if (serverVersion < Constants.SERVER_VERSION) {
            sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.server.older_server_version"), true, false, JMWSMessageType.WARNING);
            Constants.getLogger().warn(String.format("Got server version; %s expected; %s", serverVersion, Constants.SERVER_VERSION));
        } else if (serverVersion > Constants.SERVER_VERSION) {
            sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.server.newer_server_version"), true, false, JMWSMessageType.WARNING);
            Constants.getLogger().warn(String.format("Got server version; %s expected; %s", serverVersion, Constants.SERVER_VERSION));

        } else if (!CommonClass.serverConfig.jmwsEnabled) {
            sendUserAlert(CommonHelper.getTranslatableComponent("warning.jmws.server.disabled_jmws"), true, false, JMWSMessageType.WARNING);
        } else {
            sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.has_jmws", (CommonClass.serverConfig.getServerVersion())), true, false, JMWSMessageType.SUCCESS);
        }
    }

    public static void HandshakeHandler(JMWSHandshakeReplyMessage handshakePayload) {
        CommonClass.serverConfig = handshakePayload.serverConfigData != null ? handshakePayload.serverConfigData : ClientSideServerConfigObject.serverOwner(); // Use serverOwner if on LAN, serverConfigData will be null if so (Because there is no physical server), so instantiate our own fake config just so shit don't crash.
        @Nullable Double serverVersion =  CommonClass.serverConfig.getServerVersion();
        CommonClass.setServerModStatus(true); // We have JMWS on server side
        sendUserJoinAlert(serverVersion);
    }
}
