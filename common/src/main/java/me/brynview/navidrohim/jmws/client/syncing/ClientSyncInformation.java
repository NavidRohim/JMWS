package me.brynview.navidrohim.jmws.client.syncing;

import com.google.gson.*;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;

public final class ClientSyncInformation extends SyncInformation
{
    static final Gson SYNC_DECODER = new GsonBuilder().registerTypeAdapter(ClientSyncInformation.class, new ClientSyncInfoSerializer()).create();
    static SyncRegistry REGISTRY;

    public SyncRegistry syncRegistryType;

    public ClientSyncInformation(SyncInformation syncInformation, SyncRegistry syncRegistryType) {
        super(syncInformation.objectIdentifier, syncInformation.owner, syncInformation.sharedTo, syncInformation.isGlobal);
        this.syncRegistryType = syncRegistryType;
    }

    public ClientSyncInformation(String objectIdentifier, UUID owner, Set<UUID> sharedTo, boolean isGlobal, SyncRegistry syncRegistryType) {
        super(objectIdentifier, owner, sharedTo, isGlobal);
        this.syncRegistryType = syncRegistryType;
    }

    /* Serializes to JSON */
    @Override
    public @NonNull String serialize() {
        return SYNC_DECODER.toJson(this);
    }

    @Override
    public String toString()
    {
        return serialize();
    }

    private static class ClientSyncInfoSerializer implements JsonDeserializer<ClientSyncInformation>, JsonSerializer<ClientSyncInformation>
    {
        @Nullable
        private static SyncInformation syncInformationFromString(String info)
        {
            try
            {
                return JMWSCommon.gson.fromJson(info, SyncInformation.class);
            } catch (JsonSyntaxException e)
            {
                return null;
            }
        }

        @Override
        public @Nullable ClientSyncInformation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject rawJsonObject = json.getAsJsonObject();
            String rawJsonObjectString = json.getAsJsonObject().toString();
            SyncInformation info = syncInformationFromString(rawJsonObjectString);

            @Nullable SyncRegistry syncRegistryType = REGISTRY;
            try
            {
                String rawObjType = rawJsonObject.get("syncRegistryType").getAsString();
                syncRegistryType = SyncRegistry.of(rawObjType).orElse(SyncRegistry.UNKNOWN);
            } catch (NullPointerException e) {
                Constants.getLogger().error("Missing syncRegistryType field in sync information: {} will use REGISTRY default. If none is given, null will be returned. Registry default: {} This is very likely a legacy object and cannot be updated.", rawJsonObjectString, REGISTRY);
            }

            if (syncRegistryType == SyncRegistry.UNKNOWN || info == null || syncRegistryType == null)
            {
                Constants.getLogger().error("Unknown sync object type: {}", syncRegistryType);
                return null;
            }

            return new ClientSyncInformation(info, syncRegistryType);
        }

        @Override
        public JsonElement serialize(ClientSyncInformation src, Type typeOfSrc, JsonSerializationContext context) {
            // Serialize everything normally, except for syncRegistryType
            // Only syncRegistryType needs custom serialization
            JsonObject jsonObject = JMWSCommon.gson.toJsonTree(src).getAsJsonObject();
            jsonObject.remove("syncRegistryType");
            jsonObject.add("syncRegistryType", new JsonPrimitive(src.syncRegistryType.getId()));

            return jsonObject;
        }
    }
}
