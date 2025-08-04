package me.brynview.navidrohim.jmws.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import commonnetwork.api.Dispatcher;
import commonnetwork.api.Network;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.common.event.CommonEventRegistry;
import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helpers.CommonHelper;
import me.brynview.navidrohim.jmws.client.helpers.JMWSSounds;
import me.brynview.navidrohim.jmws.client.objects.SavedGroup;
import me.brynview.navidrohim.jmws.client.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.helper.CommandHelper;
import me.brynview.navidrohim.jmws.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.io.CommonIO;
import me.brynview.navidrohim.jmws.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.stream.Collectors;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JMWSPlugin implements IClientPlugin {

    // Variables
    public static final Minecraft minecraftClientInstance = Minecraft.getInstance();

    // JourneyMap API
    private IClientAPI jmAPI = null;
    private static JMWSPlugin INSTANCE;

    // Counters
    public int tickCounterUpdateThreshold = 800;//config.clientConfiguration.updateWaypointFrequency();
    public int tickCounter = 0;

    // Config
    // tbd

    // Required functions

    @Override
    public void initialize(IClientAPI jmClientApi) {

        this.jmAPI = jmClientApi;
        CommonEventRegistry.WAYPOINT_EVENT.subscribe("jmapi", this::waypointCreationHandler);
    }

    @Override
    public String getModId() {
        return Constants.MODID;
    }

    public static JMWSPlugin getInstance() {
        return INSTANCE;
    }

    // Waypoint map
    // Why is this needed? Well, it might not be anymore. In the early stages of this mod, waypoints and groups could never be uniquely identified. So I made some really
    // needlessly complex solution that you see here now.

    private final HashMap<String, Waypoint> waypointIdentifierMap = new HashMap<>();
    private final HashMap<String, WaypointGroup> groupIdentifierMap = new HashMap<>();

    public Waypoint getOldWaypoint(Waypoint newWaypoint) {
        String persistentWaypointID = newWaypoint.getCustomData();
        return waypointIdentifierMap.get(persistentWaypointID);
    }


    // General helper functions
    // Methods for manipulating / creating waypoints on the server

    private void createAction(Waypoint waypoint, boolean silent, boolean isUpdate) {
        String waypointIdentifier = CommonHelper.makeWaypointHash(minecraftClientInstance.player.getUUID(), waypoint.getGuid(), waypoint.getName());
        waypointIdentifierMap.put(waypointIdentifier, waypoint);

        waypoint.setPersistent(false);
        waypoint.setCustomData(waypointIdentifier);

        String creationData = CommandHelper.makeCreationRequestJson(waypoint, silent, isUpdate);
        Network.getNetworkHandler().sendToServer(new JMWSActionPayload(creationData), false);
    }

    private void updateAction(Waypoint waypoint, Waypoint oldWaypoint)
    {
        if (oldWaypoint != null) {
            this.deleteAction(oldWaypoint, true);
            jmAPI.removeWaypoint("journeymap", oldWaypoint);
        }
        this.createAction(waypoint, true, true);

        PlayerHelper.sendUserAlert(Component.translatable("message.jmws.modified_waypoint_success"), true, false, JMWSMessageType.SUCCESS);
    }

    private void deleteAction(Waypoint waypoint, boolean silent) {

        String waypointFilename = CommonIO.getWaypointFilename(waypoint, minecraftClientInstance.player.getUUID());

        waypointIdentifierMap.remove(waypoint.getCustomData());
        String jsonPacketData = CommandHelper.makeDeleteRequestJson(waypointFilename, silent, false);

        jmAPI.removeWaypoint("journeymap", waypoint);
        // senmd
    }

    void waypointCreationHandler(WaypointEvent waypointEvent) {

        Waypoint oldWaypoint = this.getOldWaypoint(waypointEvent.waypoint);

        switch (waypointEvent.getContext()) {
            case CREATE ->
                // Sends "create" packet | new = "SERVER_CREATE"
                    this.createAction(waypointEvent.waypoint, false, false);
            case DELETED ->
                // Sends "delete" packet | new = "COMMON_SERVER_DELETE"
                    this.deleteAction(waypointEvent.waypoint, false);
            case UPDATE ->
                // Sends both "delete" and "create" packet in respective order and respective enums.
                    this.updateAction(waypointEvent.waypoint, oldWaypoint);
        }
    }

    public static void deleteAllGroups() {
        // This method is a bodge fix. removeWaypointGroups (which I believe removes all groups) does not work because you cannot change the modId of a group.

        for (WaypointGroup waypointGroup : getInstance().jmAPI.getAllWaypointGroups()) {
            if (Constants.forbiddenGroups.contains(waypointGroup.getGuid())) {
                getInstance().jmAPI.removeWaypointGroup(waypointGroup, false);
            }
        }
    }

    // Handling packets
    public void deleteSavedObjects(Boolean deleteAll, JMWSServerIO.FetchType deletionType, String toDelete)
    {
        String deletionMessageConfirmationKey = "message.jmws.deletion_all_success";

        if (deletionType == JMWSServerIO.FetchType.WAYPOINT) {
            if (deleteAll) {
                INSTANCE.jmAPI.removeAllWaypoints("journeymap");
            } else {
                INSTANCE.jmAPI.removeWaypoint("journeymap", getInstance().waypointIdentifierMap.get(toDelete));
            }

        } else {
            deletionMessageConfirmationKey = "message.jmws.deletion_group_all_success";
            if (deleteAll) {
                JMWSPlugin.deleteAllGroups();
            } else {
                JMWSPlugin.getInstance().jmAPI.removeWaypointGroup(JMWSPlugin.getInstance().groupIdentifierMap.get(toDelete), false);
            }
        }
        PlayerHelper.sendUserAlert(Component.translatable(deletionMessageConfirmationKey), true, false, JMWSMessageType.NEUTRAL);
    }

    public static void updateWaypoints(boolean sendAlert) { // Might use delay some day

        // Sends "request" packet | New = "SYNC"
        // true was getInstance().getEnabledStatus()
        if (true) {
            Dispatcher.sendToServer(new JMWSActionPayload(CommandHelper.makeWaypointSyncRequestJson(sendAlert)));
        }

    }

    // Syncing -- Funcions for syncing waypoints and groups

    private void groupCreationHandler(WaypointGroup waypointGroup, LocalPlayer player, boolean silent, boolean isUpdate)
    {
        String waypointIdentifier = CommonHelper.makeWaypointHash(player.getUUID(), waypointGroup.getGuid(), waypointGroup.getName());

        // this is needed because for some reason, when creating a group in the wp creation dialogue box, the CREATION event fires twice.
        if (groupIdentifierMap.containsKey(waypointIdentifier)) { return; }
        groupIdentifierMap.put(waypointIdentifier, waypointGroup);

        waypointGroup.setPersistent(false);
        waypointGroup.setCustomData(waypointIdentifier);
        String creationData = CommandHelper.makeGroupCreationRequestJson(waypointGroup, silent, isUpdate);

        Dispatcher.sendToServer(new JMWSActionPayload(creationData));
    }

    // Helper for handleUploadWaypoints
    private static Set<SavedWaypoint> getSavedWaypoints(JsonObject jsonData, UUID playerUUID) throws JsonSyntaxException, IllegalStateException {
        Set<SavedWaypoint> waypoints = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            JsonObject json = JsonParser.parseString(entry.getValue().getAsString()).getAsJsonObject();
            waypoints.add(new SavedWaypoint(json, playerUUID));
        }

        return waypoints;

    }

    private static Set<SavedGroup> getSavedGroups(JsonObject jsonData) throws JsonSyntaxException, IllegalStateException {
        Set<SavedGroup> groups = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            JsonObject json = JsonParser.parseString(entry.getValue().getAsString()).getAsJsonObject();
            groups.add(new SavedGroup(json));
        }

        return groups;

    }

    // Helper for sync, handleUploadWaypoints is basically the same but different type annotations. I should've used generics
    private boolean handleUploadGroups(JsonObject jsonGroupsRaw, LocalPlayer player) throws JsonSyntaxException, IllegalStateException {
        boolean hasLocalGroup = false;

        // Get existing groups (local) and get group objects saved on server
        List<? extends WaypointGroup> existingGroups = getInstance().jmAPI.getAllWaypointGroups();
        Set<SavedGroup> savedGroups = JMWSPlugin.getSavedGroups(jsonGroupsRaw.deepCopy());

        // Get an identifier of every group, used to detect if the group already exists
        Set<String> remoteGroupKeys = savedGroups.stream()
                .map(g -> g.getName() + g.getGroupIdentifier())
                .collect(Collectors.toSet());

        // Test if any existing groups (persistent) have already been added to the server, if not, add them
        for (WaypointGroup existingGroup : existingGroups) {
            String key = existingGroup.getName() + existingGroup.getGuid();
            if (!remoteGroupKeys.contains(key) && !Constants.forbiddenGroups.contains(existingGroup.getGuid()) && existingGroup.isPersistent()) {
                getInstance().groupCreationHandler(existingGroup, player, true, false);
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
    private boolean handleUploadWaypoints(JsonObject jsonWaypoints, LocalPlayer player) throws JsonSyntaxException, IllegalStateException {
        boolean hasLocalWaypoint = false;

        // Get existing waypoints (local) and get waypoint objects saved on server
        List<? extends Waypoint> existingWaypoints = getInstance().jmAPI.getAllWaypoints();
        Set<SavedWaypoint> savedWaypoints = JMWSPlugin.getSavedWaypoints(jsonWaypoints.deepCopy(), player.getUUID());

        // Get an identifier of every waypoint (BlockPos, location), used to detect if the waypoint already exists
        Set<BlockPos> remoteWaypointPositions = savedWaypoints.stream()
                .map(w -> new BlockPos(w.getWaypointX(), w.getWaypointY(), w.getWaypointZ()))
                .collect(Collectors.toSet());

        getInstance().jmAPI.removeAllWaypoints("journeymap");

        // Test if any existing waypoints (persistent, usually death waypoints) have already been added to the server, if not, add them
        for (Waypoint existing : existingWaypoints) {
            if (!remoteWaypointPositions.contains(existing.getBlockPos()) && existing.isPersistent()) {
                getInstance().createAction(existing, true, false);
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

    public static void syncHandler(JMWSActionPayload waypointPayload, LocalPlayer player) {
        boolean hasLocalGroup = false;
        boolean hasLocalWaypoint = false;
        boolean sendAlert = waypointPayload.arguments().getLast().getAsBoolean();

        try {
            // true was config.uploadGroups() && config.serverConfiguration.serverGroupsEnabled()
            if (true) {
                hasLocalGroup = getInstance().handleUploadGroups(waypointPayload.arguments().get(1).getAsJsonObject(), player);
            }

            // true was config.uploadWaypoints() && config.serverConfiguration.serverWaypointsEnabled()
            if (true) {
                hasLocalWaypoint = getInstance().handleUploadWaypoints(waypointPayload.arguments().getFirst().getAsJsonObject(), player);
            }

            if (hasLocalGroup || hasLocalWaypoint) {
                updateWaypoints(false);
                if (hasLocalGroup && hasLocalWaypoint) {
                    PlayerHelper.sendUserAlert(Component.translatable("message.jmws.local_both_upload"), true, false, JMWSMessageType.SUCCESS);
                } else if (hasLocalGroup) {
                    PlayerHelper.sendUserAlert(Component.translatable("message.jmws.local_group_upload"), true, false, JMWSMessageType.SUCCESS);
                } else {
                    PlayerHelper.sendUserAlert(Component.translatable("message.jmws.local_waypoint_upload"), true, false, JMWSMessageType.SUCCESS);
                }
            } else if (sendAlert) {
                String updateMessageKey = "message.jmws.synced_success";

                // true was config.uploadWaypoints() && config.uploadGroups()
                if (true) {
                    updateMessageKey = "message.jmws.synced_both_success";
                } else if (true) {
                    // true was config.uploadGroups()
                    updateMessageKey = "message.jmws.synced_group_success";
                }
                PlayerHelper.sendUserAlert(Component.translatable(updateMessageKey), true, false, JMWSMessageType.NEUTRAL);
            }
            PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);

        } catch (IllegalStateException | JsonSyntaxException exception) {
            PlayerHelper.sendUserAlert(Component.translatable("error.jmws.error_corrupted_waypoint"), true, false, JMWSMessageType.FAILURE);
            PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
        }
    }
}
