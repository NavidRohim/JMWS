package me.brynview.navidrohim.jmws.client.shared;

import java.util.HashMap;
import java.util.UUID;

public class IncomingShareRequests {
    private static final HashMap<UUID, ShareRequest> incomingShareRequestList = new HashMap<>();

    public static boolean hasShareRequestFrom(UUID player)
    {
        return incomingShareRequestList.containsKey(player);
    }

    public static void addIncomingRequest(UUID from, ShareRequest shareRequest)
    {
        incomingShareRequestList.put(from, shareRequest);
    }

    public static void removeIncomingRequest(UUID from)
    {
        incomingShareRequestList.remove(from);
    }

    public static ShareRequest getIncomingRequest(UUID from)
    {
        return incomingShareRequestList.get(from);
    }

    public static ShareRequest getFirstRequest()
    {
        return incomingShareRequestList.get(incomingShareRequestList.keySet().stream().toList().getFirst());
    }
}
