package me.brynview.navidrohim.jmws.client.commands;

import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShareSuggestions {
    public static List<String> getIncomingShareRequestNames()
    {
        List<String> names = new ArrayList<>();
        for (UUID user : IncomingShareRequests.getAll().keySet())
        {
            String username = PlayerUtils.getUsernameFromUUID(user);
            if (!username.equals(CommonUtils.unknownUser))
            {
                names.add(username);
            }
        }

        return names;
    }
}
