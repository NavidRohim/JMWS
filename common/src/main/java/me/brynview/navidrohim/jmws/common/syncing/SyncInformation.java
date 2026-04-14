package me.brynview.navidrohim.jmws.common.syncing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class SyncInformation {

    public String objectIdentifier;
    public UUID owner;
    public Set<UUID> sharedTo;
    public boolean isGlobal;

    public boolean isOwner(UUID user)
    {
        return owner.equals(user);
    }

    @Nullable
    public String getSyncInformationAsString()
    {
        return JMWSCommon.gson.toJson(this);
    }

    @Nullable
    public static SyncInformation syncInformationFromString(String info)
    {
        try
        {
            return JMWSCommon.gson.fromJson(info, SyncInformation.class);
        } catch (JsonSyntaxException e)
        {
            return null;
        }
    }

    @Nullable
    public static String getOnlyIdentifier(@Nullable String syncInformation)
    {
        if (syncInformation != null)
        {
            JsonObject obj = JsonParser.parseString(syncInformation).getAsJsonObject();
            return obj.get("objectIdentifier").getAsString();
        }
        return null;
    }
}

