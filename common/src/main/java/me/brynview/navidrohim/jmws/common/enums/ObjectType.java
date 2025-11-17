package me.brynview.navidrohim.jmws.common.enums;

import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;

public enum ObjectType {
    WAYPOINT(ServerWaypoint.class),
    GROUP(ServerGroup.class),
    SHARED(null),
    GENERIC(null);

    private final Class savedClass;

    ObjectType(final Class savedClass) {
        this.savedClass = savedClass;
    }

    public Class getObjectClass() {
        return savedClass;
    }
}