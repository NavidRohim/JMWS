package me.brynview.navidrohim.jmws.client.syncing;

import com.google.gson.*;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ClientSyncInformation extends SyncInformation
{
    private static final Gson SYNC_DECODER = new GsonBuilder().registerTypeAdapter(ClientSyncInformation.class, new ClientSyncInfoDeserializer()).create();

    public SyncRegistry syncRegistryType;

    public ClientSyncInformation(SyncInformation syncInformation, SyncRegistry syncRegistryType) {
        super(syncInformation.objectIdentifier, syncInformation.owner, syncInformation.sharedTo, syncInformation.isGlobal);
        this.syncRegistryType = syncRegistryType;
    }

    public static @Nullable ClientSyncInformation syncInformationFromString(String info)
    {
        try
        {
            return SYNC_DECODER.fromJson(info, ClientSyncInformation.class);
        } catch (JsonSyntaxException e)
        {
            return null;
        }
    }

    @Override
    public @NonNull String getSyncInformationAsString() {
        return SYNC_DECODER.toJson(this);
    }

    private static class ClientSyncInfoDeserializer implements JsonDeserializer<ClientSyncInformation>
    {
        @Override
        public @Nullable ClientSyncInformation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject rawJsonObject = json.getAsJsonObject();
            String rawJsonObjectString = json.getAsJsonObject().getAsString();
            SyncInformation info = SyncInformation.syncInformationFromString(rawJsonObjectString);

            String rawObjType = rawJsonObject.get("syncObjectType").getAsString();
            SyncRegistry syncRegistryType = SyncRegistry.of(rawObjType).orElse(SyncRegistry.UNKNOWN);

            if (syncRegistryType == SyncRegistry.UNKNOWN || info == null)
            {
                Constants.getLogger().error("Unknown sync object type: {} ({})", syncRegistryType, rawObjType);
                return null;
            }

            return new ClientSyncInformation(info, syncRegistryType);
        }
    }
}
