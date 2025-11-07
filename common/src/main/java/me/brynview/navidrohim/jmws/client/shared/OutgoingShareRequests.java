package me.brynview.navidrohim.jmws.client.shared;

import java.util.HashMap;
import java.util.UUID;

public class OutgoingShareRequests {
    private static final HashMap<UUID, OutgoingShareRequest> outgoingShareRequestList = new HashMap<>();

    public static void addOutgoingRequest(UUID to, OutgoingShareRequest request)
    {
        outgoingShareRequestList.put(to, request);
    }

    public static void removeOutgoingRequest(UUID from)
    {
        outgoingShareRequestList.remove(from);
    }

    public static int getSize()
    {
        return outgoingShareRequestList.size();
    }
}
