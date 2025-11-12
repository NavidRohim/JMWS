package me.brynview.navidrohim.jmws.common.enums;

import me.brynview.navidrohim.jmws.common.objects.SavedGroup;
import me.brynview.navidrohim.jmws.common.objects.SavedObject;
import me.brynview.navidrohim.jmws.common.objects.SavedWaypoint;

public enum FetchType {
    WAYPOINT(SavedWaypoint.class),
    GROUP(SavedGroup.class),
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