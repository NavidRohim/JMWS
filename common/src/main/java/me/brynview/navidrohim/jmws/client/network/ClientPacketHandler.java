package me.brynview.navidrohim.jmws.client.network;

import com.google.gson.JsonElement;
import commonnetwork.networking.data.PacketContext;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.ClientCommonClass;
import me.brynview.navidrohim.jmws.client.SyncCounter;
import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.client.config.ConfigInterface;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.share.OutgoingShareRequests;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.assets.JMWSSounds;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.enums.ShareRequestDirection;
import me.brynview.navidrohim.jmws.common.syncing.Syncing;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static me.brynview.navidrohim.jmws.client.utils.PlayerUtils.sendUserAlert;

/**
 * Collection of static methods relating to packet handling on the client side.
 */
public class ClientPacketHandler {

    /**
     * Handles a packet sent from the server.
     * @param Context -- The packet from the server, containing a command for the client to execute. See WaypointPayloadCommand class for possible commands, though not all will be for client.
     */
    public static void handlePacket(PacketContext<JMWSActionPayload> Context) {
        JMWSActionPayload waypointPayload = Context.message();
        List<JsonElement> arguments = waypointPayload.arguments();
        ClientCommonClass.isBusy = true;

        // Check if command should be processed (must be a client of a server)
        if (ConfigInterface.getEnabledStatus()) {

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
                case REQUEST_CLIENT_SYNC -> JMWSPlugin.sync(true);

                // was display_interval
                // No outbound data
                case COMMON_DISPLAY_INTERVAL -> sendUserAlert(Component.translatable("message.jmws.sync_frequency", SyncCounter.getSyncFrequency()), true, false, MessageType.NEUTRAL);

                // was "alert"
                // No outbound data
                // This might be useless. Found out recently there is a way to do this with vanilla code without defining a custom packet.
                case CLIENT_ALERT -> {
                    String firstArgument = waypointPayload.arguments().getFirst().getAsString();
                    MessageType messageType = MessageType.valueOf(waypointPayload.arguments().getLast().getAsString());

                    if (messageType.equals(MessageType.FAILURE)) {
                        PlayerUtils.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                    }

                    sendUserAlert(Component.translatable(firstArgument), waypointPayload.arguments().get(1).getAsBoolean(), false, messageType);
                }

                // was "deleteWaypoint"
                // No outbound data
                case COMMON_DELETE_WAYPOINT ->
                {
                    String waypointIdentifier = waypointPayload.arguments().getFirst().getAsString();
                    boolean silent = arguments.get(1).getAsBoolean();

                    JMWSPlugin.getInstance().deleteSavedObjects(
                        silent,
                        ObjectType.WAYPOINT,
                        waypointIdentifier
                    );
                }
                case COMMON_DELETE_GROUP ->
                {
                    String groupIdentifier = waypointPayload.arguments().getFirst().getAsString();
                    boolean silent = arguments.get(2).getAsBoolean();

                    JMWSPlugin.getInstance().deleteSavedObjects(
                            silent,
                            ObjectType.GROUP,
                            groupIdentifier
                    );
                }

                // was "display_next_update"
                // No outbound data
                case COMMON_DISPLAY_NEXT_UPDATE -> sendUserAlert(Component.translatable("message.jmws.next_sync", (SyncCounter.timeUntilNextSync())), true, false, MessageType.NEUTRAL);

                case OBJECT_SHARE ->
                {
                    ShareRequestDirection direction = ShareRequestDirection.valueOf(arguments.getLast().getAsString());

                    ObjectType sharedObjectType = ObjectType.valueOf(arguments.get(3).getAsString());
                    String objectString = arguments.getFirst().getAsString();

                    Object object;
                    String objName;
                    String objectIdentifier;

                    if (sharedObjectType == ObjectType.WAYPOINT)
                    {
                        Waypoint objectWp = WaypointFactory.fromWaypointJsonString(objectString);
                        objectIdentifier = Syncing.getSyncingInfo(objectWp.getCustomData(Constants.MODID)).objectIdentifier;
                        object = objectWp;
                        objName = objectWp.getName();
                    } else {
                        WaypointGroup objectGp = WaypointFactory.fromGroupJsonString(objectString);
                        objectIdentifier = Syncing.getSyncingInfo(objectGp.getCustomData(Constants.MODID)).objectIdentifier;
                        object = objectGp;
                        objName = objectGp.getName();
                    }

                    if (direction.equals(ShareRequestDirection.FOR_CLIENT))
                    {
                        UUID sender = UUID.fromString(arguments.get(1).getAsString());
                        if (!ClientCommonClass.config.enableSharing.get())
                        {
                            ShareRequest.disabled(sender);
                        }
                        else if (!IncomingShareRequests.hasShareRequestFrom(sender))
                        {
                            ShareRequest request = new ShareRequest(
                                    sender,
                                    PlayerUtils.ourUUID(),
                                    object,
                                    sharedObjectType,
                                    objectIdentifier,
                                    objName
                            );

                            IncomingShareRequests.addRequest(sender, request);
                            sendUserAlert(Component.translatable("sharing.jmws.share_request", request.getSenderName()), false, true, MessageType.SUCCESS);
                        } else {
                            ShareRequest.busy(sender);
                        }
                    } else {
                        UUID incoming = UUID.fromString(arguments.get(1).getAsString());
                        OutgoingShareRequests.addRequest(incoming, new OutgoingShareRequest(PlayerUtils.ourUUID(), incoming, object, sharedObjectType, objectIdentifier, objName));
                        sendUserAlert(Component.translatable("sharing.jmws.share_sent"), true, false, MessageType.SUCCESS);
                    }
                }

                case REJECT_SHARE ->
                {
                    UUID incoming = UUID.fromString(arguments.get(1).getAsString());
                    @Nullable OutgoingShareRequest request = OutgoingShareRequests.getRequest(incoming);

                    if (request != null)
                    {
                        request.resolve();
                        sendUserAlert(Component.translatable("sharing.jmws.share_rejected", request.getRecipientName()), true, false, MessageType.FAILURE);
                    }
                }

                case USER_ALREADY_PROCESSING_SHARE ->
                {
                    UUID incoming = UUID.fromString(arguments.get(1).getAsString());
                    String declineMessage = arguments.getLast().getAsString();
                    @Nullable OutgoingShareRequest request = OutgoingShareRequests.getRequest(incoming);

                    if (request != null)
                    {
                        request.resolve();
                        sendUserAlert(Component.translatable(declineMessage, request.getRecipientName()), true, false, MessageType.WARNING);
                    }

                }

                case AFFIRM_SHARE ->
                {
                    UUID incoming = UUID.fromString(arguments.getLast().getAsString());
                    if (OutgoingShareRequests.hasShareRequestFor(incoming))
                    {
                        OutgoingShareRequest request = OutgoingShareRequests.getRequest(incoming).resolve();
                        sendUserAlert(Component.translatable("sharing.jmws.sharing_host", request.objectDisplayName, request.getRecipientName()), true, false, MessageType.SUCCESS);
                    } else {
                        sendUserAlert(Component.translatable("sharing.jmws.no_longer_valid"), true, true, MessageType.SUCCESS);
                    }
                }
                
                default -> Constants.getLogger().warn("Unknown packet command -> " + waypointPayload.command());
             }
        }
        ClientCommonClass.isBusy = false;
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
            sendUserAlert(Component.translatable("warning.jmws.server.no_version"), true, false, MessageType.FAILURE);
        } else if (serverVersion < Constants.SERVER_VERSION) {
            sendUserAlert(Component.translatable("warning.jmws.server.older_server_version"), true, false, MessageType.WARNING);
            Constants.getLogger().warn("Got server version; %s expected; %s".formatted(serverVersion, Constants.SERVER_VERSION));
        } else if (serverVersion > Constants.SERVER_VERSION) {
            sendUserAlert(Component.translatable("warning.jmws.server.newer_server_version"), true, false, MessageType.WARNING);
            Constants.getLogger().warn("Got server version; %s expected; %s".formatted(serverVersion, Constants.SERVER_VERSION));

        } else if (!ClientCommonClass.serverConfig.jmwsEnabled) {
            sendUserAlert(Component.translatable("warning.jmws.server.disabled_jmws"), true, false, MessageType.WARNING);
        } else if (!ClientCommonClass.serverConfig.waypointsEnabled) {
            sendUserAlert(Component.translatable("warning.jmws.server.disabled_waypoint"), true, false, MessageType.WARNING);
        } else if (!ClientCommonClass.serverConfig.groupsEnabled) {
            sendUserAlert(Component.translatable("warning.jmws.server.disabled_group"), true, false, MessageType.WARNING);
        } else {
            sendUserAlert(Component.translatable("message.jmws.has_jmws", (ClientCommonClass.serverConfig.getServerVersion())), true, false, MessageType.SUCCESS);
        }
    }

    /**
     * Handles handshake from the server.
     * @param handshakePayload -- Handshake packet from the server.
     */
    public static void handleHandshake(JMWSHandshakePayload handshakePayload) {
        ClientCommonClass.serverConfig = handshakePayload.serverConfigData != null ? handshakePayload.serverConfigData : ClientSideServerConfigObject.serverOwner(); // Use serverOwner if on LAN, serverConfigData will be null if so (Because there is no physical server), so instantiate our own fake config just so shit don't crash.
        @Nullable Double serverVersion =  ClientCommonClass.serverConfig.getServerVersion();
        ClientCommonClass.setServerModStatus(true); // We have JMWS on server side
        sendUserJoinAlert(serverVersion);

        if (ClientCommonClass.isMapping)
        {
            JMWSPlugin.sync(false);
        } else {
            ClientCommonClass.didHandshake = true;
        }
    }
}
