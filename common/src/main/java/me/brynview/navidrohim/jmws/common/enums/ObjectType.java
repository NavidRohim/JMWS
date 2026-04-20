package me.brynview.navidrohim.jmws.common.enums;

import me.brynview.navidrohim.jmws.client.syncing.SyncRegistry;
import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;
import org.jetbrains.annotations.Nullable;

public enum ObjectType {
    WAYPOINT(ServerWaypoint.class),
    GROUP(ServerGroup.class),
    SHARED(null),
    GENERIC(null, null);

    private final Class<? extends ServerObject> savedClass;
    private final @Nullable String objectPathPrefix;

    ObjectType(final Class<? extends ServerObject> savedClass, @Nullable String objectPathPrefix) {
        this.savedClass = savedClass;
        this.objectPathPrefix = objectPathPrefix;
    }

    ObjectType(final Class<? extends ServerObject> savedClass)
    {
        this.savedClass = savedClass;
        this.objectPathPrefix = "./jmws/%s/".formatted(this.name());
    }

    public static @Nullable String getPathLocationPrefix(ObjectType objectType)
    {
        return objectType.getObjectPathPrefix();
    }

    public Class<? extends ServerObject> getObjectClass() {
        return savedClass;
    }

    public @Nullable String getObjectPathPrefix()
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