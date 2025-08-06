package me.brynview.navidrohim.jmws.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import commonnetwork.api.Dispatcher;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.event.RegistryEvent;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.common.event.CommonEventRegistry;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.event.common.WaypointGroupEvent;
import journeymap.api.v2.common.event.common.WaypointGroupTransferEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.CommonClass;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helpers.JMWSSounds;
import me.brynview.navidrohim.jmws.client.objects.SavedGroup;
import me.brynview.navidrohim.jmws.client.objects.SavedWaypoint;
import me.brynview.navidrohim.jmws.helper.CommandHelper;
import me.brynview.navidrohim.jmws.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.helper.CommonHelper;
import me.brynview.navidrohim.jmws.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.platform.Services;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.stream.Collectors;

import static me.brynview.navidrohim.jmws.CommonClass.config;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JMWSPlugin implements IClientPlugin {

    // Variables

    // JourneyMap API
    private IClientAPI jmAPI = null;
    private static JMWSPlugin INSTANCE;
    //private static final ConfigInterface config = CommonClass.config;

    // Required functions

    @Override
    public void initialize(IClientAPI jmClientApi)
    {

        this.jmAPI = jmClientApi;

        CommonEventRegistry.WAYPOINT_EVENT.subscribe("jmapi", this::waypointCreationHandler);
        CommonEventRegistry.WAYPOINT_GROUP_EVENT.subscribe("jmapi", Constants.MODID, this::groupEventListener);
        CommonEventRegistry.WAYPOINT_GROUP_TRANSFER_EVENT.subscribe("jmapi", Constants.MODID, this::waypointDragHandler); // Not working with current JourneyMap beta.53, should be fixed with new JM version with no changes on my end
        FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(Constants.MODID, JMButtonAddon::addJMButtons);
        ClientEventRegistry.OPTIONS_REGISTRY_EVENT.subscribe("jmapi", (RegistryEvent.OptionsRegistryEvent optionsRegistryEvent) -> {
            config = new ConfigInterface();});
    }

    @Override
    public String getModId() {
        return Constants.MODID;
    }

    public static JMWSPlugin getInstance() {
        return INSTANCE;
    }

    public JMWSPlugin()
    {
        INSTANCE = this;
    }

    // General helper functions
    // Methods for manipulating / creating waypoints on the server

    private void createAction(Waypoint waypoint, boolean silent, boolean isUpdate) {
        ObjectIdentifierMap.addWaypointToMap(waypoint);
        waypoint.setPersistent(false);

        String creationData = CommandHelper.makeCreationRequestJson(waypoint, silent, isUpdate);
        Dispatcher.sendToServer(new JMWSActionPayload(creationData));
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

        String waypointFilename = CommonHelper.getWaypointFilename(waypoint, CommonClass.minecraftClientInstance.player.getUUID());

        ObjectIdentifierMap.removeWaypointFromMap(waypoint);
        String jsonPacketData = CommandHelper.makeDeleteRequestJson(waypointFilename, silent, false);
        JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonPacketData);

        jmAPI.removeWaypoint("journeymap", waypoint);
        Dispatcher.sendToServer(waypointActionPayload);
    }

    // JourneyMap event handlers
    void waypointCreationHandler(WaypointEvent waypointEvent) {

        Waypoint oldWaypoint = ObjectIdentifierMap.getOldWaypoint(waypointEvent.waypoint);

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

    private void groupEventListener(WaypointGroupEvent waypointGroupEvent)
    {
        if (CommonClass.getEnabledStatus() && config.uploadGroups.get() && !Constants.forbiddenGroups.contains(waypointGroupEvent.getGroup().getGuid())) {
            LocalPlayer player = CommonClass.minecraftClientInstance.player;
            WaypointGroup waypointGroup = waypointGroupEvent.getGroup();

            if (player == null) {
                return;
            }
            WaypointGroup oldWaypointGroup = ObjectIdentifierMap.getOldGroup(waypointGroup);

            switch (waypointGroupEvent.getContext()) {
                case CREATE -> this.groupCreationHandler(waypointGroup, player, false, false); // MAKE SURE you use beta 47 or higher
                case DELETED -> this.groupDeletionHandler(waypointGroup, player, false, waypointGroupEvent.deleteWaypoints());
                case UPDATE -> this.groupUpdateHandler(waypointGroup, oldWaypointGroup, player);
            }
        }
    }

    private void groupDeletionHandler(WaypointGroup waypointGroup, LocalPlayer player, boolean silent, boolean deleteAllWaypoints)
    {
        ObjectIdentifierMap.removeGroupFromMap(waypointGroup);
        String jsonPacketData = CommandHelper.makeDeleteGroupRequestJson(
                player.getUUID(),
                waypointGroup.getCustomData(),
                waypointGroup.getGuid(),
                silent,
                deleteAllWaypoints,
                false);

        JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonPacketData);
        Dispatcher.sendToServer(waypointActionPayload);
    }

    private void groupUpdateHandler(WaypointGroup waypointGroup, WaypointGroup oldWaypointGroup, LocalPlayer player)
    {
        if (oldWaypointGroup != null) {
            this.groupDeletionHandler(oldWaypointGroup, player, true, false);
        }
        this.groupCreationHandler(waypointGroup, player, true, true);

        PlayerHelper.sendUserAlert(Component.translatable("message.jmws.modified_group_success"), true, false, JMWSMessageType.SUCCESS);
    }

    private void waypointDragHandler(WaypointGroupTransferEvent waypointGroupTransferEvent) {
        Waypoint subjectedChangeWp = waypointGroupTransferEvent.getWaypoint();
        waypointGroupTransferEvent.getGroupTo().addWaypoint(subjectedChangeWp);

        updateAction(subjectedChangeWp, subjectedChangeWp);
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
                INSTANCE.jmAPI.removeWaypoint("journeymap", ObjectIdentifierMap.getOldWaypoint(toDelete));
            }

        } else {
            deletionMessageConfirmationKey = "message.jmws.deletion_group_all_success";
            if (deleteAll) {
                JMWSPlugin.deleteAllGroups();
            } else {
                JMWSPlugin.getInstance().jmAPI.removeWaypointGroup(ObjectIdentifierMap.getOldGroup(toDelete), false);
            }
        }
        PlayerHelper.sendUserAlert(Component.translatable(deletionMessageConfirmationKey), true, false, JMWSMessageType.NEUTRAL);
    }

    public static void updateWaypoints(boolean sendAlert) { // Might use delay some day

        // Sends "request" packet | New = "SYNC"
        if (CommonClass.getEnabledStatus()) {
            Dispatcher.sendToServer(new JMWSActionPayload(CommandHelper.makeWaypointSyncRequestJson(sendAlert)));
        }
    }

    // Syncing -- Funcions for syncing waypoints and groups

    private void groupCreationHandler(WaypointGroup waypointGroup, LocalPlayer player, boolean silent, boolean isUpdate)
    {
        ObjectIdentifierMap.addGroupToMap(waypointGroup);
        waypointGroup.setPersistent(false);
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
            ObjectIdentifierMap.addGroupToMap(group);
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
            ObjectIdentifierMap.addWaypointToMap(wp);
            getInstance().jmAPI.addWaypoint("journeymap", wp);
        }

        return hasLocalWaypoint;
    }

    public static void syncHandler(JMWSActionPayload waypointPayload, LocalPlayer player) {
        boolean hasLocalGroup = false;
        boolean hasLocalWaypoint = false;
        boolean sendAlert = waypointPayload.arguments().getLast().getAsBoolean();

        try {
            if (config.uploadGroups.get()) {
                hasLocalGroup = getInstance().handleUploadGroups(waypointPayload.arguments().get(1).getAsJsonObject(), player);
            }

            if (config.uploadGroups.get()) {
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

                if (config.uploadGroups.get() && config.uploadGroups.get()) {
                    updateMessageKey = "message.jmws.synced_both_success";
                } else if (config.uploadGroups.get()) {
                    updateMessageKey = "message.jmws.synced_group_success";
                }
                PlayerHelper.sendUserAlert(Component.translatable(updateMessageKey), true, false, JMWSMessageType.NEUTRAL);
            }

            PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);
            CommonClass.syncCounter.resetSyncThreshold();

        } catch (IllegalStateException | JsonSyntaxException exception) {
            PlayerHelper.sendUserAlert(Component.translatable("error.jmws.error_corrupted_waypoint"), true, false, JMWSMessageType.FAILURE);
            PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
        }
    }

    public static void removeAllGroups() {
        for (WaypointGroup wp : getInstance().jmAPI.getAllWaypointGroups()) {
            if (!Constants.forbiddenGroups.contains(wp.getGuid())) {
                getInstance().jmAPI.removeWaypointGroup(wp, false);
            }
        }
    }
}
