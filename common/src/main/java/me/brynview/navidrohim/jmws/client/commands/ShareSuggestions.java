package me.brynview.navidrohim.jmws.client.commands;

import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShareSuggestions {
    public static List<String> getIncomingShareRequestNames()
    {
        List<String> names = new ArrayList<>();
        for (UUID user : IncomingShareRequests.getAll().keySet())
        {
            String username = PlayerHelper.getUsernameFromUUID(user);
            if (!username.equals(CommonHelper.unknownUser))
            {
                names.add(username);
            }
        }

        return names;
    }
}
