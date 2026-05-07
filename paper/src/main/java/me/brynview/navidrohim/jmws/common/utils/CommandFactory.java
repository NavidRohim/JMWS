package me.brynview.navidrohim.jmws.common.utils;

import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.enums.ShareRequestDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandFactory {
    public static class PacketCommand {
        Commands command;
        Object[] arguments;

        public PacketCommand(Commands command, Object... arguments) {
            this.command = command;
            this.arguments = arguments;
        }
    }

    public static String makeBaseJsonRequest(Commands command, Object... arguments) {
        return CommonClass.gson.toJson(new PacketCommand(command, arguments));
    }

    public static String makeDeleteRequestJson(String waypointIdentifier, boolean silent, boolean all) {
        return CommandFactory.makeBaseJsonRequest(Commands.COMMON_DELETE_WAYPOINT, waypointIdentifier, silent, all);
    }

    public static String makeDeleteGroupRequestJson(String groupUniversalIdentifier, String groupGUID, boolean silent, boolean removeAllWaypointsInGroup, boolean removeGroupItself, boolean isGlobal, boolean deleteAllGroups) {
        return CommandFactory.makeBaseJsonRequest(Commands.COMMON_DELETE_GROUP,
                groupUniversalIdentifier,
                groupGUID,
                silent,
                removeAllWaypointsInGroup,
                removeGroupItself,
                isGlobal,
                deleteAllGroups);
    }

    public static String makeWaypointSyncRequestJson(boolean sendAlert, boolean isForDeathSync) {
        return CommandFactory.makeBaseJsonRequest(Commands.SYNC, Map.of(), Map.of(), sendAlert, isForDeathSync);
    }

    public static String makeCreationRequestJson(Object waypoint, boolean silent) {
        return CommandFactory.makeBaseJsonRequest(Commands.SERVER_CREATE, waypoint.toString(), silent);
    }

    public static String makeGroupCreationRequestJson(Object waypointGroup, boolean silent) {
        return CommandFactory.makeBaseJsonRequest(Commands.SERVER_CREATE_GROUP, waypointGroup.toString(), silent, false);
    }

    public static String makeSyncRequestResponseJson(HashMap<String, String> jsonArray, HashMap<String, String> jsonGroupArray, boolean sendAlert, boolean isDeathSync) {
        return CommandFactory.makeBaseJsonRequest(Commands.SYNC, jsonArray, jsonGroupArray, sendAlert, isDeathSync);
    }

    public static String makeClientAlertRequestJson(String message, boolean overlay, MessageType messageType) {
        return CommandFactory.makeBaseJsonRequest(Commands.CLIENT_ALERT, message, overlay, messageType);
    }

    public static String makeObjectShareRequestForUser(String waypoint, UUID to, UUID from, ShareRequestDirection direction, ObjectType objectType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.OBJECT_SHARE, waypoint, to, from, objectType, direction);
    }

    public static String makeObjectShareRequestDecline(UUID originalSender, UUID decliningUser)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.REJECT_SHARE, originalSender, decliningUser);
    }

    public static String makeObjectShareRequestDeclineWithMessage(UUID originalSender, UUID decliningUser, String messageKey)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.USER_ALREADY_PROCESSING_SHARE, originalSender, decliningUser, messageKey);
    }

    public static String makeObjectShareRequestAccept(UUID originalSender, String requestIdentifier, ObjectType sharedObjectType, UUID meantFor)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.AFFIRM_SHARE, originalSender, requestIdentifier, sharedObjectType, meantFor);
    }

    public static String makeUpdateObjectRequest(String objectIdentifier, ObjectType objectType, boolean isGlobal, Object objectData)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.UPDATE, objectIdentifier, objectType, isGlobal, objectData.toString());
    }

    public static String makeTransitionObjectRequest(String objectIdentifier, String filename, ObjectType transitionType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.TRANSITION, objectIdentifier, filename, transitionType);
    }

    public static String makeTransitionObjectRequestForLegacyCustomData(String objectIdentifier, UUID owner, boolean isGlobal, ObjectType transitionType)
    {
        return CommandFactory.makeBaseJsonRequest(Commands.TRANSITION_NEW_DATA, objectIdentifier, owner, isGlobal, transitionType);
    }

    public enum Commands {
        SERVER_CREATE,
        COMMON_DELETE_WAYPOINT,
        SERVER_CREATE_GROUP,
        COMMON_DELETE_GROUP,
        SYNC,
        REQUEST_CLIENT_SYNC,
        CLIENT_ALERT,
        COMMON_DISPLAY_INTERVAL,
        COMMON_DISPLAY_NEXT_UPDATE,
        OBJECT_SHARE,
        AFFIRM_SHARE,
        REJECT_SHARE,
        USER_ALREADY_PROCESSING_SHARE,
        UPDATE,
        TRANSITION,
        TRANSITION_NEW_DATA
    }
}
