package me.brynview.navidrohim.jmws.common.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.plugin.IClientPluginJM;
import me.brynview.navidrohim.jmws.common.SavedGroup;
import me.brynview.navidrohim.jmws.common.SavedWaypoint;
import me.brynview.navidrohim.jmws.common.enums.WaypointPayloadCommand;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class PacketCommandRunnables {
    Map<WaypointPayloadCommand, Runnable> commandHandlers = Map.of(
            SYNC, () -> handleSync(waypointPayload),
            REQUEST_CLIENT_SYNC, IClientPluginJM::updateWaypoints,
            COMMON_DISPLAY_INTERVAL, () -> sendUserAlert("Waypoints updated every " + INSTANCE.tickCounterUpdateThreshold / 20 + " seconds.", true, false),
            CLIENT_ALERT, () -> handleClientAlert(waypointPayload),
            COMMON_DELETE_WAYPOINT, () -> handleDeleteWaypoint(waypointPayload),
            COMMON_DISPLAY_NEXT_UPDATE, () -> sendUserAlert("Next waypoint update in " + (INSTANCE.tickCounterUpdateThreshold - INSTANCE.tickCounter) / 20 + " second(s)", true, false)
    );

    // Helper for handleUploadWaypoints
    private static Set<SavedWaypoint> getSavedWaypoints(JsonObject jsonData, UUID playerUUID) throws JsonSyntaxException, IllegalStateException {
        Set<SavedWaypoint> waypoints = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            JsonObject json = JsonParser.parseString(entry.getValue().getAsString()).getAsJsonObject();
            waypoints.add(new SavedWaypoint(json, playerUUID));
        }

        return waypoints;

    }

    // Helper for handleUploadGroups
    private static Set<SavedGroup> getSavedGroups(JsonObject jsonData) throws JsonSyntaxException, IllegalStateException {
        Set<SavedGroup> groups = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            JsonObject json = JsonParser.parseString(entry.getValue().getAsString()).getAsJsonObject();
            groups.add(new SavedGroup(json));
        }

        return groups;

    }

    // Helper for sync, handleUploadWaypoints is basically the same but different type annotations. I should've used generics
    private static boolean handleUploadGroups(IClientAPI instance, JsonObject jsonGroupsRaw, ClientPlayNetworking.Context context) {
        boolean hasLocalGroup = false;

        // Get existing groups (local) and get group objects saved on server
        List<? extends WaypointGroup> existingGroups = instance.getAllWaypointGroups();
        Set<SavedGroup> savedGroups = getSavedGroups(jsonGroupsRaw.deepCopy());

        // Get an identifier of every group, used to detect if the group already exists
        Set<String> remoteGroupKeys = savedGroups.stream()
                .map(g -> g.getName() + g.getGroupIdentifier())
                .collect(Collectors.toSet());

        // Test if any existing groups (persistent) have already been added to the server, if not, add them
        for (WaypointGroup existingGroup : existingGroups) {
            String key = existingGroup.getName() + existingGroup.getGuid();
            if (!remoteGroupKeys.contains(key) && !getInstance().forbiddenGroups.contains(existingGroup.getGuid())) {
                getInstance().groupCreationHandler(existingGroup, context.player(), true);
                hasLocalGroup = true;
            }
        }

        // Add server groups to the client
        for (SavedGroup savedGroup : savedGroups) {
            WaypointGroup group = WaypointFactory.fromGroupJsonString(savedGroup.getRawPacketData());
            getInstance().groupIdentifierMap.put(savedGroup.getUniversalIdentifier(), group);
            getInstance().jmAPI.addWaypointGroup(group);
        }

        // return this because need to give an alert
        return hasLocalGroup;
    }

    // Helper for sync but for waypoints
    private boolean handleUploadWaypoints(JsonObject jsonWaypoints, ClientPlayNetworking.Context context) throws JsonSyntaxException, IllegalStateException {
        boolean hasLocalWaypoint = false;

        // Get existing waypoints (local) and get waypoint objects saved on server
        List<? extends Waypoint> existingWaypoints = getInstance().jmAPI.getAllWaypoints();
        Set<SavedWaypoint> savedWaypoints = IClientPluginJM.getSavedWaypoints(jsonWaypoints.deepCopy(), context.player().getUuid());

        // Get an identifier of every waypoint (BlockPos, location), used to detect if the waypoint already exists
        Set<BlockPos> remoteWaypointPositions = savedWaypoints.stream()
                .map(w -> new BlockPos(w.getWaypointX(), w.getWaypointY(), w.getWaypointZ()))
                .collect(Collectors.toSet());

        getInstance().jmAPI.removeAllWaypoints("journeymap");

        // Test if any existing waypoints (persistent, usually death waypoints) have already been added to the server, if not, add them
        for (Waypoint existing : existingWaypoints) {
            if (!remoteWaypointPositions.contains(existing.getBlockPos())) {
                getInstance().createAction(existing, context.player(), true);
                hasLocalWaypoint = true;
            }
        }

        // Add server waypoints to the client
        for (SavedWaypoint savedWaypoint : savedWaypoints) {
            Waypoint wp = WaypointFactory.fromWaypointJsonString(savedWaypoint.getRawPacketData());
            getInstance().waypointIdentifierMap.put(savedWaypoint.getUniversalIdentifier(), wp);
            getInstance().jmAPI.addWaypoint("journeymap", wp);
        }

        return hasLocalWaypoint;
    }
}
