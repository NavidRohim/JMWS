package me.navidrohim.jmws.plugin;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.waypoint.WaypointStore;
import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.client.enums.JMWSMessageType;
import me.navidrohim.jmws.client.helpers.JMWSSounds;
import me.navidrohim.jmws.client.objects.SavedWaypoint;
import me.navidrohim.jmws.helper.CommandHelper;
import me.navidrohim.jmws.helper.CommonHelper;
import me.navidrohim.jmws.helper.PlayerHelper;
import me.navidrohim.jmws.payloads.JMWSActionMessage;

import me.navidrohim.jmws.payloads.JMWSNetworkWrapper;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.*;
import java.util.stream.Collectors;

import static me.navidrohim.jmws.CommonClass.config;

@ClientPlugin
public class JMWSPlugin implements IClientPlugin {

    // Variables

    // JourneyMap API
    private IClientAPI jmAPI = null;
    private static JMWSPlugin INSTANCE;

    // Required functions

    @Override
    public void initialize(IClientAPI jmClientApi)
    {

        this.jmAPI = jmClientApi;

        /*
        CommonEventRegistry.WAYPOINT_EVENT.subscribe("jmapi", this::waypointCreationHandler);*/
    }

    @Override
    public String getModId() {
        return Constants.MODID;
    }

    @Override
    public void onEvent(ClientEvent clientEvent) {

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
        JMWSNetworkWrapper.INSTANCE.sendToServer(new JMWSActionMessage(creationData));
        //Dispatcher.sendToServer(new JMWSActionPayload(creationData));
    }

    private void updateAction(Waypoint waypoint, Waypoint oldWaypoint)
    {
        if (oldWaypoint != null) {
            this.deleteAction(oldWaypoint, true);
            jmAPI.remove(oldWaypoint);
        }
        this.createAction(waypoint, true, true);

        PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.modified_waypoint_success"), true, false, JMWSMessageType.SUCCESS);
    }

    private void deleteAction(Waypoint waypoint, boolean silent) {

        String waypointFilename = CommonHelper.getWaypointFilename(waypoint, CommonClass.minecraftClientInstance.player.getUniqueID());

        ObjectIdentifierMap.removeWaypointFromMap(waypoint);
        String jsonPacketData = CommandHelper.makeDeleteRequestJson(waypointFilename, silent, false);
        JMWSActionMessage waypointActionPayload = new JMWSActionMessage(jsonPacketData);

        jmAPI.remove(waypoint);
        JMWSNetworkWrapper.INSTANCE.sendToServer(waypointActionPayload);
    }

    // JourneyMap event handlers
/*  void waypointCreationHandler(WaypointEvent waypointEvent) {

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
    }*/

    // Handling packets
    public void deleteSavedObjects(Boolean deleteAll, String toDelete)
    {
        String deletionMessageConfirmationKey = "message.jmws.deletion_all_success";
        if (deleteAll) {
            INSTANCE.jmAPI.removeAll("journeymap");
        } else {
            INSTANCE.jmAPI.remove(ObjectIdentifierMap.getOldWaypoint(toDelete));
        }


        PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent(deletionMessageConfirmationKey), true, false, JMWSMessageType.NEUTRAL)  ;
    }

    public static void updateWaypoints(boolean sendAlert) { // Might use delay some day

        // Sends "request" packet | New = "SYNC"
        if (CommonClass.getEnabledStatus()) {
            JMWSNetworkWrapper.INSTANCE.sendToServer(new JMWSActionMessage(CommandHelper.makeWaypointSyncRequestJson(sendAlert)));
        }
    }

    // Helper for handleUploadWaypoints
    private static Set<SavedWaypoint> getSavedWaypoints(JsonObject jsonData, UUID playerUUID) throws JsonSyntaxException, IllegalStateException {
        Set<SavedWaypoint> waypoints = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {

            JsonObject json = new JsonParser().parse(entry.getValue().getAsString()).getAsJsonObject();
            waypoints.add(new SavedWaypoint(json, playerUUID));
        }

        return waypoints;

    }

    // Helper for sync but for waypoints
    private boolean handleUploadWaypoints(JsonObject jsonWaypoints, EntityPlayerSP player) throws JsonSyntaxException, IllegalStateException {
        /*
        // Get existing waypoints (local) and get waypoint objects saved on server
        List<? extends Waypoint> existingWaypoints = Collections.emptyList(); //jmAPI.getAllWaypoints();
        Set<SavedWaypoint> savedWaypoints = JMWSPlugin.getSavedWaypoints(jsonWaypoints, player.getUniqueID()); //POTENTIAL ISSUE

        // Get an identifier of every waypoint (BlockPos, location), used to detect if the waypoint already exists
        Set<BlockPos> remoteWaypointPositions = savedWaypoints.stream()
                .map(w -> new BlockPos(w.getWaypointX(), w.getWaypointY(), w.getWaypointZ()))
                .collect(Collectors.toSet());

        jmAPI.removeAll("journeymap", DisplayType.Waypoint);

        // Test if any existing waypoints (persistent, usually death waypoints) have already been added to the server, if not, add them
        for (Waypoint existing : existingWaypoints) {
            if (!remoteWaypointPositions.contains(existing.getPosition()) && existing.isPersistent()) {
                createAction(existing, true, false);
                hasLocalWaypoint = true;
            }
        }

        try {
            for (SavedWaypoint savedWaypoint : savedWaypoints) {
                Waypoint wp = new Waypoint(Constants.MODID, savedWaypoint.getName(), 1, new BlockPos(savedWaypoint.getWaypointX(), savedWaypoint.getWaypointY(), savedWaypoint.getWaypointZ()));
                //Waypoint wp = WaypointFactory.fromWaypointJsonString(savedWaypoint.getRawPacketData());
                ObjectIdentifierMap.addWaypointToMap(wp);
                jmAPI.show(wp);
            }
            return hasLocalWaypoint;
        } catch (Exception exception) {
            Constants.LOGGER.error("Could not display server waypoint.");
        }
        return false;*/

        //Constants.LOGGER.info(wa);
        return false;
    }

    public static void syncHandler(JMWSActionMessage waypointPayload, EntityPlayerSP player) {
        boolean hasLocalWaypoint = false;
        boolean sendAlert = waypointPayload.arguments().get(waypointPayload.arguments().size() - 1).getAsBoolean();

        String test = WaypointStore.INSTANCE.getAll().toString();
        Constants.LOGGER.info("WPSTORE; " + test);

        try {
            if (config.uploadWaypoints) {
                hasLocalWaypoint = getInstance().handleUploadWaypoints(waypointPayload.arguments().get(0).getAsJsonObject(), player);
            }

            if (hasLocalWaypoint) {
                updateWaypoints(false);
                PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.local_waypoint_upload"), true, false, JMWSMessageType.SUCCESS);

            } else if (sendAlert) {
                String updateMessageKey = "message.jmws.synced_success";
                PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent(updateMessageKey), true, false, JMWSMessageType.NEUTRAL);
            }

            PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_SUCCEED);
            CommonClass.syncCounter.resetSyncThreshold();

        } catch (IllegalStateException | JsonSyntaxException exception) {
            PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("error.jmws.error_corrupted_waypoint"), true, false, JMWSMessageType.FAILURE);
            PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
        }
    }
}
