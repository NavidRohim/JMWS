package me.brynview.navidrohim.jmws.common.enums;

import me.brynview.navidrohim.jmws.server.objects.ServerGroup;
import me.brynview.navidrohim.jmws.server.objects.ServerWaypoint;

public enum FetchType {
    WAYPOINT(ServerWaypoint.class),
    GROUP(ServerGroup.class),
    SHARED(null),
    GENERIC(null);

    private final Class savedClass;

    FetchType(final Class savedClass) {
        this.savedClass = savedClass;
    }

    public Class getObjectClass() {
        return savedClass;
    }
}