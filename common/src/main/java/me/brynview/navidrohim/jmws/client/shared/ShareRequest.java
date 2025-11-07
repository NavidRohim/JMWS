package me.brynview.navidrohim.jmws.client.shared;

import commonnetwork.api.Dispatcher;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ClientVariables;
import me.brynview.navidrohim.jmws.common.helper.CommandHelper;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ShareRequest {
    @Nullable
    public UUID originalSender = null;
    @Nullable
    public Object currentSharedObject = null;

    private static final List<Class<?>> AcceptedObjects = List.of(Waypoint.class, WaypointGroup.class);

    public ShareRequest(UUID uuid, Object waypointOrGroup) {
        Constants.getLogger().info(String.valueOf(waypointOrGroup.getClass()));

        this.originalSender = uuid;
        this.currentSharedObject = waypointOrGroup;

    }

    public void declineShare()
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandHelper.makeObjectShareRequestDecline(this.originalSender)));
        ClientVariables.shareRequest = null;
    }

    public static void declareBusy(UUID originalSender)
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandHelper.makeObjectShareRequestUserBusy(originalSender)));
    }

    public void acceptShare()
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandHelper.makeObjectShareRequestAccept(this.originalSender)));
        ClientVariables.shareRequest = null;
    }
}
