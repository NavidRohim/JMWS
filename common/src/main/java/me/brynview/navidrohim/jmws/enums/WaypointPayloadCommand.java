package me.brynview.navidrohim.jmws.enums;

public enum WaypointPayloadCommand {

    // Waypoint handling
    SERVER_CREATE,
    COMMON_DELETE_WAYPOINT,

    // Group handling
    SERVER_CREATE_GROUP,
    COMMON_DELETE_GROUP,

    // Utility
    SYNC,
    REQUEST_CLIENT_SYNC,
    CLIENT_ALERT,
    COMMON_DISPLAY_INTERVAL,
    COMMON_DISPLAY_NEXT_UPDATE,
}
