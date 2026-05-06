package me.brynview.navidrohim.jmws.client.share.request;

import com.mojang.authlib.GameProfile;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncRegistry;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.rules.registry.ClientShareRule;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.common.utils.CommonUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ShareRequest {

    public UUID originalSender;
    public UUID meantFor;

    public ClientObjectWrapper<?> currentSharedObject;
    public ClientSyncRegistry sharedObjectType;
    public List<ClientShareRule> rules = new ArrayList<>();

    public String requestIdentifier;
    public String objectDisplayName;

    @Nullable public GameProfile sender;
    @Nullable public GameProfile to;

    public final ScheduledFuture<?> timeout;

    public ShareRequest(@Nullable UUID uuid, @Nullable UUID meantForPlayerUUID, @Nullable ClientObjectWrapper<?> waypointOrGroup) {
        this.originalSender = uuid;
        this.meantFor = meantForPlayerUUID;
        this.currentSharedObject = waypointOrGroup;
        this.sharedObjectType = waypointOrGroup.getType();
        this.requestIdentifier = waypointOrGroup.getIdentifier();
        this.objectDisplayName = waypointOrGroup.getName();

        PlayerUtils.getUserFromUUID(uuid).ifPresentOrElse(p -> {this.sender = p;}, () -> {this.sender = null;});
        PlayerUtils.getUserFromUUID(meantForPlayerUUID).ifPresentOrElse(pFor -> {this.to = pFor;}, () -> {this.sender = null;});

        Constants.LoggerHolder.debug("creating timeout future", "TIMEOUT FUTURE CREATION");
        this.timeout = IncomingShareRequests.requestScheduler.schedule(this::timeout, 20, TimeUnit.SECONDS);
    }

    public ShareRequest resolve()
    {
        this.finishRequest();
        return this;
    }

    public void decline()
    {
        ClientNetworkDispatcher.PeerToPeer.declineShare(this.originalSender);
        //ClientNetworkDispatcher.sendString(CommandFactory.makeObjectShareRequestDecline(this.originalSender));
        this.finishRequest();
    }

    public static void busy(UUID originalSender)
    {
        ClientNetworkDispatcher.PeerToPeer.declineShare(originalSender, "sharing.jmws.share_busy");
        //ClientNetworkDispatcher.sendString(CommandFactory.makeObjectShareRequestDeclineWithMessage(originalSender, "sharing.jmws.share_busy"));
    }

    public static void disabled(UUID originalSender) {
        ClientNetworkDispatcher.PeerToPeer.declineShare(originalSender, "sharing.jmws.disabled");
        //ClientNetworkDispatcher.sendString(CommandFactory.makeObjectShareRequestDeclineWithMessage(originalSender, "sharing.jmws.disabled"));
    }

    public void accept()
    {
        JMWSClientCommon.isBusy = true;
        ClientNetworkDispatcher.PeerToPeer.acceptShare(this);
        JMWSPlugin.getInstance().addObjectFromRequest(this);

        this.finishRequest();
        JMWSClientCommon.isBusy = false;
    }

    protected void timeout()
    {
        JMWSClientCommon.incomingShareRequests.removeRequest(this.originalSender);
        PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.request_timeout_from", this.getSenderName()), true, false, MessageType.WARNING);
    }

    private void finishRequest()
    {
        Constants.LoggerHolder.debug("Finished share request for " + this.originalSender, "SHARE FINISH");
        this.timeout.cancel(true);
        JMWSClientCommon.incomingShareRequests.removeRequest(this.originalSender);
    }

    public boolean isResolved()
    {
        return this.timeout.isCancelled() || this.timeout.isDone();
    }

    public String getSenderName()
    {
        return sender != null ? sender.name() : CommonUtils.unknownUser;
    }

    public String getRecipientName()
    {
    return to != null ? to.name() : CommonUtils.unknownUser;
    }
}

