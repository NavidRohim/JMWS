package me.brynview.navidrohim.jmws.client.share;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.share.io.ClientShareIO;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ShareRequest {

    public UUID originalSender;
    public UUID meantFor;
    public Object currentSharedObject;
    public FetchType sharedObjectType;
    public String requestIdentifier;

    public enum Direction
    {
        FOR_HOST,
        FOR_CLIENT
    }

    public ShareRequest(@Nullable UUID uuid, @Nullable UUID meantForPlayerUUID, @Nullable Object waypointOrGroup, FetchType sharedObjectType, String requestIdentifier) {
        this.originalSender = uuid;
        this.meantFor = meantForPlayerUUID;
        this.currentSharedObject = waypointOrGroup;
        this.sharedObjectType = sharedObjectType;
        this.requestIdentifier = requestIdentifier;
    }

    public void declineShare()
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestDecline(this.originalSender)));
        IncomingShareRequests.removeIncomingRequest(this.originalSender);
    }

    public static void declareBusy(UUID originalSender)
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestUserBusy(originalSender)));
    }

    public void acceptShare()
    {
        ClientShareIO.addToShareList(this.requestIdentifier);
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestAccept(this)));
        JMWSPlugin.getInstance().addWaypoint(this.currentSharedObject);

        IncomingShareRequests.removeIncomingRequest(this.originalSender);
    }
}
