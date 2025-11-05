package me.brynview.navidrohim.jmws.client.network;

import commonnetwork.networking.data.PacketContext;
import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.JMWSSounds;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static me.brynview.navidrohim.jmws.client.helper.PlayerHelper.sendUserAlert;

/**
 * Collection of static methods relating to packet handling on the client side.
 */
public class PacketHandler {

    /**
     * Handles a packet sent from the server.
     * @param Context -- The packet from the server, containing a command for the client to execute. See WaypointPayloadCommand class for possible commands, though not all will be for client.
     */
    public static void handlePacket(PacketContext<JMWSActionPayload> Context) {
        JMWSActionPayload waypointPayload = Context.message();

        // Check if command should be processed (must be a client of a server)
        if (CommonClass.getEnabledStatus()) {

            if (CommonClass.minecraftClientInstance.player == null)
            {
                return;
            }

            switch (waypointPayload.command()) {

                // Was creation_response
                // Sends no outbound data
                case SYNC -> JMWSPlugin.syncHandler(waypointPayload);

                // was "update"
                // Sends "request" packet | New = "SYNC"
                case REQUEST_CLIENT_SYNC -> JMWSPlugin.updateWaypoints(true);

                // was display_interval
                // No outbound data
                case COMMON_DISPLAY_INTERVAL -> sendUserAlert(Component.translatable("message.jmws.sync_frequency", CommonClass.getSyncFrequency()), true, false, JMWSMessageType.NEUTRAL);

                // was "alert"
                // No outbound data
                // This might be useless. Found out recently there is a way to do this with vanilla code without defining a custom packet.
                case CLIENT_ALERT -> {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    boolean isError = waypointPayload.arguments().getLast().getAsBoolean();
                    JMWSMessageType messageType = JMWSMessageType.NEUTRAL;

                    if (isError) {
                        messageType = JMWSMessageType.FAILURE;
                        PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                    }

                    sendUserAlert(Component.translatable(firstArgument), waypointPayload.arguments().get(1).getAsBoolean(), false, messageType);
                }

                // was "deleteWaypoint"
                // No outbound data
                case COMMON_DELETE_WAYPOINT ->
                {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();

                    // firstArgument can be "*" to delete all waypoints / groups, deleteAll must be `true` for this to happen still.
                    JMWSPlugin.getInstance().deleteSavedObjects(
                            Objects.equals(firstArgument, "*"),
                            FetchType.valueOf(waypointPayload.arguments().get(1).getAsString()),
                            firstArgument
                    );
                }

                // was "display_next_update"
                // No outbound data
                case COMMON_DISPLAY_NEXT_UPDATE -> sendUserAlert(Component.translatable("message.jmws.next_sync", (CommonClass.timeUntilNextSync())), true, false, JMWSMessageType.NEUTRAL);

                default -> Constants.getLogger().warn("Unknown packet command -> " + waypointPayload.command());
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
            sendUserAlert(Component.translatable("warning.jmws.server.no_version"), true, false, JMWSMessageType.FAILURE);
        } else if (serverVersion < Constants.SERVER_VERSION) {
            sendUserAlert(Component.translatable("warning.jmws.server.older_server_version"), true, false, JMWSMessageType.WARNING);
            Constants.getLogger().warn("Got server version; %s expected; %s".formatted(serverVersion, Constants.SERVER_VERSION));
        } else if (serverVersion > Constants.SERVER_VERSION) {
            sendUserAlert(Component.translatable("warning.jmws.server.newer_server_version"), true, false, JMWSMessageType.WARNING);
            Constants.getLogger().warn("Got server version; %s expected; %s".formatted(serverVersion, Constants.SERVER_VERSION));

        } else if (!CommonClass.serverConfig.jmwsEnabled) {
            sendUserAlert(Component.translatable("warning.jmws.server.disabled_jmws"), true, false, JMWSMessageType.WARNING);
        } else if (!CommonClass.serverConfig.waypointsEnabled) {
            sendUserAlert(Component.translatable("warning.jmws.server.disabled_waypoint"), true, false, JMWSMessageType.WARNING);
        } else if (!CommonClass.serverConfig.groupsEnabled) {
            sendUserAlert(Component.translatable("warning.jmws.server.disabled_group"), true, false, JMWSMessageType.WARNING);
        } else {
            sendUserAlert(Component.translatable("message.jmws.has_jmws", (CommonClass.serverConfig.getServerVersion())), true, false, JMWSMessageType.SUCCESS);
        }
    }

    /**
     * Handles handshake from the server.
     * @param handshakePayload -- Handshake packet from the server.
     */
    public static void HandshakeHandler(JMWSHandshakePayload handshakePayload) {
        CommonClass.serverConfig = handshakePayload.serverConfigData != null ? handshakePayload.serverConfigData : ClientSideServerConfigObject.serverOwner(); // Use serverOwner if on LAN, serverConfigData will be null if so (Because there is no physical server), so instantiate our own fake config just so shit don't crash.
        @Nullable Double serverVersion =  CommonClass.serverConfig.getServerVersion();
        CommonClass.setServerModStatus(true); // We have JMWS on server side
        sendUserJoinAlert(serverVersion);
    }
}
