package me.brynview.navidrohim.jmws.common.enums;

import me.brynview.navidrohim.jmws.client.syncing.SyncRegistry;
import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;
import org.jetbrains.annotations.Nullable;

public enum ObjectType {
    WAYPOINT(ServerWaypoint.class, "./jmws/"),
    GROUP(ServerGroup.class, "./jmws/groups/"),
    SHARED(null, "./jmws/users/"),
    GENERIC(null, null);

    private final Class<? extends ServerObject> savedClass;
    private final String objectPathPrefix;

    ObjectType(final Class<? extends ServerObject> savedClass, String objectPathPrefix) {
        this.savedClass = savedClass;
        this.objectPathPrefix = objectPathPrefix;
    }

    public static String getPathLocationPrefix(ObjectType objectType)
    {
        return objectType.getObjectPathPrefix();
    }

    public Class<? extends ServerObject> getObjectClass() {
        return savedClass;
    }

    public String getObjectPathPrefix()
    {
        return this.objectPathPrefix;
    }

    @Nullable
    public static ObjectType get(String registry)
    {
        for (ObjectType type : ObjectType.values())
        {
            if (type.toString().equals(registry))
            {
                return type;
            }
        }
        return null;
    }
}