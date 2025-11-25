package me.brynview.navidrohim.jmws.client.plugin;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.utils.ObjectUtils;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.syncing.SyncingInformation;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

import static me.brynview.navidrohim.jmws.common.CommonClass.*;
import static me.brynview.navidrohim.jmws.server.objects.LegacyObject.isLegacyDataField;

/**
 * Note: the term "object" may be used. In this context is a generic term for waypoints or groups.
 * This static class keeps track of all waypoints and groups.
 * Due to internal limitations, waypoints and groups have no unique identifier that stays constant for their whole lifespan.
 * This is bad because waypoints and groups must be updated. When a waypoint or group is updated, we would have no way of keeping track of that updated waypoint
 * due to again, there is no universal identifier. So, when a waypoint or group is created, we create a universal identifier and store it here.
 */
public class ObjectIdentifierMap {

    // Waypoint identifier map (key is the universal identifier, value is the waypoint)
    private static final HashMap<String, Waypoint> waypointIdentifierMap = new HashMap<>();

    // Group identifier map
    private static final HashMap<String, WaypointGroup> groupIdentifierMap = new HashMap<>();

    /**
     * Creates a universal identifier from the players UUID, the waypoints GUID and name of the object.
     * @param playerUUID -- UUID of the player who is creating a waypoint.
     * @param waypointGUID -- GUID of the object being created.
     * @param objectName -- The name of the waypoint or group.
     * @return String -- The universal identifier.
     */
    private static String makeWaypointHash(UUID playerUUID, String waypointGUID, String objectName)
    {
        return DigestUtils.sha256Hex(playerUUID.toString() + waypointGUID + objectName);
    }

    /**
     * Get an old waypoint from a new waypoint (unique identifier)
     * @param newWaypoint -- The new waypoint being updated.
     * @return Waypoint -- The old waypoint before update.
     */
    public static Waypoint getOldWaypoint(Waypoint newWaypoint) {
        SyncingInformation persistentWaypointID = SyncingInformation.getSyncingInfo(newWaypoint.getCustomData());
        if (persistentWaypointID != null)
        {
            return waypointIdentifierMap.get(persistentWaypointID.objectIdentifier);
        }
        return null;
    }

    /**
     * Get an old waypoint from a unique identifier.
     * @param waypointID -- The waypoints unique identifier.
     * @return Waypoint -- The old waypoint before update.
     */
    public static Waypoint getOldWaypoint(String waypointID)
    {
        return waypointIdentifierMap.get(waypointID);
    }

    /**
     * Get an old group from a new group (unique identifier)
     * @param newWaypointGroup -- The new group being updated.
     * @return Waypoint -- The old group before update.
     */
    public static WaypointGroup getOldGroup(WaypointGroup newWaypointGroup)
    {
        return groupIdentifierMap.get(SyncingInformation.getSyncingInfo(newWaypointGroup.getCustomData()).objectIdentifier);
    }

    /**
     * Get an old group from a unique identifier.
     * @param groupID -- The groups unique identifier.
     * @return Waypoint -- The old group before update.
     */
    public static WaypointGroup getOldGroup(String groupID)
    {
        return groupIdentifierMap.get(groupID);
    }

    /**
     * Adds a waypoint to the identifier map.
     * @param waypoint -- The waypoint that will be added to the map.
     */
    public static boolean addWaypointToMap(Waypoint waypoint)
    {
        String customDataField = waypoint.getCustomData();
        if (isLegacyDataField(customDataField))
        {
            ObjectUtils.transitionObject(waypoint, PlayerHelper.ourUUID(), ObjectType.WAYPOINT);
            return false;
        } else {
            String waypointIdentifier;
            @Nullable SyncingInformation waypointSyncInfo = SyncingInformation.getSyncingInfo(customDataField, true);
            if (waypointSyncInfo != null)
            {
                waypointIdentifier = waypointSyncInfo.objectIdentifier;
            } else {
                waypointIdentifier = makeWaypointHash(minecraftClientInstance.player.getUUID(), waypoint.getGuid(), waypoint.getName());
                waypoint.setCustomData(SyncingInformation.getEmptySyncingInfoString(waypointIdentifier, minecraftClientInstance.player.getUUID(), false));
            }

            waypointIdentifierMap.put(waypointIdentifier, waypoint);
            return true;
        }
    }

    /**
     * Adds a group to the identifier map.
     * @param waypointGroup -- The group that will be added to the map.
     */
    public static boolean addGroupToMap(WaypointGroup waypointGroup)
    {
        String customDataField = waypointGroup.getCustomData();

        if (isLegacyDataField(customDataField))
        {
            ObjectUtils.transitionObject(waypointGroup, PlayerHelper.ourUUID(), ObjectType.GROUP);
            return false;
        } else {
            String groupIdentifier;
            @Nullable SyncingInformation groupSyncInfo = SyncingInformation.getSyncingInfo(waypointGroup.getCustomData(), true);
            if (groupSyncInfo != null)
            {
                groupIdentifier = groupSyncInfo.objectIdentifier;
            } else {
                groupIdentifier = makeWaypointHash(minecraftClientInstance.player.getUUID(), waypointGroup.getGuid(), waypointGroup.getName());
                waypointGroup.setCustomData(SyncingInformation.getEmptySyncingInfoString(groupIdentifier, minecraftClientInstance.player.getUUID(), false));
            }

            groupIdentifierMap.put(groupIdentifier, waypointGroup);
            return true;
        }
    }

    /**
     * Removes a waypoint from the identifier map.
     * @param waypoint -- The waypoint that will be removed from the map.
     */
    public static void removeWaypointFromMap(Waypoint waypoint)
    {
        waypointIdentifierMap.remove(SyncingInformation.getSyncingInfo(waypoint.getCustomData()).objectIdentifier);
    }

    /**
     * Removes a group from the identifier map.
     * @param group -- The group that will be removed from the map.
     */
    public static void removeGroupFromMap(WaypointGroup group)
    {
        @Nullable SyncingInformation groupSyncInfo = SyncingInformation.getSyncingInfo(group.getCustomData());
        if (groupSyncInfo != null)
            groupIdentifierMap.remove(groupSyncInfo.objectIdentifier);
    }
}
