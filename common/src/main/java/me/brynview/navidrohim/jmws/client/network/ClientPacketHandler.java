package me.brynview.navidrohim.jmws.client.network;

import com.google.gson.JsonElement;
import commonnetwork.networking.data.PacketContext;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.client.config.ConfigInterface;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncInformation;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncUtils;
import me.brynview.navidrohim.jmws.client.syncing.SyncRegistry;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.factory.ClientObjectFactory;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientGroupWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.assets.JMWSSounds;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
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
        JMWSClientCommon.isBusy = true;

        // Check if command should be processed (must be a client of a server)
        if (ConfigInterface.getEnabledStatus()) {

            if (JMWSCommon.minecraftClientInstance.player == null)
            {
                return;
            }

            switch (waypointPayload.command()) {

                // Was creation_response
                // Sends no outbound data
                case SYNC -> JMWSPlugin.syncHandler(waypointPayload);

                // was "alert"
                // No outbound data
                // This might be useless. Found out recently there is a way to do this with vanilla code without defining a custom packet.
                case CLIENT_ALERT -> {
                    String key = arguments.get(0).getAsString();
                    boolean overlay = arguments.get(1).getAsBoolean();
                    MessageType messageType = MessageType.valueOf(arguments.get(2).getAsString());

                    Component message;
                    if (arguments.size() > 3) {
                        int size = arguments.get(3).getAsJsonArray().size();
                        String[] transArgs = new String[size];
                        for (int i = 0; i < size; i++) {
                            transArgs[i] = arguments.get(3).getAsJsonArray().get(i).getAsString();
                        }
                        message = Component.translatable(key, (Object[]) transArgs);
                    } else {
                        message = Component.translatable(key);
                    }

                    if (messageType.equals(MessageType.FAILURE)) {
                        PlayerUtils.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
                    }

                    sendUserAlert(message, overlay, false, messageType);
                }

                // was "deleteWaypoint"
                // No outbound data
                case COMMON_DELETE_WAYPOINT ->
                {
                    String waypointIdentifier = waypointPayload.arguments().getFirst().getAsString();
                    boolean silent = arguments.get(1).getAsBoolean();

                    JMWSPlugin.getInstance().deleteSavedObjects(
                        silent,
                        ClientWaypointWrapper.class,
                        waypointIdentifier
                    );
                }
                case COMMON_DELETE_GROUP ->
                {
                    String groupIdentifier = waypointPayload.arguments().getFirst().getAsString();
                    boolean silent = arguments.get(2).getAsBoolean();

                    JMWSPlugin.getInstance().deleteSavedObjects(
                            silent,
                            ClientGroupWrapper.class,
                            groupIdentifier
                    );
                }

                case SPECIAL_FORWARD_TO_CLIENT ->
                {

                    Constants.getLogger().info(arguments.toString());
                    UUID senderUUID = UUID.fromString(arguments.getFirst().getAsString());
                    CommandFactory.PeerToPeerCommand sentCommand = CommandFactory.PeerToPeerCommand.valueOf(arguments.get(1).getAsString());
                    List<JsonElement> argumentsForClient = arguments.subList(2, arguments.size());

                    Constants.LoggerHolder.debug(argumentsForClient, "ARGS FOR CLIENT");
                    ClientPacketHandler.handlePeerToPeerPacket(sentCommand, senderUUID, argumentsForClient.getFirst().getAsJsonArray().asList());
                }

                case AFFIRM_SHARE ->
                {
                    ClientSyncInformation syncInfo = ClientSyncUtils.syncInformationFromString(arguments.getFirst().getAsString());

                    if (syncInfo != null && JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(syncInfo.owner))
                    {
                        OutgoingShareRequest request = JMWSClientCommon.outgoingShareRequests.getRequest(syncInfo.owner).resolve();
                        request.currentSharedObject.addSharedTo(syncInfo.owner);

                        sendUserAlert(Component.translatable("sharing.jmws.sharing_host", request.objectDisplayName, request.getRecipientName()), true, false, MessageType.SUCCESS);
                    } else {
                        sendUserAlert(Component.translatable("sharing.jmws.no_longer_valid"), true, true, MessageType.SUCCESS);
                    }
                }
                default -> Constants.getLogger().warn("Unknown packet command -> {} ", waypointPayload.command);
             }
        }
        JMWSClientCommon.isBusy = false;
    }

    private static void handlePeerToPeerPacket(CommandFactory.PeerToPeerCommand sentCommand, UUID senderUUID, List<JsonElement> argumentsForClient)
    {
        argumentsForClient = argumentsForClient.get(2).getAsJsonArray().asList();
        switch (sentCommand)
        {
            case CommandFactory.PeerToPeerCommand.CLIENT_SHARE_REQUEST ->
            {
                String data = argumentsForClient.getFirst().getAsString();
                Optional<SyncRegistry> possibleType = SyncRegistry.of(argumentsForClient.get(1).getAsString());

                if (possibleType.isPresent())
                {
                    SyncRegistry type = possibleType.get();
                    ClientObjectWrapper<?> objectWrapper = ClientObjectFactory.fromType(type, data);

                    if (!JMWSClientCommon.config.enableSharing.get())
                    {
                        ShareRequest.disabled(senderUUID);
                    }
                    else if (!JMWSClientCommon.incomingShareRequests.hasShareRequestFrom(senderUUID))
                    {
                        ShareRequest request = new ShareRequest(senderUUID, PlayerUtils.ourUUID(), objectWrapper);
                        JMWSClientCommon.incomingShareRequests.addRequest(senderUUID, request);

                        sendUserAlert(Component.translatable("sharing.jmws.share_request", request.getSenderName()), false, true, MessageType.SUCCESS);
                    } else {
                        ShareRequest.busy(senderUUID);
                    }
                } else {
                    sendUserAlert(Component.translatable("sharing.jmws.invalid_type"), true, true, MessageType.FAILURE);
                }
            }

            case CLIENT_REJECTED_SHARE_WITH_REASON ->
            {
                String declineMessage = argumentsForClient.getFirst().getAsString();
                @Nullable OutgoingShareRequest request = JMWSClientCommon.outgoingShareRequests.getRequest(senderUUID);

                if (request != null)
                {
                    request.resolve();
                    sendUserAlert(Component.translatable(declineMessage, request.getRecipientName()), true, false, MessageType.FAILURE);
                }
            }

            default -> Constants.getLogger().warn("Unknown peer to peer command -> {} ", sentCommand);
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
            sendUserAlert(Component.translatable("warning.jmws.server.no_version"), true, false, MessageType.FAILURE);
        } else if (serverVersion < Constants.SERVER_VERSION) {
            sendUserAlert(Component.translatable("warning.jmws.server.older_server_version"), true, false, MessageType.WARNING);
            Constants.getLogger().warn("Got server version; %s expected; %s".formatted(serverVersion, Constants.SERVER_VERSION));
        } else if (serverVersion > Constants.SERVER_VERSION) {
            sendUserAlert(Component.translatable("warning.jmws.server.newer_server_version"), true, false, MessageType.WARNING);
            Constants.getLogger().warn("Got server version; %s expected; %s".formatted(serverVersion, Constants.SERVER_VERSION));

        } else if (!JMWSClientCommon.serverConfig.jmwsEnabled) {
            sendUserAlert(Component.translatable("warning.jmws.server.disabled_jmws"), true, false, MessageType.WARNING);
        } else if (!JMWSClientCommon.serverConfig.waypointsEnabled) {
            sendUserAlert(Component.translatable("warning.jmws.server.disabled_waypoint"), true, false, MessageType.WARNING);
        } else if (!JMWSClientCommon.serverConfig.groupsEnabled) {
            sendUserAlert(Component.translatable("warning.jmws.server.disabled_group"), true, false, MessageType.WARNING);
        } else {
            sendUserAlert(Component.translatable("message.jmws.has_jmws", (JMWSClientCommon.serverConfig.getServerVersion())), true, false, MessageType.SUCCESS);
        }
    }

    /**
     * Handles handshake from the server.
     * @param handshakePayload -- Handshake packet from the server.
     */
    public static void handleHandshake(JMWSHandshakePayload handshakePayload) {
        JMWSClientCommon.serverConfig = handshakePayload.serverConfigData != null ? handshakePayload.serverConfigData : ClientSideServerConfigObject.serverOwner(); // Use serverOwner if on LAN, serverConfigData will be null if so (Because there is no physical server), so instantiate our own fake config just so shit don't crash.
        @Nullable Double serverVersion =  JMWSClientCommon.serverConfig.getServerVersion();
        JMWSClientCommon.setServerModStatus(true); // We have JMWS on server side
        sendUserJoinAlert(serverVersion);

        if (JMWSClientCommon.isMapping)
        {
            JMWSPlugin.sync(false);
        } else {
            JMWSClientCommon.didHandshake = true;
        }
    }
}
