package me.brynview.navidrohim.jmws.client.plugin;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.utils.ObjectUtils;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

import static me.brynview.navidrohim.jmws.common.CommonClass.*;

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

    private static boolean isLegacyDataField(@Nullable String field) {

        if (field != null)
        {
            for (int i = 0; i < field.length(); i++) {
                char c = field.charAt(i);
                if (!Character.isLetterOrDigit(c))
                    return false;
            }

            return field.length() == 64;
        }
        return true;
    }
    /**
     * Get an old waypoint from a new waypoint (unique identifier)
     * @param newWaypoint -- The new waypoint being updated.
     * @return Waypoint -- The old waypoint before update.
     */
    public static Waypoint getOldWaypoint(Waypoint newWaypoint) {
        ServerObject.SyncingInformation persistentWaypointID = ServerObject.SyncingInformation.getSyncingInfo(newWaypoint.getCustomData());
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
        return groupIdentifierMap.get(ServerObject.SyncingInformation.getSyncingInfo(newWaypointGroup.getCustomData()).objectIdentifier);
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
    public static void addWaypointToMap(Waypoint waypoint)
    {
        String customDataField = waypoint.getCustomData();
        if (isLegacyDataField(customDataField))
        {
            ObjectUtils.transitionObject(customDataField, PlayerHelper.ourUUID(), ObjectType.WAYPOINT);
        }

        String waypointIdentifier;
        @Nullable ServerObject.SyncingInformation waypointSyncInfo = ServerObject.SyncingInformation.getSyncingInfo(customDataField, true);
        if (waypointSyncInfo != null)
        {
            waypointIdentifier = waypointSyncInfo.objectIdentifier;
        } else {
            waypointIdentifier = makeWaypointHash(minecraftClientInstance.player.getUUID(), waypoint.getGuid(), waypoint.getName());
            waypoint.setCustomData(ServerObject.SyncingInformation.getEmptySyncingInfoString(waypointIdentifier, minecraftClientInstance.player.getUUID(), false));
        }

        waypointIdentifierMap.put(waypointIdentifier, waypoint);
    }

    /**
     * Adds a group to the identifier map.
     * @param waypointGroup -- The group that will be added to the map.
     */
    public static void addGroupToMap(WaypointGroup waypointGroup)
    {
        String waypointIdentifier;
        @Nullable ServerObject.SyncingInformation waypointSyncInfo = ServerObject.SyncingInformation.getSyncingInfo(waypointGroup.getCustomData(), true);
        if (waypointSyncInfo != null)
        {
            waypointIdentifier = waypointSyncInfo.objectIdentifier;
        } else {
            waypointIdentifier = makeWaypointHash(minecraftClientInstance.player.getUUID(), waypointGroup.getGuid(), waypointGroup.getName());
            waypointGroup.setCustomData(ServerObject.SyncingInformation.getEmptySyncingInfoString(waypointIdentifier, minecraftClientInstance.player.getUUID(), false));
        }

        groupIdentifierMap.put(waypointIdentifier, waypointGroup);
    }

    /**
     * Removes a waypoint from the identifier map.
     * @param waypoint -- The waypoint that will be removed from the map.
     */
    public static void removeWaypointFromMap(Waypoint waypoint)
    {
        waypointIdentifierMap.remove(ServerObject.SyncingInformation.getSyncingInfo(waypoint.getCustomData()).objectIdentifier);
    }

    /**
     * Removes a group from the identifier map.
     * @param group -- The group that will be removed from the map.
     */
    public static void removeGroupFromMap(WaypointGroup group)
    {
        @Nullable ServerObject.SyncingInformation groupSyncInfo = ServerObject.SyncingInformation.getSyncingInfo(group.getCustomData());
        if (groupSyncInfo != null)
            groupIdentifierMap.remove(groupSyncInfo.objectIdentifier);
    }
}
