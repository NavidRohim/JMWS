package me.brynview.navidrohim.jmws.common.enums;

/**
 * Enums for different packet commands
 */

public enum ObjectPayloadCommands {

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

    // Object sharing
    OBJECT_SHARE, // Share waypoint / group
    AFFIRM_SHARE, // Confirm user wants shared object
    REJECT_SHARE, // User doesnt want shared object.

    // Object sharing errors
    USER_ALREADY_PROCESSING_SHARE // User is already processing another share request

}
