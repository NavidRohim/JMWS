package me.brynview.navidrohim.jmws.client.share.network;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;

import java.util.List;

public class Sharing {
    public static void requestWaypointFromIdentifier(List<String> identifiers)
    {
        Dispatcher.sendToServer(new JMWSActionPayload(CommandFactory.makeWaypointFetchRequestFromIdentifier(identifiers)));
    }
}
