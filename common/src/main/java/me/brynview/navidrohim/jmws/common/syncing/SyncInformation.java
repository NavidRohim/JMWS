package me.brynview.navidrohim.jmws.common.syncing;

import com.google.gson.JsonSyntaxException;
import me.brynview.navidrohim.jmws.common.CommonClass;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SyncInformation {

    public String objectIdentifier;
    public UUID owner;
    public List<UUID> sharedTo;
    public boolean isGlobal;

    @Nullable
    public static SyncInformation syncInformationFromString(String info)
    {
        try
        {
            return CommonClass.gson.fromJson(info, SyncInformation.class);
        } catch (JsonSyntaxException e)
        {
            return null;
        }
    }

    @Nullable
    public String getSyncInformationAsString()
    {
        return CommonClass.gson.toJson(this);
    }
}
