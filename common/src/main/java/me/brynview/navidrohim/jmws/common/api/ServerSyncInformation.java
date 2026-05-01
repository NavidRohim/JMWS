package me.brynview.navidrohim.jmws.common.api;

import com.google.gson.*;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.server.JMWSServerCommon;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistryEntry;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;

import static me.brynview.navidrohim.jmws.client.syncing.ClientSyncUtils.syncInformationFromString;

public class ServerSyncInformation extends SyncInformation
{
    public static final Gson SYNC_DECODER = new GsonBuilder().registerTypeAdapter(ServerSyncInformation.class, new ServerSyncInfoSerializer(false)).create();
    public static final Gson SYNC_DECODER_WITH_INS = new GsonBuilder().registerTypeAdapter(ServerSyncInformation.class, new ServerSyncInfoSerializer(true)).create();

    static ServerSyncRegistryEntry<?> DEFAULT_REGISTRY;

    public transient @Nullable ServerObject object = null;
    public ServerSyncRegistryEntry<?> syncRegistryType;

    public ServerSyncInformation(String identifier, UUID owner, Set<UUID> sharedTo, boolean isGlobal, ServerSyncRegistryEntry<?> syncRegistryType) {
        super(identifier, owner, sharedTo, isGlobal);
        this.syncRegistryType = syncRegistryType;
    }

    public ServerSyncInformation(String identifier, UUID owner, Set<UUID> sharedTo, boolean isGlobal, ServerSyncRegistryEntry<?> syncRegistryType, @Nullable ServerObject object) {
        super(identifier, owner, sharedTo, isGlobal);
        this.syncRegistryType = syncRegistryType;
        this.object = object;
    }

    public void addUserToShare(UUID userUUID)
    {
        this.sharedTo.add(userUUID);
    }

    public void removeUserFromShare(UUID userUUID)
    {
        this.sharedTo.remove(userUUID);
    }

    public void removeAllFromShare()
    {
        this.sharedTo.clear();
    }

    public boolean isGlobal()
    {
        return this.isGlobal;
    }

    public void setGlobal(boolean global)
    {
        this.isGlobal = global;
    }

    public UUID getOwner()
    {
        return this.owner;
    }

    public void setRegistry(@Nullable ServerSyncRegistryEntry<?> registry)
    {
        this.syncRegistryType = registry;
    }

    public static ServerSyncInformation getFromString(String data, boolean withObjectInstance)
    {
        if (withObjectInstance)
        {
            return ServerSyncInformation.SYNC_DECODER_WITH_INS.fromJson(data, ServerSyncInformation.class);
        }
        return ServerSyncInformation.SYNC_DECODER.fromJson(data, ServerSyncInformation.class);
    }

    @Override
    public @NonNull String serialize() {
        return SYNC_DECODER.toJson(this);
    }

    @Override
    public String toString() {
        return "<%s, %s, %s, %s, %s, %s>".formatted(this.getClass().getName(), this.objectIdentifier, this.owner, this.sharedTo, this.isGlobal, this.syncRegistryType);
    }

    private record ServerSyncInfoSerializer(boolean provideObjectInstance) implements JsonDeserializer<ServerSyncInformation>, JsonSerializer<ServerSyncInformation>
    {
        @Override
        public ServerSyncInformation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            System.out.println(json.toString());
            JsonObject rawJsonObject = json.getAsJsonObject();
            String rawJsonObjectString = json.getAsJsonObject().toString();
            SyncInformation info = syncInformationFromString(rawJsonObjectString);

            @Nullable ServerSyncRegistryEntry<?> syncRegistryType = ServerSyncInformation.DEFAULT_REGISTRY;
            try {
                String rawObjType = rawJsonObject.get("syncRegistryType").getAsString();
                syncRegistryType = JMWSServerCommon.REGISTRY.getOptional(rawObjType).orElse(ServerSyncRegistry.GENERIC);
            } catch (NullPointerException e) {
                Constants.getLogger().error("Missing syncRegistryType field in sync information: {} will use REGISTRY default. If none is given, null will be returned. Registry default: {} This is very likely a legacy object and cannot be updated.", rawJsonObjectString, JMWSServerCommon.REGISTRY);
            }

            if (syncRegistryType == ServerSyncRegistry.GENERIC || info == null || syncRegistryType == null) {
                Constants.getLogger().error("Unknown sync object type: {}", syncRegistryType);
                return null;
            }

            if (!this.provideObjectInstance)
            {
                return new ServerSyncInformation(info.objectIdentifier, info.owner, info.sharedTo, info.isGlobal, syncRegistryType);
            }

            @Nullable ServerObject serverObject = JMWSServerIO.getObjectFromDisk(info.objectIdentifier, info.owner, syncRegistryType, false, info.isGlobal);
            return new ServerSyncInformation(info.objectIdentifier, info.owner, info.sharedTo, info.isGlobal, syncRegistryType, serverObject);
            }

            @Override
            public JsonElement serialize(ServerSyncInformation src, Type typeOfSrc, JsonSerializationContext context) {
                // Serialize using SyncInformation's type so Gson only touches the base fields,
                // avoiding the Class<?> field in ServerSyncRegistryEntry entirely.
                JsonObject jsonObject = context.serialize(src, SyncInformation.class).getAsJsonObject();
                jsonObject.addProperty("syncRegistryType", src.syncRegistryType.getId());
                return jsonObject;
            }
        }
}
