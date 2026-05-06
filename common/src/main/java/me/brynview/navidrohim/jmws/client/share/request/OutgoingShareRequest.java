package me.brynview.navidrohim.jmws.client.share.request;

import com.mojang.authlib.GameProfile;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.server.syncing.registry.ServerSyncRegistryEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class OutgoingShareRequest extends ShareRequest {

    public OutgoingShareRequest(@Nullable UUID uuid, @Nullable UUID meantForPlayerUUID, ClientObjectWrapper<?> sharedObject) {
        super(uuid, meantForPlayerUUID, sharedObject);
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
        Constants.LoggerHolder.debug(JMWSClientCommon.outgoingShareRequests, "OUTGOING SHARE REQUEST");
        boolean didRemove = JMWSClientCommon.outgoingShareRequests.removeRequest(this.meantFor);
        if (didRemove)
        {
            PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.request_timeout_to", this.getRecipientName()), true, false, MessageType.WARNING);
        }
    }

    private void finishRequest()
    {
        Constants.LoggerHolder.debug(JMWSClientCommon.outgoingShareRequests, "OUTGOING SHARE REQUEST");
        JMWSClientCommon.outgoingShareRequests.removeRequest(this.meantFor);
        Constants.LoggerHolder.debug(JMWSClientCommon.outgoingShareRequests, "OUTGOING SHARE REQUEST");
        this.timeout.cancel(true);
        Constants.LoggerHolder.debug(this.timeout.state(), "SR TIMEOUT STATE");
    }

    public static void sendShareRequest(ClientObjectWrapper<?> shareableObject, GameProfile user)
    {
        if (!JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(user.id()))
        {
            if (!shareableObject.getSharedTo().contains(user.id()))
            {
                if (!user.equals(JMWSCommon.minecraftClientInstance.player.getGameProfile()))
                {
                    shareableObject.sendShareRequest(user.id());
                } else {
                    PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.cannot_share"), true, true, MessageType.WARNING);
                }
            } else {
                PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.already_sharing", shareableObject.getName(), user.name()), true, true, MessageType.WARNING);
            }
        } else {
            PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.share_busy", user.name()), true, true, MessageType.PENDING);
        }
    }
}
