package me.brynview.navidrohim.jmws.client.share;

import me.brynview.navidrohim.jmws.client.enums.JMWSMessageType;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class OutgoingShareRequest extends ShareRequest {

    public static final boolean isOutgoing = true; // kinda useless

    public OutgoingShareRequest(@Nullable UUID uuid, @Nullable UUID meantForPlayerUUID, Object waypointOrGroup, ObjectType sharedObjectType, String requestIdentifier, String objectDisplayName) {
        super(uuid, meantForPlayerUUID, waypointOrGroup, sharedObjectType, requestIdentifier, objectDisplayName);
    }

    public OutgoingShareRequest resolve()
    {
        this.finishRequest();
        return this;
    }

    @Override
    protected void timeout()
    {
        OutgoingShareRequests.removeRequest(this.originalSender);
        PlayerHelper.sendUserAlert(Component.translatable("sharing.jmws.request_timeout"), true, false, JMWSMessageType.WARNING);
    }

    private void finishRequest()
    {
        IncomingShareRequests.removeRequest(this.originalSender);
        this.timeout.cancel(true);
    }
}
