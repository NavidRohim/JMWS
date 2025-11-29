package me.brynview.navidrohim.jmws.client.share;

import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class OutgoingShareRequests {
    private static final HashMap<UUID, OutgoingShareRequest> outgoingShareRequestList = new HashMap<>();
    public static boolean hasShareRequestFor(UUID player)
    {
        return outgoingShareRequestList.containsKey(player);
    }

    public static void addRequest(UUID to, OutgoingShareRequest request)
    {
        outgoingShareRequestList.put(to, request);
    }

    public static void removeRequest(UUID from)
    {
        outgoingShareRequestList.remove(from);
    }

    @Nullable
    public static OutgoingShareRequest getRequest(UUID from)
    {
        return outgoingShareRequestList.get(from);
    }

    public static void clearAll() {
        for (OutgoingShareRequest request : outgoingShareRequestList.values())
        {
            request.resolve();
        }
    }
}
