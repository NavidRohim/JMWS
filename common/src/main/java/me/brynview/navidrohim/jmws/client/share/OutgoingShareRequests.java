package me.brynview.navidrohim.jmws.client.share;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.server.JMWSServerCommon;
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

    public boolean removeRequest(UUID from)
    {
        @Nullable OutgoingShareRequest valueRemoved = this.remove(from);
        if (valueRemoved == null)
        {
            Constants.getLogger().warn("No request found for player " + from + " to remove.");
            return false;
        } else {
            Constants.getLogger().info("Removed request for player " + from + "Thread state: " + valueRemoved.timeout.state());
            return true;
        }
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
        this.addRequest(sharedTo, new OutgoingShareRequest(PlayerUtils.ourUUID(), sharedTo, sharedObject, JMWSServerCommon.REGISTRY.getStrict(sharedObject.getType().getId()), sharedObject.getIdentifier(), sharedObject.getName()));
        ClientNetworkDispatcher.PeerToPeer.shareWith(sharedTo, sharedObject);
    }
}
