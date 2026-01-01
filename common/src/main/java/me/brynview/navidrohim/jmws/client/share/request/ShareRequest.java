package me.brynview.navidrohim.jmws.client.share.request;

import com.mojang.authlib.GameProfile;
import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
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
    public ObjectType sharedObjectType;
    public String requestIdentifier;
    public String objectDisplayName;

    @Nullable public GameProfile sender;
    @Nullable public GameProfile to;

    public final ScheduledFuture<?> timeout;

    public enum Direction
    {
        FOR_HOST,
        FOR_CLIENT
    }

    public ShareRequest(@Nullable UUID uuid, @Nullable UUID meantForPlayerUUID, @Nullable Object waypointOrGroup, ObjectType sharedObjectType, String requestIdentifier, String objectDisplayName) {
        this.originalSender = uuid;
        this.meantFor = meantForPlayerUUID;
        this.currentSharedObject = waypointOrGroup;
        this.sharedObjectType = sharedObjectType;
        this.requestIdentifier = requestIdentifier;
        this.objectDisplayName = objectDisplayName;

        PlayerHelper.getUserFromUUID(uuid).ifPresentOrElse(p -> {this.sender = p;}, () -> {this.sender = null;});
        PlayerHelper.getUserFromUUID(meantForPlayerUUID).ifPresentOrElse(pFor -> {this.to = pFor;}, () -> {this.sender = null;});

        this.timeout = IncomingShareRequests.requestScheduler.schedule(this::timeout, 20, TimeUnit.SECONDS);
    }

    public ShareRequest resolve()
    {
        this.finishRequest();
        return this;
    }

    public void decline()
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestDecline(this.originalSender)));
        this.finishRequest();
    }

    public static void busy(UUID originalSender)
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestDeclineWithMessage(originalSender, "sharing.jmws.share_busy")));
    }

    public static void disabled(UUID originalSender) {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestDeclineWithMessage(originalSender, "sharing.jmws.disabled")));
    }

    public void accept()
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeObjectShareRequestAccept(this)));
        JMWSPlugin.getInstance().addObjectFromRequest(this);

        this.finishRequest();
    }

    protected void timeout()
    {
        IncomingShareRequests.removeRequest(this.originalSender);
        PlayerHelper.sendUserAlert(Component.translatable("sharing.jmws.request_timeout_from", this.getSenderName()), true, false, JMWSMessageType.WARNING);
    }

    private void finishRequest()
    {
        IncomingShareRequests.removeRequest(this.originalSender);
        this.timeout.cancel(true);
    }

    public String getSenderName()
    {
        return sender != null ? sender.name() : CommonHelper.unknownUser;
    }

    public String getRecipientName()
    {
    return to != null ? to.name() : CommonHelper.unknownUser;
    }
}

