package me.brynview.navidrohim.jmws.common.enums;

import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;
import me.brynview.navidrohim.jmws.common.platform.Services;

import java.io.File;
import java.nio.file.Path;

public enum ObjectType {
    WAYPOINT(ServerWaypoint.class),
    GROUP(ServerGroup.class),
    SHARED(null),
    GENERIC(null);

    private final Class<? extends ServerObject> savedClass;

    ObjectType(final Class<? extends ServerObject> savedClass) {
        this.savedClass = savedClass;
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
        Path directory = switch (this) {
            case WAYPOINT -> Services.PLATFORM.getWaypointDirectory();
            case GROUP -> Services.PLATFORM.getGroupDirectory();
            case SHARED -> Services.PLATFORM.getUserDirectory();
            case GENERIC -> null;
        };

        return directory != null ? directory.toString() + File.separator : null;
    }
}
