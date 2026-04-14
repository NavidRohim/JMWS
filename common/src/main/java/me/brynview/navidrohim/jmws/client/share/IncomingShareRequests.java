package me.brynview.navidrohim.jmws.client.share;

import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.client.share.request.ShareRequest;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class IncomingShareRequests extends HashMap<UUID, ShareRequest> {
    public final static ScheduledExecutorService requestScheduler =  Executors.newScheduledThreadPool(1);

    public boolean hasShareRequestFrom(UUID player)
    {
        return this.containsKey(player);
    }

    public void addRequest(UUID from, ShareRequest shareRequest)
    {
        this.put(from, shareRequest);
    }

    public void removeRequest(UUID from)
    {
        if (this.containsKey(from)) {
            ShareRequest shareRequest = this.remove(from);
            if (!shareRequest.isResolved()) {
                shareRequest.resolve();
            }
        }
    }

    public HashMap<String, ShareRequest> getAllUserKey()
    {
        HashMap<String, ShareRequest> r = new HashMap<>();
        for (Map.Entry<UUID, ShareRequest> s : this.entrySet())
        {
            r.put(PlayerUtils.getUsernameFromUUID(s.getKey()), s.getValue());
        }
        return r;
    }

    public void clearAll() {
        for (ShareRequest request : this.values())
        {
            request.resolve();
        }
        this.clear();
    }
}
