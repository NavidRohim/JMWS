package me.brynview.navidrohim.jmws.client.syncing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.common.utils.SyncUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ClientSyncUtils
{
    public static @Nullable ClientSyncInformation syncInformationFromString(String info, @Nullable SyncRegistry registryDefault)
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

    public static @NotNull ClientSyncInformation getEmptySyncInformation(String identifier, boolean isGlobal, @Nullable SyncRegistry registryDefault)
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
