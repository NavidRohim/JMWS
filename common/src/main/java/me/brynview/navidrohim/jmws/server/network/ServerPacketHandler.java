package me.brynview.navidrohim.jmws.server.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import commonnetwork.api.Dispatcher;
import commonnetwork.networking.data.PacketContext;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.api.ServerSyncInformation;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.server.JMWSServerCommon;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistryEntry;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import me.brynview.navidrohim.jmws.server.objects.LegacyObject;
import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.io.UserSharingFile;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper.sendUserMessage;

public class ServerPacketHandler {

    private static boolean serverEnabledJMWS() {
        return ServerConfig.getConfig().jmwsEnabled && (ServerConfig.getConfig().groupsEnabled || ServerConfig.getConfig().waypointsEnabled);
    }

    private static void debugLogCorruptPacket(CommandFactory.Commands command, UUID playerUUID, List<JsonElement> arguments, Exception error)
    {
        Constants.getLogger().debug("Got corrupt packet command %s for user %s data following\n\nCommand: %s\nArguments: %s\nException: %s".formatted(command, playerUUID, command, arguments.toString(), error.getMessage()));
    }

    public static void sendUserSync(ServerPlayer player, boolean sendAlert, boolean isDeathSync, boolean onlySyncShared)
    {
        if (ServerConfig.getConfig().serverEnabled())
        {
            UUID playerUUID = player.getUUID();
            try {
                int lastIterWp = 0;
                int lastIterGp = 0;
                Constants.getLogger().info(ServerSyncRegistry.WAYPOINT.toString());
                List<Path> playerWaypoints = JMWSServerIO.getObjectPathsForUser(playerUUID, ServerSyncRegistry.WAYPOINT);
                List<Path> playerGroups = JMWSServerIO.getObjectPathsForUser(playerUUID, ServerSyncRegistry.GROUP);

                HashMap<String, String> jsonWaypointPayloadArray = new HashMap<>();
                HashMap<String, String> jsonGroupPayloadArray = new HashMap<>();

                // User-defined objects
                if (!onlySyncShared)
                {
                    for (int i = 0 ; i < playerWaypoints.size() ; i++) {
                        Path waypointFilename = playerWaypoints.get(i);
                        String jsonWaypointFileString = Files.readString(waypointFilename);
                        jsonWaypointPayloadArray.put(String.valueOf(i), jsonWaypointFileString);
                        lastIterWp = i;
                    }

                    for (int ix = 0 ; ix < playerGroups.size() ; ix++) {
                        Path groupFilename = playerGroups.get(ix);
                        String jsonGroupFileString = Files.readString(groupFilename);
                        jsonGroupPayloadArray.put(String.valueOf(ix), jsonGroupFileString);
                        lastIterGp = ix;
                    }
                }

                for (Path globalWpPath : JMWSServerIO.getGlobalObjects(ServerSyncRegistry.WAYPOINT))
                {
                    lastIterWp++;
                    jsonWaypointPayloadArray.put(String.valueOf(lastIterWp), Files.readString(globalWpPath));
                }

                for (Path globalGpPath : JMWSServerIO.getGlobalObjects(ServerSyncRegistry.GROUP))
                {
                    lastIterGp++;
                    jsonGroupPayloadArray.put(String.valueOf(lastIterGp), Files.readString(globalGpPath));
                }

                // Shared objects
                if (ServerConfig.serverConfig.sharingEnabled)
                {
                    try (UserSharingFile userSharingFile = new UserSharingFile(playerUUID))
                    {
                        for (String shared : userSharingFile.getSharedList(ServerSyncRegistry.WAYPOINT))
                        {
                            lastIterWp++;
                            ServerWaypoint wp = ServerWaypoint.getWaypointFromUniqueIdentifier(shared, playerUUID);
                            if (wp != null)
                            {
                                if (!wp.serverSyncingHandler.isGlobal())
                                {
                                    jsonWaypointPayloadArray.put(String.valueOf(lastIterWp), wp.getRawString());
                                }
                            }
                            else {
                                userSharingFile.removeFromShared(shared, ServerSyncRegistry.WAYPOINT);
                            }
                        }
                        for (String sharedGpString : userSharingFile.getSharedList(ServerSyncRegistry.GROUP))
                        {
                            lastIterGp++;
                            ServerGroup gp = ServerGroup.getGroupFromUniqueIdentifier(sharedGpString, playerUUID);
                            if (gp != null)
                            {
                                if (!gp.serverSyncingHandler.isGlobal())
                                {
                                    jsonGroupPayloadArray.put(String.valueOf(lastIterGp), gp.getRawString());
                                }
                            } else {
                                userSharingFile.removeFromShared(sharedGpString, ServerSyncRegistry.GROUP);
                            }
                        }
                    }
                }

                String jsonData = CommandFactory.makeSyncRequestResponseJson(jsonWaypointPayloadArray, jsonGroupPayloadArray, sendAlert, isDeathSync);
                if (jsonData.getBytes().length >= 2000000) { // packet size limit, I tried to reach this limit, but I got nowhere near.
                    sendUserMessage(player, "error.jmws.error_packet_size", false, true);
                } else {
                    JMWSActionPayload waypointPayloadOutbound = new JMWSActionPayload(jsonData);
                    Dispatcher.sendToClient(waypointPayloadOutbound, player);
                }
            } catch (IOException ioe) {
                Constants.getLogger().error("Error on server when trying to process sync from {} ERROR: {}", player.getUUID(), ioe.toString());
            }
        }
    }

    public static void handleIncomingActionCommand(PacketContext<JMWSActionPayload> Context, @Nullable ServerPlayer player) {

        if (player == null)
        {
            Constants.getLogger().debug("Trashing packet as it's context is no longer valid. Cause is unknown but could be mod incompatibility or lag.\nDead packet = %s".formatted(Context.message().command()));
            return;
        }

        JMWSActionPayload waypointActionPayload = Context.message();
        CommandFactory.Commands command = waypointActionPayload.command();
        List<JsonElement> arguments = waypointActionPayload.arguments();
        UUID playerUUID = player.getGameProfile().id();

        try
        {
            switch (command) {

                // Following two cases are for deleting waypoints and groups
                case CommandFactory.Commands.COMMON_DELETE_GROUP -> {

                    String groupUniversalIdentifier = arguments.getFirst().getAsString();
                    String groupGUID = arguments.get(1).getAsString();

                    boolean silent = arguments.get(2).getAsBoolean();
                    boolean deleteAllWaypointsInGroup = arguments.get(3).getAsBoolean();
                    boolean removeGroupItself = arguments.get(4).getAsBoolean();
                    boolean isObjGlobal = arguments.get(5).getAsBoolean();
                    boolean deleteAllObjects = arguments.getLast().getAsBoolean();

                    boolean result;
                    @Nullable ServerGroup group = ServerGroup.getGroupFromUniqueIdentifier(groupUniversalIdentifier, playerUUID);

                    if (group != null)
                    {
                        if (group.serverSyncingHandler.isOwner(playerUUID))
                        {
                            if (deleteAllWaypointsInGroup)
                            {
                                result = group.deleteWaypoints();
                                Constants.LoggerHolder.debug(String.valueOf(result), "DELETE RESULT");
                                if (!removeGroupItself && result)
                                {
                                    sendUserMessage(player, "message.jmws.deleted_waypoints_in_group", true, false, silent);
                                    return;
                                }
                            }
                            group.stopSharingWithAll();
                            result = group.delete(false);
                            if (result) {
                                sendUserMessage(player, "message.jmws.deletion_group_success", true, false, silent);
                            } else {
                                sendUserMessage(player, "message.jmws.deletion_group_failure", true, true, silent);
                            }

                        } else if (group.serverSyncingHandler.isGlobal()) {
                            sendUserMessage(player, "global.jmws.cannot_delete_global", true, MessageType.ONE_TIME_WARNING);
                        } else {
                            group.stopSharingWith(playerUUID);
                            sendUserMessage(player, "sharing.jmws.no_longer_sharing", true, false);
                        }

                    } else if (deleteAllWaypointsInGroup)
                    {
                        ServerGroup.deleteWaypoints(playerUUID, groupGUID);
                        sendUserMessage(player, "message.jmws.deleted_waypoints_in_group", true, false);
                    } else if (deleteAllObjects)
                    {
                        if (ServerObject.deleteAll(playerUUID, ServerSyncRegistry.GROUP)) {
                            sendUserMessage(player, "message.jmws.deletion_group_success", true, false, silent);
                        } else {
                            sendUserMessage(player, "message.jmws.deletion_group_failure", true, true, silent);
                        }

                    }
                }

                case CommandFactory.Commands.COMMON_DELETE_WAYPOINT -> {

                    String waypointIdentifier = arguments.getFirst().getAsString().stripTrailing();
                    boolean silent = arguments.get(1).getAsBoolean();
                    boolean deleteAll = arguments.getLast().getAsBoolean();
                    boolean result;

                    ServerWaypoint waypoint = ServerWaypoint.getWaypointFromUniqueIdentifier(waypointIdentifier, playerUUID);

                    if (waypoint != null) {
                        if (waypoint.serverSyncingHandler.isOwner(playerUUID)) {
                            result = waypoint.delete(true);

                            if (!silent) {
                                if (result) {
                                    sendUserMessage(player, "message.jmws.deletion_success", true, false);
                                } else {
                                    sendUserMessage(player, "message.jmws.deletion_failure", true, true);
                                }
                            }
                        } else if (waypoint.serverSyncingHandler.isGlobal()) {
                            sendUserMessage(player, "global.jmws.cannot_delete_global", true, MessageType.ONE_TIME_WARNING);
                        } else {
                            waypoint.stopSharingWith(playerUUID);
                            sendUserMessage(player, "sharing.jmws.no_longer_sharing", true, false);
                        }
                    } else if (deleteAll)
                    {
                        if (ServerObject.deleteAll(playerUUID, ServerSyncRegistry.WAYPOINT))
                        {
                            sendUserMessage(player, "message.jmws.deletion_success", true, false);
                        } else {
                            sendUserMessage(player, "message.jmws.deletion_failure", true, true);
                        }
                    } else {
                        sendUserMessage(player, "message.jmws.deletion_failure", true, true);
                    }
                }

                // Following two cases regarding creating groups and waypoints
                case CommandFactory.Commands.SERVER_CREATE -> {
                    if (serverEnabledJMWS() && (ServerConfig.getConfig().waypointsEnabled)) {
                        JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                        boolean silent = arguments.get(1).getAsBoolean();
                        boolean waypointCreationSuccess = ServerWaypoint.createWaypoint(jsonCreationData, playerUUID);

                        if (!silent) {
                            if (waypointCreationSuccess) {
                                sendUserMessage(player, "message.jmws.creation_success", true, false);
                            } else {
                                sendUserMessage(player, "message.jmws.creation_failure", false, true);
                            }
                        }
                    } else {
                        sendUserMessage(player, "message.jmws.server_disabled_waypoints", true, true);
                    }
                }

                case CommandFactory.Commands.SERVER_CREATE_GROUP -> {

                    boolean _UNUSED_isUpdateFromCreation = arguments.get(2).getAsBoolean(); // This is kept for compatibility between versions. I should've removed it in 1.2.0
                    // Could technically remove and wouldn't change anything, just keeping it here to remind me the issue exists. Must keep it on the client side though.

                    if (serverEnabledJMWS() && ServerConfig.getConfig().groupsEnabled) {
                        JsonObject jsonCreationData = JsonParser.parseString(arguments.getFirst().getAsString()).getAsJsonObject();
                        boolean silent = arguments.get(1).getAsBoolean();
                        boolean waypointCreationSuccess = ServerGroup.createGroup(jsonCreationData, playerUUID);

                        if (!silent) {
                            if (waypointCreationSuccess) {
                                sendUserMessage(player, "message.jmws.creation_group_success", true, false);
                            } else {
                                sendUserMessage(player, "message.jmws.creation_group_failure", false, true);

                            }
                        }
                    } else {
                        sendUserMessage(player, "message.jmws.server_disabled_groups", true, true);
                    }
                }

                case UPDATE -> // Bug here; after updating, the user shareWith list is cleared
                {
                    String objectIdentifier = arguments.getFirst().getAsString();
                    ServerSyncRegistryEntry modifyingType = JMWSServerCommon.REGISTRY.getStrict(arguments.get(1).getAsString());
                    // boolean isGlobal = arguments.get(2).getAsBoolean();
                    String objectData = arguments.getLast().getAsString();

                    ServerObject obj = JMWSServerIO.getObjectFromUniqueIdentifier(objectIdentifier, playerUUID, modifyingType);

                    if (obj != null)
                    {
                        if (obj.serverSyncingHandler.isOwner(playerUUID))
                        {
                            obj.update(objectData, false);
                            obj.serverSyncingHandler.syncToUsers();

                            if (modifyingType == ServerSyncRegistry.WAYPOINT)
                            {

                                PlayerNetworkingHelper.sendUserMessage(player, "message.jmws.modified_waypoint_success", true, MessageType.NEUTRAL);
                            } else {
                                PlayerNetworkingHelper.sendUserMessage(player, "message.jmws.modified_group_success", true, MessageType.NEUTRAL);
                            }
                        } else {
                            sendUserMessage(player, "sharing.jmws.local_only", false, MessageType.ONE_TIME_WARNING);
                        }
                    }
                }

                // was "request"
                case CommandFactory.Commands.SYNC -> {
                    boolean sendAlert = arguments.get(2).getAsBoolean();
                    boolean isDeathSync = arguments.getLast().getAsBoolean();
                    sendUserSync(player, sendAlert, isDeathSync, false);
                }

                case CommandFactory.Commands.AFFIRM_SHARE ->
                {
                    String rawSyncInfo = arguments.getFirst().getAsString();
                    ServerSyncInformation syncInfo = ServerSyncInformation.getFromString(rawSyncInfo);

                    Constants.LoggerHolder.debug(syncInfo.toString(), "SYNC INFO");
                    Constants.LoggerHolder.debug(rawSyncInfo, "RAW SYNC INFO");
                    ServerObject sharedWp = JMWSServerIO.getObjectFromSyncInformation(syncInfo);

                    if (sharedWp != null)
                    {
                        try (UserSharingFile usf = new UserSharingFile(playerUUID))
                        {
                            usf.addToShared(syncInfo.objectIdentifier, syncInfo.syncRegistryType);
                        }
                        sharedWp.serverSyncingHandler.addUserToShare(playerUUID);

                        Dispatcher.sendToClient(waypointActionPayload, JMWSCommon.minecraftServerInstance.getPlayerList().getPlayer(syncInfo.owner));
                    } else {
                        sendUserMessage(player, "sharing.jmws.object_no_longer_exists", true, true);
                    }
                }

                case TRANSITION ->
                {
                    String objectID = arguments.getFirst().getAsString();
                    Path legacyObjPath = Path.of(arguments.get(1).getAsString());
                    ServerSyncRegistryEntry serverSyncRegistry = JMWSServerCommon.REGISTRY.getStrict(arguments.getLast().getAsString());

                    LegacyObject.transitionIfNeed(legacyObjPath, playerUUID, serverSyncRegistry);

                }

                case MAKE_GLOBAL ->
                {
                    UUID from = UUID.fromString(arguments.getFirst().getAsString());
                    String objectIdentifier = arguments.get(1).getAsString();
                    ServerSyncRegistryEntry serverSyncRegistry = JMWSServerCommon.REGISTRY.getStrict(arguments.get(2).getAsString());
                    boolean global = arguments.getLast().getAsBoolean();

                    ServerWaypoint globalObject = JMWSServerIO.getObjectFromUniqueIdentifier(objectIdentifier, from, serverSyncRegistry);
                    Constants.getLogger().info("Making global " + global);
                    Constants.getLogger().info("global " + globalObject);
                    if (globalObject != null)
                    {
                        if (global)
                        {
                            if (!globalObject.serverSyncingHandler.isGlobal())
                            {
                                globalObject.makeGlobal();
                                PlayerNetworkingHelper.sendUserMessage(from, "global.jmws.made_global", true, MessageType.NEUTRAL);
                            } else {
                                PlayerNetworkingHelper.sendUserMessage(from, "global.jmws.already_global", true, MessageType.WARNING);
                            }
                        } else {
                            if (globalObject.serverSyncingHandler.isGlobal())
                            {
                                globalObject.removeGlobal();
                                PlayerNetworkingHelper.sendUserMessage(from, "global.jmws.remove_global", true, MessageType.NEUTRAL);
                            } else {
                                PlayerNetworkingHelper.sendUserMessage(from, "global.jmws.not_global", true, MessageType.NEUTRAL);
                            }
                        }
                    }
                }

                case TRANSITION_NEW_DATA ->
                {
                    String legacyObjectIdentifier = arguments.getFirst().getAsString();
                    UUID legacyOwnerUUID =  UUID.fromString(arguments.get(1).getAsString());
                    boolean isGlobal = arguments.get(2).getAsBoolean();
                    ServerSyncRegistryEntry legacyServerSyncRegistry = JMWSServerCommon.REGISTRY.getStrict(arguments.getLast().getAsString());

                    JMWSServerIO.getObjectFromDisk(legacyObjectIdentifier, legacyOwnerUUID, legacyServerSyncRegistry, false, isGlobal);
                }

                case SPECIAL_FORWARD_TO_CLIENT ->
                {
                    CommandFactory.PeerToPeerCommand commandForClient = CommandFactory.PeerToPeerCommand.valueOf(arguments.getFirst().getAsString());
                    UUID toPlayer = UUID.fromString(arguments.get(1).getAsString());
                    @Nullable ServerPlayer toPlayerObject = JMWSCommon.minecraftServerInstance.getPlayerList().getPlayer(toPlayer);

                    if (toPlayerObject != null)
                    {
                        Dispatcher.sendToClient(new JMWSActionPayload(CommandFactory.makeBaseJsonRequest(CommandFactory.Commands.SPECIAL_FORWARD_TO_CLIENT, playerUUID, commandForClient, arguments)), toPlayerObject);
                    } else {
                        PlayerNetworkingHelper.sendUserMessage(player, "error.jmws.player_offline", true, MessageType.WARNING);
                    }
                }

                case SERVER_REMOVE_SHARE_WITH -> {
                    UUID with = UUID.fromString(arguments.getFirst().getAsString());
                    ServerSyncInformation syncInformation = ServerSyncInformation.getFromString(arguments.getLast().getAsString());

                    @Nullable ServerPlayer withPlayer = JMWSCommon.minecraftServerInstance.getPlayerList().getPlayer(with);

                    if (syncInformation.object != null) {
                        syncInformation.object.serverSyncingHandler.removeUserFromShare(with);
                        if (withPlayer != null)
                        {
                            sendUserSync(withPlayer, false, false, false);
                        }
                    }
                }

                default -> Constants.getLogger().warn("Unknown packet command -> {}", command);}

        } catch (UnsupportedOperationException error)
        {
            // Thrown if arguments cannot be parsed by Gson. Usually a corrupt packet, but ideally this should never be called as it is
            // Handled on the client. Other errors will just have a normal traceback.
            debugLogCorruptPacket(command, playerUUID, arguments, error);
        }
    }
}
