package me.brynview.navidrohim.jmws.common.utils;

public enum WaypointPayloadCommand {
    SYNC,
    REQUEST_CLIENT_SYNC,
    CLIENT_ALERT,

    SERVER_CREATE,
    SERVER_HANDSHAKE,

    COMMON_DISPLAY_INTERVAL,
    COMMON_DISPLAY_NEXT_UPDATE,
    COMMON_DELETE_WAYPOINT
}
