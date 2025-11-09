package me.brynview.navidrohim.jmws.server.io;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShareIO implements AutoCloseable
{

    public static class SharedObjectUsers {

        public List<UUID> userList;

        public SharedObjectUsers(List<UUID> userUUIDs)
        {
            this.userList = userUUIDs;
        }
    }

    private final List<UUID> data = new ArrayList<>();

    public final Path objectPath;

    public ShareIO(Path sharedObjectFilePath) {
        this.objectPath = sharedObjectFilePath;

        try {
            JsonArray jsonElements = JMWSServerIO.getObjectDataFromDisk(sharedObjectFilePath, true).get("sharedWith").getAsJsonArray();

            for (JsonElement elem : jsonElements)
            {
                data.add(UUID.fromString(elem.getAsString()));
            }
        } catch (NullPointerException MissingFile)
        {
            writeSharedList();
        }
    }

    private void writeSharedList()
    {
        Gson gsonWriter = new Gson();
        String permissionsJson = gsonWriter.toJson(new SharedObjectUsers(data));

        try (FileWriter permissionsListFileWriter = new FileWriter(this.objectPath.toFile()))
        {
            permissionsListFileWriter.write(permissionsJson);
        } catch (IOException ignored)
        {

        }
    }

    public void addUserToSharedObject(UUID user)
    {
        data.add(user);
    }

    public void removeUserFromSharedObject(UUID user)
    {
        data.remove(user);
    }

    public boolean userIsInSharedObject(UUID user)
    {
        return data.contains(user);
    }

    @Override
    public void close() throws Exception
    {
        writeSharedList();
    }
}
