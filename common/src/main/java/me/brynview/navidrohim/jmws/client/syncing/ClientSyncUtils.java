package me.brynview.navidrohim.jmws.client.syncing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class ClientSyncUtils
{
    public static @Nullable ClientSyncInformation syncInformationFromString(String info, @Nullable ClientSyncRegistry registryDefault)
    {
        try
        {
            ClientSyncInformation.REGISTRY = registryDefault;
            return ClientSyncInformation.SYNC_DECODER.fromJson(info, ClientSyncInformation.class);
        } catch (JsonSyntaxException e)
        {
            return null;
        }
    }

    public static @Nullable ClientSyncInformation syncInformationFromString(String info)
    {
        return syncInformationFromString(info, null);
    }

    public static @NotNull ClientSyncInformation getEmptySyncInformation(String identifier, boolean isGlobal, @Nullable ClientSyncRegistry registryDefault)
    {
        return new ClientSyncInformation(identifier, PlayerUtils.ourUUID(), new HashSet<>(), isGlobal, registryDefault);
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
