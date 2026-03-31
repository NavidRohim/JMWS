package me.brynview.navidrohim.jmws.client.syncing;

import com.google.gson.JsonSyntaxException;
import commonnetwork.api.Dispatcher;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.api.CommonSyncHandler;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.syncing.ServerSyncingHandler;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public class ClientSyncingHandler extends CommonSyncHandler {

    private ObjectType objectType;

    public ClientSyncingHandler(List<String> sharedTo, String identifier, UUID owner, boolean isGlobal) {
        super(sharedTo, identifier, owner, isGlobal);
    }

    private static ClientSyncingHandler getClientSyncingHandlerFromData(String jmwsCustomData, ObjectType objectType)
    {
        try {
            ClientSyncingHandler handler = CommonClass.gson.fromJson(jmwsCustomData, ClientSyncingHandler.class);
            handler.objectType = objectType;

            return handler;
        } catch (IllegalStateException | JsonSyntaxException reader) {
            PlayerUtils.sendUserAlert(Component.literal("FATAL: You are on the wrong JMWS version! Update to JMWS v%s as soon as possible or you may suffer data loss!".formatted(Constants.SERVER_VERSION)), false, true, MessageType.FAILURE);

            return null;
        }
    }

    public static ClientSyncingHandler getClientSyncingHandlerFromWaypoint(Waypoint waypoint)
    {
        return getClientSyncingHandlerFromData(waypoint.getCustomData(Constants.MODID), ObjectType.WAYPOINT);
    }

    public static ClientSyncingHandler getClientSyncingHandlerFromGroup(WaypointGroup waypointGroup)
    {
        return getClientSyncingHandlerFromData(waypointGroup.getCustomData(Constants.MODID), ObjectType.GROUP);
    }

    @Override
    public void addUserToShare(UUID playerUUID) {
        super.addUserToShare(playerUUID);
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeShareRequestForServer(owner, playerUUID, objectIdentifier, objectType)));
    }

    @Override
    public void removeUserFromShare(UUID playerUUID) {
        super.removeUserFromShare(playerUUID);
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeUnshareRequestForUserOnServer(owner, playerUUID, objectIdentifier, objectType)));
    }

    @Override
    public void removeAllFromShare()
    {
        super.removeAllFromShare();
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeUnshareRequestForAllOnServer(owner, objectIdentifier, objectType)));
    }

    @Override
    public void setGlobal(boolean global)
    {
        super.setGlobal(global);
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeGlobalRequestForServer(owner, objectIdentifier, objectType, global)));

    }
}
