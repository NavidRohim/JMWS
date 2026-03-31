package me.brynview.navidrohim.jmws.common.syncing;

import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.server.syncing.ServerSyncingHandler;

import java.util.List;
import java.util.UUID;

public class SyncInformation {
    public String objectIdentifier;
    public UUID owner;
    public List<String> sharedTo;
    public boolean global;

    public static SyncInformation SyncInformationFromString(String info)
    {
        return CommonClass.gson.fromJson(info, SyncInformation.class);
    }

    public String getSyncInformationAsString()
    {
        return CommonClass.gson.toJson(this);
    }
}
