package me.brynview.navidrohim.jmws.client.share;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ShareRequest {

    public UUID originalSender;
    public UUID meantFor;
    public Object currentSharedObject;
    public FetchType sharedObjectType;
    public String requestIdentifier;

    protected final ScheduledFuture<?> timeout;



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

        this.timeout = IncomingShareRequests.requestScheduler.schedule(this::timeout, 20, TimeUnit.SECONDS);
    }

    public void decline()
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestDecline(this.originalSender)));
        this.finishRequest();
    }

    public static void busy(UUID originalSender)
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestUserBusy(originalSender)));
    }

    public void accept()
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestAccept(this)));
        JMWSPlugin.getInstance().addWaypoint(this.currentSharedObject);

        this.finishRequest();
    }

    protected void timeout()
    {
        IncomingShareRequests.removeRequest(this.originalSender);
        PlayerHelper.sendUserAlert(Component.translatable("sharing.jmws.request_timeout"), true, false, JMWSMessageType.WARNING);
    }

    private void finishRequest()
    {
        IncomingShareRequests.removeRequest(this.originalSender);
        this.timeout.cancel(true);
    }
}
