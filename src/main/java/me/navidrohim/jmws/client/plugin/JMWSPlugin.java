package me.navidrohim.jmws.client.plugin;


import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.event.DeathWaypointEvent;
import journeymap.client.model.Waypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.waypoint.WaypointEditor;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ClientCommandHandler;
import scala.tools.nsc.backend.icode.analysis.CopyPropagation;

import java.util.*;
import java.util.stream.Collectors;

import static me.navidrohim.jmws.CommonClass.config;

@ClientPlugin
public class JMWSPlugin implements IClientPlugin {

    // Variables

    // JourneyMap API
    private IClientAPI jmAPI = null;
    private static JMWSPlugin INSTANCE;

    public static void updateEvent(journeymap.client.model.Waypoint originalWaypoint, journeymap.client.model.Waypoint editedWaypoint) {
    }

    // Required functions

    @Override
    public void initialize(IClientAPI jmClientApi)
    {

        this.jmAPI = jmClientApi;
        this.jmAPI.subscribe(Constants.MODID, EnumSet.of(ClientEvent.Type.DEATH_WAYPOINT));
    }

    @Override
    public String getModId() {
        return Constants.MODID;
    }

    @Override
    public void onEvent(ClientEvent clientEvent)
    {
        if (CommonClass.getEnabledStatus())
        {
            DeathWaypointEvent deathWaypointEvent = (DeathWaypointEvent) clientEvent;

            clientEvent.cancel();
            Waypoint deathpoint = Waypoint.at(deathWaypointEvent.location, Waypoint.Type.Death, deathWaypointEvent.dimension);

            createAction(deathpoint, true, false);
        }
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

    public static void createAction(journeymap.client.model.Waypoint waypoint, boolean silent, boolean isUpdate)
    {
        WaypointStore.INSTANCE.remove(waypoint);
        waypoint.setPersistent(false);

        String creationData = CommandHelper.makeCreationRequestJson(waypoint.toString(), silent, isUpdate);
        JMWSNetworkWrapper.INSTANCE.sendToServer(new JMWSActionMessage(creationData));

    }

    public void updateAction(Waypoint waypoint, Waypoint oldWaypoint)
    {
        if (oldWaypoint != null) {
            this.deleteAction(oldWaypoint, true);
        }

        createAction(waypoint, true, true);
        PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("message.jmws.modified_waypoint_success"), true, false, JMWSMessageType.SUCCESS);
    }

    public void deleteAction(Waypoint waypoint, boolean silent) {

        String waypointFilename = CommonHelper.getWaypointFilename(waypoint, CommonClass.minecraftClientInstance.player.getUniqueID());

        String jsonPacketData = CommandHelper.makeDeleteRequestJson(waypointFilename, silent, false);
        JMWSActionMessage waypointActionPayload = new JMWSActionMessage(jsonPacketData);

        //jmAPI.remove(waypoint);
        JMWSNetworkWrapper.INSTANCE.sendToServer(waypointActionPayload);
    }

    // Handling packets
    public void deleteSavedObjects(Boolean deleteAll, String toDelete)
    {
        String deletionMessageConfirmationKey = "message.jmws.deletion_all_success";
        if (deleteAll) {
            INSTANCE.jmAPI.removeAll("journeymap");
        }
        /*else {
            INSTANCE.jmAPI.remove(ObjectIdentifierMap.getOldWaypoint(toDelete));
        }*/


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
        boolean hasLocalWaypoint = false;

        // Get existing waypoints (local) and get waypoint objects saved on server
        Collection<journeymap.client.model.Waypoint> existingWaypoints = WaypointStore.INSTANCE.getAll();
        Set<SavedWaypoint> savedWaypoints = JMWSPlugin.getSavedWaypoints(jsonWaypoints, player.getUniqueID()); //POTENTIAL ISSUE

        // Get an identifier of every waypoint (BlockPos, location), used to detect if the waypoint already exists
        Set<Vec3d> remoteWaypointPositions = savedWaypoints.stream()
                .map(w -> new Vec3d(w.getWaypointX(), w.getWaypointY(), w.getWaypointZ()))
                .collect(Collectors.toSet());

        //jmAPI.removeAll("journeymap");
        //jmAPI.removeAll(Constants.MODID);
        WaypointStore.INSTANCE.reset();

        // Test if any existing waypoints (persistent, usually death waypoints) have already been added to the server, if not, add them
        for (journeymap.client.model.Waypoint existing : existingWaypoints) {
            if (!remoteWaypointPositions.contains(existing.getPosition()) && existing.isPersistent()) {
                createAction(existing, true, false);
                hasLocalWaypoint = true;
            }
        }

        try {
            for (SavedWaypoint savedWaypoint : savedWaypoints) {
                journeymap.client.model.Waypoint wp = journeymap.client.model.Waypoint.fromString(savedWaypoint.getRawPacketData());
                int[] numArray = wp.getDimensions().stream()
                        .mapToInt(Integer::intValue)
                        .toArray();

                int primaryDim = Iterables.get(wp.getDimensions(), 0);
                journeymap.client.api.display.Waypoint displayableWp = new journeymap.client.api.display.Waypoint(Constants.MODID, wp.getName(), primaryDim, new BlockPos(wp.getPosition()))
                        .setPersistent(false)
                        .setDisplayDimensions(numArray)
                        .setColor(wp.getColor());

                jmAPI.show(displayableWp);
            }
            return hasLocalWaypoint;
        } catch (Exception exception) {
            Constants.LOGGER.error("Could not display server waypoint.");
        }
        return false;
    }

    public static void syncHandler(JMWSActionMessage waypointPayload, EntityPlayerSP player) {
        boolean hasLocalWaypoint = false;
        boolean sendAlert = waypointPayload.arguments().get(waypointPayload.arguments().size() - 1).getAsBoolean();

        String test = WaypointStore.INSTANCE.getAll().toString();
        /*
        try {*/
        if (CommonClass.getEnabledStatus()) {
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

        /*
        } catch (IllegalStateException | JsonSyntaxException exception) {
            PlayerHelper.sendUserAlert(CommonHelper.getTranslatableComponent("error.jmws.error_corrupted_waypoint"), true, false, JMWSMessageType.FAILURE);
            PlayerHelper.sendUserSoundAlert(JMWSSounds.ACTION_FAILURE);
        }*/
    }
}
