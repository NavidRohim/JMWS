package me.brynview.navidrohim.jmws.client.share;

import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

    public static void clear()
    {
        incomingShareRequestList.clear();
    }

    public static HashMap<UUID, ShareRequest> getAll()
    {
        return incomingShareRequestList;
    }

    public static HashMap<String, ShareRequest> getAllUserKey()
    {
        HashMap<String, ShareRequest> r = new HashMap<>();
        for (Map.Entry<UUID, ShareRequest> s : getAll().entrySet())
        {
            r.put(PlayerHelper.getUserFromUUID(s.getKey()).getDisplayName().getString(), s.getValue());
        }
        return r;
    }
}
