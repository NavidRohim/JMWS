package me.brynview.navidrohim.jmws.client.share.request;

import com.mojang.authlib.GameProfile;
import me.brynview.navidrohim.jmws.client.ClientCommonClass;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;
import me.brynview.navidrohim.jmws.common.utils.CommonUtils;
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

    public ShareRequest(@Nullable UUID uuid, @Nullable UUID meantForPlayerUUID, @Nullable Object waypointOrGroup, ObjectType sharedObjectType, String requestIdentifier, String objectDisplayName) {
        this.originalSender = uuid;
        this.meantFor = meantForPlayerUUID;
        this.currentSharedObject = waypointOrGroup;
        this.sharedObjectType = sharedObjectType;
        this.requestIdentifier = requestIdentifier;
        this.objectDisplayName = objectDisplayName;

        PlayerUtils.getUserFromUUID(uuid).ifPresentOrElse(p -> {this.sender = p;}, () -> {this.sender = null;});
        PlayerUtils.getUserFromUUID(meantForPlayerUUID).ifPresentOrElse(pFor -> {this.to = pFor;}, () -> {this.sender = null;});

        this.timeout = IncomingShareRequests.requestScheduler.schedule(this::timeout, 20, TimeUnit.SECONDS);
    }

    public ShareRequest resolve()
    {
        this.finishRequest();
        return this;
    }

    public void decline()
    {
        ClientNetworkDispatcher.sendString(CommandFactory.makeObjectShareRequestDecline(this.originalSender));
        this.finishRequest();
    }

    public static void busy(UUID originalSender)
    {
        ClientNetworkDispatcher.sendString(CommandFactory.makeObjectShareRequestDeclineWithMessage(originalSender, "sharing.jmws.share_busy"));
    }

    public static void disabled(UUID originalSender) {
        ClientNetworkDispatcher.sendString(CommandFactory.makeObjectShareRequestDeclineWithMessage(originalSender, "sharing.jmws.disabled"));
    }

    public void accept()
    {
        ClientCommonClass.isBusy = true;
        ClientNetworkDispatcher.sendString(CommandFactory.makeObjectShareRequestAccept(this));
        JMWSPlugin.getInstance().addObjectFromRequest(this);

        this.finishRequest();
        ClientCommonClass.isBusy = false;
    }

    protected void timeout()
    {
        IncomingShareRequests.removeRequest(this.originalSender);
        PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.request_timeout_from", this.getSenderName()), true, false, MessageType.WARNING);
    }

    private void finishRequest()
    {
        this.timeout.cancel(true);
        IncomingShareRequests.removeRequest(this.originalSender);
    }

    public boolean isResolved()
    {
        return this.timeout.isCancelled() || this.timeout.isDone();
    }

    public String getSenderName()
    {
        return sender != null ? sender.getName() : CommonUtils.unknownUser;
    }

    public String getRecipientName()
    {
    return to != null ? to.getName() : CommonUtils.unknownUser;
    }
}

