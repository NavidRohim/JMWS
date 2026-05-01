package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.server.syncing.registry.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.syncing.registry.ServerSyncRegistryEntry;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class UserSharingFile implements AutoCloseable {

    protected final Map<String, List<String>> data = new HashMap<>();
    private final Path objectPath;

    private List<String> getList(ServerSyncRegistryEntry<?> entry)
    {
        return data.computeIfAbsent(entry.getDisplayName(), k -> new ArrayList<>());
    }

    private boolean isInShared(String sharedValue, ServerSyncRegistryEntry<?> sharedServerSyncRegistry)
    {
        return getList(sharedServerSyncRegistry).contains(sharedValue);
    }

    private void writeSharedList()
    {
        String permissionsJson = JMWSCommon.gson.toJson(data);

        try (FileWriter permissionsListFileWriter = new FileWriter(this.objectPath.toFile()))
        {
            permissionsListFileWriter.write(permissionsJson);

        } catch (FileNotFoundException noShareFile)
        {
            JMWSCommon.createServerResources();
            writeSharedList();
        }
        catch (IOException reason)
        {
            Constants.getLogger().error("Failed to write permissions file. Reason: ", reason);
        }
    }

    public UserSharingFile(UUID userUUID) {
        this.objectPath = JMWSServerIO.PathUtils.getObjectFilename(userUUID, "SHARED", ServerSyncRegistry.SHARED, false);
        try {
            JsonObject json = JMWSServerIO.getObjectDataFromDisk(objectPath, true);
            if (json != null)
            {
                for (Map.Entry<String, JsonElement> entry : json.entrySet())
                {
                    List<String> list = new ArrayList<>();
                    for (JsonElement elem : entry.getValue().getAsJsonArray())
                    {
                        list.add(elem.getAsString());
                    }
                    data.put(entry.getKey(), list);
                }
            }
        } catch (NullPointerException MissingFile)
        {
            writeSharedList();
        }
    }

    public void addToShared(String sharedValue, ServerSyncRegistryEntry<?> sharedServerSyncRegistry)
    {
        if (!isInShared(sharedValue, sharedServerSyncRegistry))
        {
            getList(sharedServerSyncRegistry).add(sharedValue);
            writeSharedList();
        }
    }

    public void removeFromShared(String sharedValue, ServerSyncRegistryEntry<?> sharedServerSyncRegistry)
    {
        getList(sharedServerSyncRegistry).remove(sharedValue);
        writeSharedList();
    }

    public static void removeObjectFromUser(UUID playerUUID, String objectIdentifier, ServerSyncRegistryEntry<?> sharedServerSyncRegistry)
    {
        try (UserSharingFile usf = new UserSharingFile(playerUUID))
        {
            usf.removeFromShared(objectIdentifier, sharedServerSyncRegistry);
        }
    }

    public List<String> getSharedList(ServerSyncRegistryEntry<?> sharedObjectType)
    {
        return getList(sharedObjectType);
    }

    @Override
    public void close() {
        writeSharedList();
    }
}
