package me.brynview.navidrohim.jmws.common.share.io;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;
import me.brynview.navidrohim.jmws.server.io.ServerShareIO;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CommonShareIO implements AutoCloseable {
    public static class SharedObjectUsers {

        public List<String> valueList;

        public SharedObjectUsers(List<String> sharedValues)
        {
            this.valueList = sharedValues;
        }
    }

    private final List<String> data = new ArrayList<>();

    public final Path objectPath;

    public CommonShareIO(Path sharedObjectFilePath) {
        this.objectPath = sharedObjectFilePath;

        try {
            JsonArray jsonElements = JMWSServerIO.getObjectDataFromDisk(sharedObjectFilePath, true).get("sharedWith").getAsJsonArray();

            for (JsonElement elem : jsonElements)
            {
                data.add(elem.getAsString());
            }
        } catch (NullPointerException MissingFile)
        {
            writeSharedList();
        }
    }

    public CommonShareIO(String sharedObjectFilePath)
    {
        this(Path.of(sharedObjectFilePath));
    }

    protected void writeSharedList()
    {
        Gson gsonWriter = new Gson();
        String permissionsJson = gsonWriter.toJson(new ServerShareIO.SharedObjectUsers(data));

        try (FileWriter permissionsListFileWriter = new FileWriter(this.objectPath.toFile()))
        {
            permissionsListFileWriter.write(permissionsJson);
        } catch (IOException ignored)
        {

        }
    }

    public void addToShared(String sharedValue)
    {
        data.add(sharedValue);
    }

    public void removeFromShared(String sharedValue)
    {
        data.remove(sharedValue);
    }

    public boolean isInShared(String sharedValue)
    {
        return data.contains(sharedValue);
    }

    @Override
    public void close() {
        writeSharedList();
    }
}
