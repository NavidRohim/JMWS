package me.brynview.navidrohim.jmws.client.share;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class IncomingShareRequests {
    private static final HashMap<UUID, ShareRequest> incomingShareRequestList = new HashMap<>();

    public static boolean hasShareRequestFrom(UUID player)
    {
        return incomingShareRequestList.containsKey(player);
    }
    public final static ScheduledExecutorService requestScheduler =  Executors.newScheduledThreadPool(1);

    public static void addRequest(UUID from, ShareRequest shareRequest)
    {
        incomingShareRequestList.put(from, shareRequest);
    }

    public static void removeRequest(UUID from)
    {
        incomingShareRequestList.remove(from);
    }

    @Nullable
    public static ShareRequest getRequest(UUID from)
    {
        return incomingShareRequestList.get(from);
    }

    @Nullable
    public static ShareRequest getFirstRequest()
    {
        try
        {
            return incomingShareRequestList.get(incomingShareRequestList.keySet().stream().toList().getFirst());
        } catch (NoSuchElementException ignored)
        {
            return null;
        }
    }
}
