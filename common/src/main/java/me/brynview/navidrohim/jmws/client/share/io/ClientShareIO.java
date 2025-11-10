package me.brynview.navidrohim.jmws.client.share.io;

import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.share.io.CommonShareIO;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;

import java.util.List;
import java.util.stream.Stream;

public class ClientShareIO extends CommonShareIO {
    private ClientShareIO() {
        super(JMWSServerIO.getPathLocationPrefix(FetchType.SHARED) + "shared.json");
    }

    public static boolean isObjectInShareList(String identifier)
    {
        try (ClientShareIO file = new ClientShareIO())
        {
            return file.isInShared(identifier);
        }
    }

    public static void removeFromShareList(String identifier) {
        try (ClientShareIO file = new ClientShareIO())
        {
            file.removeFromShared(identifier);
        }
    }

    public static void addToShareList(String identifier)
    {
        try (ClientShareIO file = new ClientShareIO())
        {
            file.addToShared(identifier);
        }
    }

    public static List<String> getSharedIdentifiers()
    {
        try (ClientShareIO file = new ClientShareIO())
        {
            return file.data;
        }
    }
}
