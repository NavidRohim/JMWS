package me.brynview.navidrohim.jmws.client.plugin;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.client.utils.LegacyUtils;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.utils.SyncUtils;
import me.brynview.navidrohim.jmws.server.syncing.ServerSyncingHandler;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import static me.brynview.navidrohim.jmws.common.CommonClass.*;
import static me.brynview.navidrohim.jmws.common.utils.SyncUtils.isLegacySyncField;

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

    private static final HashMap<String, ClientBaseObjectWrapper<Object>> clientObjectMap = new HashMap<>();
    private static final HashMap<String, ClientWaypointWrapper> waypointIdentifierMapForContextMenu = new HashMap<>();

    private static String getContextMenuKey(Waypoint waypoint)
    {
        return "%s%s%s".formatted(waypoint.getName(), waypoint.getColor(), waypoint.getX());
    }

    /**
     * Creates a universal identifier from the players UUID, the waypoints GUID and name of the object.
     * @param waypointGUID -- GUID of the object being created.
     * @param objectName -- The name of the waypoint or group.
     * @return String -- The universal identifier.
     */
    public static String makeWaypointHash(String waypointGUID, String objectName)
    {
        return DigestUtils.sha256Hex(PlayerUtils.ourUUID() + waypointGUID + objectName);
    }

    // Getters

    /**
     * Get an old waypoint from a unique identifier.
     * @param waypointID -- The waypoints unique identifier.
     * @return Waypoint -- The old waypoint before update.
     */
    public static Waypoint getWaypoint(String waypointID)
    {
        return waypointIdentifierMap.get(waypointID);
    }

    /**
     * Get an old group from a unique identifier.
     * @param groupID -- The groups unique identifier.
     * @return Waypoint -- The old group before update.
     */
    public static WaypointGroup getGroup(String groupID)
    {
        return groupIdentifierMap.get(groupID);
    }

    public static Waypoint getWaypointFromContextMenu(Waypoint waypoint)
    {
        return waypointIdentifierMapForContextMenu.get(getContextMenuKey(waypoint));
    }

    // Adding

    public static boolean addObjectToMap(ClientBaseObjectWrapper<Object> object, boolean silent, boolean createRemotely)
    {
        if (object.isUsable() || object.isNative())
        {
            if (createRemotely)
            {
                object.createRemotely(silent);
            }
            if (object instanceof ClientWaypointWrapper)
            {
                waypointIdentifierMapForContextMenu.put(object.getIdentifier(), object);
            }
            clientObjectMap.put(object.getIdentifier(), object);

            return true;
        }
        return false;
    }

    @Nullable
    public static <T extends ClientBaseObjectWrapper<Object>> T getObjectFromMap(String identifier, Class<T> clazz)
    {
        ClientBaseObjectWrapper<Object> obj = clientObjectMap.get(identifier);
        if ( obj != null && clazz.isAssignableFrom(obj.getClass()))
        {
            return (T) obj;
        }
        return null;

    }

    public static boolean removeObjectFromMap(ClientBaseObjectWrapper<Object> object, boolean silent, boolean deleteRemotely)
    {
        if (object.isUsable())
        {
            clientObjectMap.remove(object.getIdentifier());

            if (deleteRemotely)
            {
                object.removeRemotely(silent);
            }

            return true;
        }
        return false;
    }
    /**
     * Adds a group to the identifier map.
     * @param waypointGroup -- The group that will be added to the map.
     */
    public static boolean addGroupToMap(WaypointGroup waypointGroup)
    {
        String customDataField = waypointGroup.getCustomData(Constants.MODID);
        if (isLegacySyncField(customDataField))
        {
            LegacyUtils.transitionObject(waypointGroup, PlayerUtils.ourUUID(), ObjectType.GROUP);
            return false;
        } else {
            String groupIdentifier;
            @Nullable ServerSyncingHandler groupSyncInfo = me.brynview.navidrohim.jmws.common.utils.SyncUtils.getSyncingInfo(waypointGroup.getCustomData(Constants.MODID), true);
            if (groupSyncInfo != null)
            {
                groupIdentifier = groupSyncInfo.objectIdentifier;
            } else {
                groupIdentifier = makeWaypointHash(waypointGroup.getGuid(), waypointGroup.getName());
                waypointGroup.setCustomData(Constants.MODID, SyncUtils.getEmptySyncingInfoString(groupIdentifier, minecraftClientInstance.player.getUUID(), false));
            }

            groupIdentifierMap.put(groupIdentifier, waypointGroup);
            return true;
        }
    }

    // Removing

    /**
     * Removes a waypoint from the identifier map.
     * @param waypoint -- The waypoint that will be removed from the map.
     */
    public static void removeWaypointFromMap(Waypoint waypoint)
    {
        try
        {
            waypointIdentifierMapForContextMenu.remove(getContextMenuKey(waypoint));
            waypointIdentifierMap.remove(me.brynview.navidrohim.jmws.common.utils.SyncUtils.getSyncingInfo(waypoint.getCustomData(Constants.MODID)).objectIdentifier);
        } catch (NullPointerException _) {}
    }

    /**
     * Removes a group from the identifier map.
     * @param group -- The group that will be removed from the map.
     */
    public static void removeGroupFromMap(WaypointGroup group)
    {
        try
        {
            @Nullable ServerSyncingHandler groupSyncInfo = me.brynview.navidrohim.jmws.common.utils.SyncUtils.getSyncingInfo(group.getCustomData(Constants.MODID));
            if (groupSyncInfo != null)
                groupIdentifierMap.remove(groupSyncInfo.objectIdentifier);
        } catch (NullPointerException _) {}
    }

    public static void removeAll(ObjectType objectType)
    {
        if (objectType == ObjectType.WAYPOINT)
        {
            waypointIdentifierMap.clear();
        } else {
            groupIdentifierMap.clear();
        }
    }
}
