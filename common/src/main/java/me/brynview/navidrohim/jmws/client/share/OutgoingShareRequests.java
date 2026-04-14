package me.brynview.navidrohim.jmws.client.share;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class OutgoingShareRequests extends HashMap<UUID, OutgoingShareRequest> {

    public boolean hasShareRequestFor(UUID player)
    {
        return this.containsKey(player);
    }

    public void addRequest(UUID to, OutgoingShareRequest request)
    {
        this.put(to, request);
    }

    public void removeRequest(UUID from)
    {
        this.remove(from);
    }

    @Nullable
    public OutgoingShareRequest getRequest(UUID from)
    {
        return this.get(from);
    }

    public void clearAll() {
        for (OutgoingShareRequest request : this.values())
        {
            request.resolve();
        }
        this.clear();
    }

    public void sendRequest(UUID sharedTo, ClientBaseObjectWrapper<?> sharedObject)
    {
        this.addRequest(sharedTo, new OutgoingShareRequest(PlayerUtils.ourUUID(), sharedTo, sharedObject, ObjectType.valueOf(sharedObject.getType().getId()), sharedObject.getIdentifier(), sharedObject.getName()));
        ClientNetworkDispatcher.shareWith(sharedTo, sharedObject);
    }
}
