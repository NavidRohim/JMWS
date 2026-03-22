package me.brynview.navidrohim.jmws.client.share.request;

import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.share.OutgoingShareRequests;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class OutgoingShareRequest extends ShareRequest {

    public static final boolean isOutgoing = true; // kinda useless

    public OutgoingShareRequest(@Nullable UUID uuid, @Nullable UUID meantForPlayerUUID, Object waypointOrGroup, ObjectType sharedObjectType, String requestIdentifier, String objectDisplayName) {
        super(uuid, meantForPlayerUUID, waypointOrGroup, sharedObjectType, requestIdentifier, objectDisplayName);
    }

    @Override
    public OutgoingShareRequest resolve()
    {
        this.finishRequest();
        return this;
    }

    @Override
    protected void timeout()
    {
        OutgoingShareRequests.removeRequest(this.meantFor);
        PlayerHelper.sendUserAlert(Component.translatable("sharing.jmws.request_timeout_to", this.getRecipientName()), true, false, MessageType.WARNING);
    }

    private void finishRequest()
    {
        OutgoingShareRequests.removeRequest(this.originalSender);
        this.timeout.cancel(true);
    }
}
