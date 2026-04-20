package me.brynview.navidrohim.jmws.client.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.event.*;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.common.event.CommonEventRegistry;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import journeymap.api.v2.common.event.common.WaypointEvent;
import journeymap.api.v2.common.event.common.WaypointGroupEvent;
import journeymap.api.v2.common.event.common.WaypointGroupTransferEvent;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.assets.JMWSTextures;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncInformation;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncUtils;
import me.brynview.navidrohim.jmws.client.syncing.SyncRegistry;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;
import me.brynview.navidrohim.jmws.client.syncing.objects.factory.ClientObjectFactory;
import me.brynview.navidrohim.jmws.client.ui.screen.ShareScreen;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientGroupWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.config.ConfigInterface;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.assets.JMWSSounds;
import me.brynview.navidrohim.jmws.common.utils.CommonUtils;
import me.brynview.navidrohim.jmws.common.utils.SyncUtils;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.brynview.navidrohim.jmws.common.JMWSCommon.*;

/**
 * Main JMWS plugin which interfaces with JourneyMap.
 */
@JourneyMapPlugin(apiVersion = "2.0.0")
public class JMWSPlugin implements IClientPlugin {

    enum Action{
        GLOBAL,
        UNGLOBAL,
        SHARE

    }
    // JourneyMap API
    private IClientAPI jmAPI = null;
    private static JMWSPlugin INSTANCE;

    // Required functions

    @Override
    public void initialize(@NotNull IClientAPI jmClientApi)
    {
        this.jmAPI = jmClientApi;

        CommonEventRegistry.WAYPOINT_EVENT.subscribe(Constants.MODID, this::waypointEventHandler);
        CommonEventRegistry.WAYPOINT_GROUP_EVENT.subscribe(Constants.MODID + "group_event", Constants.MODID, this::groupEventListener);
        CommonEventRegistry.WAYPOINT_GROUP_TRANSFER_EVENT.subscribe(Constants.MODID + "group_transfer", Constants.MODID, this::waypointDragHandler);

        FullscreenEventRegistry.WAYPOINT_POPUP_MENU_EVENT.subscribe(Constants.MODID, this::addOptionForContextMenu);
        FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(Constants.MODID, JMButtonAddon::addJMButtons);
        FullscreenEventRegistry.FULLSCREEN_RENDER_EVENT.subscribe(Constants.MODID, (_) -> {
            JMWSClientCommon.config.serverEnabled.set(JMWSClientCommon.serverConfig.serverEnabled());
            JMWSClientCommon.config.serverUploadWaypoints.set(JMWSClientCommon.serverConfig.waypointsEnabled());
            JMWSClientCommon.config.serverUploadGroups.set(JMWSClientCommon.serverConfig.groupsEnabled());
            JMWSClientCommon.config.serverAllowsSharing.set(JMWSClientCommon.serverConfig.sharingEnabled);
        });

        ClientEventRegistry.DEATH_WAYPOINT_EVENT.subscribe(Constants.MODID, this::handleUserDeath);
        ClientEventRegistry.OPTIONS_REGISTRY_EVENT.subscribe(Constants.MODID, (_ -> JMWSClientCommon.config = new ConfigInterface()));
        ClientEventRegistry.MAPPING_EVENT.subscribe(Constants.MODID, (MappingEvent event) -> {
            if (JMWSClientCommon.didHandshake)
            {
                JMWSPlugin.sync(false);
            } else {
                JMWSClientCommon.isMapping = true;
            }
            });

    }

    private void addOptionForContextMenu(PopupMenuEvent.WaypointPopupMenuEvent waypointPopupMenuEvent)
    {
        if (ConfigInterface.getEnabledStatus() && JMWSClientCommon.config.waypointsEnabled() && JMWSClientCommon.serverConfig.waypointsEnabled())
        {
            ClientWaypointWrapper waypoint = ObjectIdentifierMap.getWaypointFromContextMenu(waypointPopupMenuEvent.getWaypoint());
            if (waypoint.getOwner().equals(PlayerUtils.ourUUID()))
            {
                if (!waypoint.getGlobal())
                {
                    waypointPopupMenuEvent.getPopupMenu().addMenuItem("Global", (blockPos) -> {this.handleWaypointContextMenuClick(waypoint, blockPos, Action.GLOBAL);});
                } else {
                    waypointPopupMenuEvent.getPopupMenu().addMenuItem("Remove Global", (blockPos) -> {this.handleWaypointContextMenuClick(waypoint, blockPos, Action.UNGLOBAL);});
                }

                waypointPopupMenuEvent.getPopupMenu().addMenuItem("Share", (blockPos) -> {this.handleWaypointContextMenuClick(waypoint, blockPos, Action.SHARE);});
            }
        }
    }

    private void handleWaypointContextMenuClick(ClientWaypointWrapper waypoint, BlockPos ignoredBlockPos, Action action)
    {

        switch (action)
        {
            case GLOBAL ->
            {
                waypoint.setGlobal(true);
            }
            case UNGLOBAL ->
            {
                waypoint.setGlobal(false);
            }
            case SHARE ->
            {
                ShareScreen.openShare(waypoint);
            }
        }
    }

    /**
     * Only called from DEATH_WAYPOINT_EVENT (when user is killed for any reason) Do not call.
     * @param deathWaypointEvent -- The event. Does not have method for retrieving the death waypoint for some reason.
     */
    private void handleUserDeath(DeathWaypointEvent deathWaypointEvent) {
        if (!isInternalServer())
        {
            // We just manually sync the client to upload the death waypoint. This is because DeathWaypointEvent does not give access to the actual Waypoint instance
            // So there is no actual way of uploading the death waypoint with createAction since it requires a Waypoint instance.
            scheduler.schedule(() -> sync(true, true), 5, TimeUnit.SECONDS);
        }
    }
    
    @Override
    public String getModId() {
        return Constants.MODID;
    }

    /**
     * Get the current plugin instance.
     * @return JMWSPlugin -- The JMWS plugin instance. There can only be one per instance of the game.
     */
    public static JMWSPlugin getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor for the plugin. Use the initialise method to do logic and other stuff.
     */
    public JMWSPlugin()
    {
        INSTANCE = this;
    }

    // General helper functions
    // Methods for manipulating / creating waypoints on the server

    /**
     * Creates a waypoint on the server.
     *
     * @param waypoint -- Local instance of the new waypoint to create.
     * @param silent   -- If the creation should happen silently (no text alert on the client)
     */
    private void createAction(ClientWaypointWrapper waypoint, boolean silent) {
        if (JMWSClientCommon.serverConfig.waypointsEnabled()) {
            Constants.getLogger().info(String.valueOf(waypoint.getContext()));
            if (waypoint.getContext() == Context.NATIVE)
            {
                ObjectIdentifierMap.addObjectToMap(waypoint, silent, true);
            }
        } else {
            PlayerUtils.sendUserAlert(Component.translatable( "message.jmws.server_disabled_waypoints"), true, false, MessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Syncs an updated waypoint to the server.
     * @param waypoint -- What waypoint needs updating
     */
    private void updateAction(ClientWaypointWrapper waypoint)
    {
        if (JMWSClientCommon.serverConfig.waypointsEnabled()) // Check config
        {
            if (waypoint.getContext() == Context.SYNCHRONISE)
            {
                waypoint.updateRemotely();
            } else if (waypoint.getContext() == Context.NATIVE)
            {
                this.createAction(waypoint, true);
            }

        } else {
            PlayerUtils.sendUserAlert(Component.translatable( "message.jmws.server_disabled_waypoints"), true, false, MessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Deletes the specified waypoint on the server.
     *
     * @param waypoint -- What waypoint to delete
     */
    private void deleteAction(ClientWaypointWrapper waypoint) {
        // Check if action is allowed by the server.
        if (JMWSClientCommon.serverConfig.waypointsEnabled()) {
            //@Nullable ServerSyncingHandler serverSyncingHandler = SyncUtils.getSyncingInfo(waypoint.getCustomData(Constants.MODID));
            Constants.LoggerHolder.debug(waypoint.getContext(), "DELETE CONTEXT");
            if (waypoint.getContext() == Context.SYNCHRONISE) // Can be null if JMWS has no knowledge of a waypoint
            {
                ObjectIdentifierMap.removeObjectFromMap(waypoint, false, true);
                //ClientNetworkDispatcher.deleteWaypoint(syncWaypoint.getIdentifier(), false, false);
                //CommandFactory.deleteWaypoint(serverSyncingHandler.objectIdentifier,false, false);
                // removedWaypoint is called here because, yes, we do listen for the deletion with the event (meaning, the waypoint should be already gone by the time the event is called)
                // But for some reason it bugs out and the waypoint stays and becomes persistent
                //jmAPI.removeWaypoint(waypoint.getModId(), waypoint);
            }

        } else {
            PlayerUtils.sendUserAlert(Component.translatable( "message.jmws.server_disabled_waypoints"), true, false, MessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Only called from WAYPOINT_EVENT (when a waypoint is created, updated, or deleted) Do not call.
     * @param waypointEvent The event.
     */
    void waypointEventHandler(WaypointEvent waypointEvent) {

        if (Constants.DEBUG) {
            Constants.LoggerHolder.debug(String.valueOf((!JMWSClientCommon.isBusy && ConfigInterface.getEnabledStatus() && JMWSClientCommon.config.waypointsEnabled() && JMWSClientCommon.serverConfig.waypointsEnabled())), "can exec");
            Constants.LoggerHolder.debug(String.valueOf(JMWSClientCommon.serverConfig.waypointsEnabled()), "wp enable test");
        }
        if (!JMWSClientCommon.isBusy && ConfigInterface.getEnabledStatus() && JMWSClientCommon.config.waypointsEnabled() && JMWSClientCommon.serverConfig.waypointsEnabled()) { // Check that the user is in the physical server, user config allows event, and server config allows event.
            // Get old waypoint if context is UPDATE (needed because the server needs reference to the waypoint before it was updated so it can be deleted on the server)
            JMWSClientCommon.isBusy = true;
            ClientWaypointWrapper waypoint = ClientObjectFactory.fromWaypoint(waypointEvent.waypoint);
            switch (waypointEvent.getContext()) {
                case CREATE ->
                    // Sends "create" packet | new = "SERVER_CREATE"
                        this.createAction(waypoint, false);
                case DELETED ->
                    // Sends "delete" packet | new = "COMMON_SERVER_DELETE"
                        this.deleteAction(waypoint);
                case UPDATE ->
                {
                        waypoint.setNativeObject(waypointEvent.waypoint);
                        this.updateAction(waypoint);
                }
            }
            JMWSClientCommon.isBusy = false;
        }
    }

    /**
     * Only called from WAYPOINT_GROUP_EVENT (when group is created, updated, or deleted) Do not call.
     * @param waypointGroupEvent The event.
     */
    private void groupEventListener(WaypointGroupEvent waypointGroupEvent)
    {
        if (!JMWSClientCommon.isBusy && ConfigInterface.getEnabledStatus() && JMWSClientCommon.config.groupsEnabled() && JMWSClientCommon.serverConfig.groupsEnabled()) // Check that user is in physical server, user config allows event, and server config allows event.
        {
            JMWSClientCommon.isBusy = true;
            WaypointGroup waypointGroup = waypointGroupEvent.getGroup();
            ClientGroupWrapper syncGroup = ClientObjectFactory.fromGroup(waypointGroup);
            LocalPlayer player = JMWSCommon.minecraftClientInstance.player;

            Constants.LoggerHolder.debug(syncGroup.getContext().toString(), "GROUP TYPE");
            if (player == null) {
                return;
            }
            // Get old group if context is UPDATE (needed because server needs reference to group before it was updated so it can be deleted on the server)

            switch (waypointGroupEvent.getContext()) {
                case CREATE -> this.groupCreationHandler(syncGroup, false);
                case DELETED -> this.groupDeletionHandler(syncGroup, waypointGroupEvent.deleteWaypoints());
                case UPDATE -> {
                    syncGroup.setNativeObject(waypointGroupEvent.getGroup());
                    this.groupUpdateHandler(syncGroup);
                }
            }
            JMWSClientCommon.isBusy = false;
        }
    }

    /**
     * Delete group(s) on the server.
     *
     * @param waypointGroup -- What group needs deleting
     */
    private void groupDeletionHandler(ClientGroupWrapper waypointGroup, boolean removeAll)
    {
        if (JMWSClientCommon.serverConfig.groupsEnabled()) // Make sure config allows it
        {
            if (waypointGroup.isUsableOrNative())
            {
                Constants.LoggerHolder.debug(waypointGroup.getContext().toString(), "GROUP TYPE");
                if (waypointGroup.getContext() == Context.SYNCHRONISE) {
                    if (!waypointGroup.getGlobal())
                    {
                        ObjectIdentifierMap.removeObjectFromMap(waypointGroup, false, false);
                        waypointGroup.removeRemotely(false, false, true);
                    } else {
                        PlayerUtils.sendUserAlert(Component.translatable("global.jmws.cannot_delete_global"), true, false, MessageType.ONE_TIME_WARNING);
                    }
                } else if (waypointGroup.getContext() == Context.INBUILT) {
                    waypointGroup.removeRemotely(false, false, false);
                }
            }
        } else {
            PlayerUtils.sendUserAlert(Component.translatable( "message.jmws.server_disabled_waypoints"), true, false, MessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Syncs an updated group to the server.
     * @param waypointGroup -- What group needs updating
     */
    private void groupUpdateHandler(ClientGroupWrapper waypointGroup)
    {
        if (JMWSClientCommon.serverConfig.groupsEnabled()) // Make sure config allows it
        {
            if (waypointGroup.getContext() == Context.SYNCHRONISE)
            {
                waypointGroup.updateRemotely();
            } else if (waypointGroup.getContext() == Context.NATIVE)
            {
                waypointGroup.createRemotely(false);
            }
        } else {
            PlayerUtils.sendUserAlert(Component.translatable("message.jmws.server_disabled_groups"), true, false, MessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Only called from WAYPOINT_GROUP_TRANSFER_EVENT (when waypoint is dragged into a group) Do not call.
     * @param waypointGroupTransferEvent The event.
     */
    private void waypointDragHandler(WaypointGroupTransferEvent waypointGroupTransferEvent) {
        // Do not do on LAN, since there is no physical server.
        Waypoint subjectedChangeWp = waypointGroupTransferEvent.getWaypoint();
        @Nullable String identifier = ClientSyncUtils.getOnlyIdentifier(subjectedChangeWp.getCustomData(Constants.MODID));
        @Nullable ClientWaypointWrapper waypoint = ObjectIdentifierMap.getObjectFromMap(identifier, ClientWaypointWrapper.class);

        if (!isInternalServer() && waypoint != null)
        {
            updateAction(waypoint);
        }
        //waypointGroupTransferEvent.getGroupTo().addWaypoint(subjectedChangeWp);
    }

    /**
     * Removes all local groups. There is no API call to remove all groups.
     */

    public static void deleteAllGroups() {
        // This method is a bodge fix. removeWaypointGroups (which I believe removes all groups) does not work because you cannot change the modId of a group.

        // Iterate through each group, delete it
        for (WaypointGroup wp : getInstance().jmAPI.getAllWaypointGroups()) {
            if (!Constants.forbiddenGroups.contains(wp.getGuid())) {
                getInstance().jmAPI.removeWaypointGroup(wp, false);
            }
        }
    }

    /**
     * Delete a synced object (waypoint or group). This method is only called by COMMON_DELETE_WAYPOINT in the packet handler on the client-side.
     *
     * @param toDelete     -- ObjectIdentifierMap ID, this is stored on the server only in the "customData" field (example; 38ab19a2e6544389265e40ad23b49983d9620b199c111e20b2b9a5159458b519)
     */
    public <T extends ClientBaseObjectWrapper<Object>> void deleteSavedObjects(boolean silent, Class<T> objectClass, String toDelete)
    {
        String deletionMessageConfirmationKey = "message.jmws.deletion_all_success";
        @Nullable ClientBaseObjectWrapper<Object> obj = ObjectIdentifierMap.getObjectFromMap(toDelete, objectClass);

        if (obj != null) {
            ObjectIdentifierMap.removeObjectFromMap(obj, false, false);
        }

        if (!silent)
        {
            PlayerUtils.sendUserAlert(Component.translatable(deletionMessageConfirmationKey), true, false, MessageType.NEUTRAL);
        }
    }

    /**
     * Syncs client to server
     * @param sendAlert -- If to send an alert when finished syncing
     * @param fromDeathEvent -- If this is being called as regards to a death event (Forces a local waypoint update)
     */
    public static void sync(boolean sendAlert, boolean fromDeathEvent) {

        // Sends "request" packet | New = "SYNC"
        if (ConfigInterface.getEnabledStatus()) {
            ClientNetworkDispatcher.sync(sendAlert, fromDeathEvent);
        }
    }

    /**
     * Syncs client to server
     * @param sendAlert -- If to send an alert when finished syncing
     */
    public static void sync(boolean sendAlert) {
        sync(sendAlert, false);
    }

    private static void portLegacyDataField(@Nullable String objectAsString, ObjectType transitionType)
    {
        JsonObject legacy = CommonUtils.parseStringToJsonObject(objectAsString);

        if (legacy.has("customData"))
        {
            JsonElement customDataOld = legacy.get("customData");
            String customDataString = customDataOld.getAsString();
            JsonObject customData = CommonUtils.parseStringToJsonObject(customDataString);

            if (SyncUtils.isValidSyncField(customDataString))
            {
                String legacyObjectIdentifier = customData.get("objectIdentifier").getAsString();
                UUID legacyOwnerUUID = UUID.fromString(customData.get("owner").getAsString());
                boolean isGlobal = customData.get("isGlobal").getAsBoolean();

                ClientNetworkDispatcher.transitionToNewCustomData(legacyObjectIdentifier, legacyOwnerUUID, isGlobal, transitionType);
                Constants.getLogger().info("Detected legacy customData from an object not belonging to this client. Sent porting packet. Please use '/jmws sync' to resync.");
            } else {
                Constants.getLogger().error("Error transitioning customData to customDataMap. Trace: {} {} {}", customDataOld, customDataString, customData);
            }
        } else {
            Constants.getLogger().error("portLegacyDataField was called but customData not present? **TRACE** Object as String: {} <> as JSON: {} <> transitionType: {}", objectAsString, legacy, transitionType);
        }
    }
    // Syncing -- Functions for syncing waypoints and groups

    /**
     * Handles a local groups creation, syncs it to the server
     *
     * @param waypointGroup -- What waypoint group to create / update
     * @param silent        -- If there should be an alert when created
     */
    private void groupCreationHandler(ClientGroupWrapper waypointGroup, boolean silent)
    {
        if (JMWSClientCommon.serverConfig.groupsEnabled()) {
            Constants.getLogger().info(String.valueOf(waypointGroup.getContext()));
            if (waypointGroup.getContext() == Context.NATIVE)
            {
                ObjectIdentifierMap.addObjectToMap(waypointGroup, silent, true);
            }
        } else {
            PlayerUtils.sendUserAlert(Component.translatable("message.jmws.server_disabled_groups"), true, false, MessageType.ONE_TIME_WARNING);
        }
    }

    /**
     * Helper for syncHandler, do not use.
     * @param jsonData Json data from the server
     * @return A set of SavedWaypoints from the server.
     * @throws JsonSyntaxException If waypoint is malformed or does not parse.
     * @throws IllegalStateException Cannot remember why this can be thrown.
     */
    private static Set<Waypoint> getSavedWaypoints(JsonObject jsonData) throws JsonSyntaxException, IllegalStateException {
        Set<Waypoint> waypoints = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            String json = entry.getValue().getAsString();
            waypoints.add(WaypointFactory.fromWaypointJsonString(json));
        }

        return waypoints;

    }

    /**
     * Helper for syncHandler, do not use.
     * @param jsonData JSON data from the server
     * @return A set of SavedGroups from the server.
     * @throws JsonSyntaxException If group is malformed or does not parse.
     * @throws IllegalStateException Cannot remember why this can be thrown.
     */
    private static Set<WaypointGroup> getSavedGroups(JsonObject jsonData) throws JsonSyntaxException, IllegalStateException {
        Set<WaypointGroup> groups = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
            String json = entry.getValue().getAsString();
            groups.add(WaypointFactory.fromGroupJsonString(json));
        }

        return groups;

    }

    /**
     * Sync helper for waypoints
     * @param jsonGroupsRaw -- The groups to add to the client.
     * @return boolean -- If the user had any local groups to upload.
     * @throws JsonSyntaxException -- If there is a syntax error with the Json, usually from a corrupted group.
     */
    private static boolean handleUploadGroups(JsonObject jsonGroupsRaw, boolean showSharingLabels, boolean showGlobalLabels) throws JsonSyntaxException, IllegalStateException, NullPointerException {
        boolean hasLocalGroup = false;

        try
        {
            // Get existing groups (local) and of group objects saved on server
            List<? extends WaypointGroup> existingGroups = getInstance().jmAPI.getAllWaypointGroups();
            Set<WaypointGroup> savedGroups = JMWSPlugin.getSavedGroups(jsonGroupsRaw.deepCopy());

            // Get an identifier of every group, used to detect if the group already exists
            Set<String> remoteGroupKeys = savedGroups.stream()
                    .map(g -> g.getName() + g.getGuid() + g.getColor())
                    .collect(Collectors.toSet());

            // Test if any existing groups (persistent) have already been added to the server, if not, add them

            for (WaypointGroup existingGroup : existingGroups) {
                String groupKey = existingGroup.getName() + existingGroup.getGuid() + existingGroup.getColor();

                if (!remoteGroupKeys.contains(groupKey) && existingGroup.isPersistent() && Constants.allowedMods.contains(existingGroup.getModId()) && !Constants.forbiddenGroups.contains(existingGroup.getGuid())) {
                    ClientGroupWrapper group = ClientObjectFactory.fromGroup(existingGroup);
                    getInstance().groupCreationHandler(group, true);
                    hasLocalGroup = true;
                }
            }

            // Add server groups to the client
            for (WaypointGroup savedGroup : savedGroups) {
                ClientSyncInformation gpSync = ClientSyncUtils.syncInformationFromString(savedGroup.getCustomData(Constants.MODID), SyncRegistry.GROUP);

                if (gpSync == null) {
                    portLegacyDataField(savedGroup.toString(), ObjectType.GROUP);
                    continue;
                }

                if (gpSync.isGlobal && gpSync.isOwner(PlayerUtils.ourUUID()))
                {
                    savedGroup.setLocked(false);
                }

                if (!gpSync.isOwner(PlayerUtils.ourUUID()))
                {
                    savedGroup.setLocked(true);
                    if (gpSync.isGlobal && showGlobalLabels)
                    {
                        savedGroup.setName(savedGroup.getName() + " (%s)".formatted(CommonUtils.globalStringTag));
                    } else if (showSharingLabels)
                    {
                        String ownerUser = PlayerUtils.getUsernameFromUUID(gpSync.owner);
                        savedGroup.setName(savedGroup.getName() + " (%s)".formatted(ownerUser));
                    }
                }
                addGroup(savedGroup);
            }

            // return this because need to give an alert
            return hasLocalGroup;
        } catch (Exception exc)
        {
            JMWSClientCommon.isBusy = false;
            Constants.getLogger().error("Failed to sync groups. Exception thrown: ", exc);
            throw exc;
        }
    }

    /**
     * Sync helper for waypoints
     * @param jsonWaypoints -- The waypoints to add to the client.
     * @return boolean -- If the user had any local waypoints to upload.
     * @throws JsonSyntaxException -- If there is a syntax error with the JSON, usually from a corrupted waypoint.
     */
    private static boolean handleUploadWaypoints(JsonObject jsonWaypoints, boolean showSharingLabels, boolean showGlobalLabels) throws JsonSyntaxException, NullPointerException {
        try
        {
            boolean hasLocalWaypoint = false;

            // Get existing waypoints (local) and of waypoint objects saved on server
            List<? extends Waypoint> existingWaypoints = getInstance().jmAPI.getAllWaypoints();
            Set<Waypoint> savedWaypoints = JMWSPlugin.getSavedWaypoints(jsonWaypoints.deepCopy());

            // Get an identifier of every waypoint (BlockPos, location), used to detect if the waypoint already exists
            Set<BlockPos> remoteWaypointPositions = savedWaypoints.stream()
                    .map(Waypoint::getBlockPos)
                    .collect(Collectors.toSet());

            getInstance().jmAPI.removeAllWaypoints(Constants.MODID); // Delete all waypoints belonging to JMWS
            getInstance().jmAPI.removeAllWaypoints("journeymap");

            // Test if any existing waypoints (persistent, usually death waypoints or 3rd party waypoints from another add-on) have already been added to the server, if not, add them
            for (Waypoint existing : existingWaypoints) {
                @Nullable ClientWaypointWrapper existingWaypoint = ClientObjectFactory.fromWaypoint(existing);

                if (!remoteWaypointPositions.contains(existing.getBlockPos()) && (existingWaypoint.getContext() == Context.NATIVE)) {
                    getInstance().createAction(existingWaypoint, true);
                    hasLocalWaypoint = true;
                }
            }

            // Add server waypoints to the client
            for (Waypoint savedWaypoint : savedWaypoints) {
                @Nullable ClientSyncInformation wpSync = ClientSyncUtils.syncInformationFromString(savedWaypoint.getCustomData(Constants.MODID), SyncRegistry.WAYPOINT);

                if (wpSync == null) {
                    //portLegacyDataField(savedWaypoint.toString(), ObjectType.WAYPOINT);
                    continue;
                }
                Constants.LoggerHolder.debug(savedWaypoint.getCustomData(Constants.MODID), "SYNC INFORMATION");
                if (!wpSync.isOwner(PlayerUtils.ourUUID()))
                {
                    if (wpSync.isGlobal && showGlobalLabels) // Global
                    {
                        savedWaypoint.setIconResourceLoctaion(JMWSTextures.globalObjectAsset);
                        savedWaypoint.setName(savedWaypoint.getName() + " (%s)".formatted(CommonUtils.globalStringTag));
                    } else if (showSharingLabels) // Shared
                    {
                        String ownerUser = PlayerUtils.getUsernameFromUUIDForShare(wpSync.owner);
                        savedWaypoint.setName(savedWaypoint.getName() + " (%s)".formatted(ownerUser));
                        savedWaypoint.setIconResourceLoctaion(JMWSTextures.sharedObjectAsset);
                    }

                }
                addWaypoint(savedWaypoint);
            }

            return hasLocalWaypoint;
        } catch (Exception e) {
            JMWSClientCommon.isBusy = false;
            Constants.getLogger().error("Failed to sync. Exception thrown: ", e);

            throw e;
        }
    }

    /**
     * Adds and syncs waypoints and groups to and from the server
     * @param waypointPayload -- The sync payload from the server.
     */
    public static void syncHandler(JMWSActionPayload waypointPayload) {

        boolean hasLocalGroup = false;
        boolean hasLocalWaypoint = false;
        boolean sendAlert = waypointPayload.arguments().get(2).getAsBoolean(); // If to send an alert
        boolean isDeathSync = waypointPayload.arguments().getLast().getAsBoolean(); // If the sync was from a death waypoint creation

        boolean showSharingLabels = JMWSClientCommon.config.showSharingLabels.get();
        boolean showGlobalLabels = JMWSClientCommon.config.showGlobalLabels.get();

        try {
            // Sync remote and local groups if server and client permits
            if (JMWSClientCommon.config.uploadGroups.get() && JMWSClientCommon.serverConfig.groupsEnabled()) {
                hasLocalGroup = handleUploadGroups(waypointPayload.arguments().get(1).getAsJsonObject(), showSharingLabels, showGlobalLabels);
            }

            // Sync remote and local waypoints if server and client permits
            if (JMWSClientCommon.config.uploadGroups.get() && JMWSClientCommon.serverConfig.waypointsEnabled()) {
                hasLocalWaypoint = handleUploadWaypoints(waypointPayload.arguments().getFirst().getAsJsonObject(), showSharingLabels, showGlobalLabels);
            }

            // Send alerts if there were any local waypoints and or groups
            if (hasLocalGroup || hasLocalWaypoint) {
                sync(false);
                if (isDeathSync) {
                    PlayerUtils.sendUserAlert(Component.translatable("message.jmws.death_waypoint_sync"), true, false, MessageType.SUCCESS);
                } else if (hasLocalGroup && hasLocalWaypoint) {
                    PlayerUtils.sendUserAlert(Component.translatable("message.jmws.local_both_upload"), true, false, MessageType.SUCCESS);
                } else if (hasLocalGroup) {
                    PlayerUtils.sendUserAlert(Component.translatable("message.jmws.local_group_upload"), true, false, MessageType.SUCCESS);
                } else {
                    PlayerUtils.sendUserAlert(Component.translatable("message.jmws.local_waypoint_upload"), true, false, MessageType.SUCCESS);
                }

            } else if (sendAlert) { // send alert, client permitting
                String updateMessageKey = "message.jmws.synced_success";

                // Sync message can change depending on what client permissions there are
                if (JMWSClientCommon.config.uploadGroups.get() && JMWSClientCommon.config.uploadGroups.get()) {
                    updateMessageKey = "message.jmws.synced_both_success";
                } else if (JMWSClientCommon.config.uploadGroups.get()) {
                    updateMessageKey = "message.jmws.synced_group_success";
                }
                PlayerUtils.sendUserAlert(Component.translatable(updateMessageKey), true, false, MessageType.NEUTRAL);
            }

            PlayerUtils.sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);
            JMWSClientCommon.syncCounter.resetSyncThreshold(); // Reset auto-sync timer

        } catch (IllegalStateException | JsonSyntaxException exception) {
            PlayerUtils.sendUserAlert(Component.translatable("error.jmws.error_corrupted_waypoint"), true, false, MessageType.FAILURE);
            PlayerUtils.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
        }
    }

    public static void addWaypoint(Waypoint waypoint)
    {
        @Nullable ClientWaypointWrapper wp = ClientObjectFactory.fromWaypoint(waypoint);

        if (ObjectIdentifierMap.addObjectToMap(wp, true, false))
        {
            getInstance().jmAPI.addWaypoint(waypoint.getModId(), waypoint);
        }
    }

    public static void addGroup(WaypointGroup waypointGroup) {
        ClientGroupWrapper gp = ClientObjectFactory.fromGroup(waypointGroup);

        if (ObjectIdentifierMap.addObjectToMap(gp, true, false)) { // DEP
            getInstance().jmAPI.addWaypointGroup(waypointGroup);
        }

    }

    public void addObjectFromRequest(ShareRequest request)
    {
        if (request.sharedObjectType.equals(SyncRegistry.WAYPOINT))
        {
            addWaypoint((Waypoint) request.currentSharedObject.getNativeObject());
        } else {
            addGroup((WaypointGroup) request.currentSharedObject.getNativeObject());
        }
    }

    public void addWaypointFromWrapper(ClientWaypointWrapper waypointWrapper)
    {
        jmAPI.addWaypoint(waypointWrapper.getNativeObject().getModId(),  waypointWrapper.getNativeObject());
    }

    public void removeWaypointFromWrapper(ClientWaypointWrapper waypointWrapper)
    {
        jmAPI.removeWaypoint(waypointWrapper.getNativeObject().getModId(),  waypointWrapper.getNativeObject());
    }

    public void addGroupFromWrapper(ClientGroupWrapper waypointWrapper)
    {
        jmAPI.addWaypointGroup(waypointWrapper.getNativeObject());
    }

    public void removeGroupFromWrapper(ClientGroupWrapper groupWrapper)
    {
        jmAPI.removeWaypointGroup(groupWrapper.getNativeObject(),  false);
    }
}
