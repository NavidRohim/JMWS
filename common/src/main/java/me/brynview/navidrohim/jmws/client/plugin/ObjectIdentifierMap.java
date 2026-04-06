package me.brynview.navidrohim.jmws.client.plugin;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Note: the term "object" may be used. In this context is a generic term for waypoints or groups.
 * This static class keeps track of all waypoints and groups.
 * Due to internal limitations, waypoints and groups have no unique identifier that stays constant for their whole lifespan.
 * This is bad because waypoints and groups must be updated. When a waypoint or group is updated, we would have no way of keeping track of that updated waypoint
 * due to again, there is no universal identifier. So, when a waypoint or group is created, we create a universal identifier and store it here.
 */
public class ObjectIdentifierMap {

    // I think nested HashMap is a bad idea.
    private static final HashMap<Class<?>, HashMap<String, ClientBaseObjectWrapper<Object>>> clientObjectMap = new HashMap<>();
    private static final HashMap<String, ClientWaypointWrapper> waypointIdentifierMapForContextMenu = new HashMap<>();

    private static String getContextMenuKey(Waypoint waypoint)
    {
        return "%s%s%s".formatted(waypoint.getName(), waypoint.getColor(), waypoint.getX());
    }

    public static ClientWaypointWrapper getWaypointFromContextMenu(Waypoint waypoint)
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

            if (object instanceof ClientWaypointWrapper wp)
            {
                waypointIdentifierMapForContextMenu.put(getContextMenuKey(wp.getNativeObject()), wp);
            }

            if (!clientObjectMap.containsKey(object.getClass()))
            {
                clientObjectMap.put(object.getClass(), new HashMap<>());
            }

            clientObjectMap.get(object.getClass()).put(object.getIdentifier(), object);
            return true;
        }
        return false;
    }

    @Nullable
    public static <T extends ClientBaseObjectWrapper<Object>> T getObjectFromMap(String identifier, Class<T> clazz)
    {
        if (!clientObjectMap.containsKey(clazz))
        {
            return null;
        }

        ClientBaseObjectWrapper<Object> obj = clientObjectMap.get(clazz).get(identifier);

        if ( obj != null && clazz.isAssignableFrom(obj.getClass()))
        {
            return (T) obj;
        }
        return null;

    }

    @Nullable
    public static HashMap<String, ClientBaseObjectWrapper<Object>> getObjectsOfClass(Class<?> clazz)
    {
        return clientObjectMap.get(clazz);
    }

    public static void removeObjectFromMap(ClientBaseObjectWrapper<Object> object, boolean silent, boolean deleteRemotely)
    {
        if (object.isUsable())
        {
            if (clientObjectMap.containsKey(object.getClass()))
            {
                clientObjectMap.get(object.getClass()).remove(object.getIdentifier());
            }

            if (deleteRemotely)
            {
                object.removeRemotely(silent);
            }

        }
    }

    public static void removeAllOfObjectFromMap(Class<?> clazz)
    {
        clientObjectMap.remove(clazz);
    }
}
