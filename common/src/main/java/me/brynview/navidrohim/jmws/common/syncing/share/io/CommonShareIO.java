package me.brynview.navidrohim.jmws.common.syncing.share.io;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.server.io.JMWSServerIO;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CommonShareIO implements AutoCloseable {

    private static class SharedObjectUsers {

        public List<String> sharedWaypoints;
        public List<String> sharedGroups;

        public SharedObjectUsers(List<String> sharedWaypoints, List<String> sharedGroups)
        {
            this.sharedWaypoints = sharedWaypoints;
            this.sharedGroups = sharedGroups;
        }
    }

    protected final List<String> WpData = new ArrayList<>();
    protected final List<String> GpData = new ArrayList<>();

    public final Path objectPath;

    public CommonShareIO(Path sharedObjectFilePath) {
        this.objectPath = sharedObjectFilePath;

        try {
            JsonArray jsonElements = JMWSServerIO.getObjectDataFromDisk(sharedObjectFilePath, true).get("sharedWaypoints").getAsJsonArray();
            JsonArray jsonElementsGps = JMWSServerIO.getObjectDataFromDisk(sharedObjectFilePath, true).get("sharedGroups").getAsJsonArray();

            for (JsonElement elem : jsonElements)
            {
                WpData.add(elem.getAsString());
            }
            for (JsonElement elemGp : jsonElementsGps)
            {
                GpData.add(elemGp.getAsString());
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
        String permissionsJson = CommonClass.gson.toJson(new SharedObjectUsers(WpData, GpData));

        try (FileWriter permissionsListFileWriter = new FileWriter(this.objectPath.toFile()))
        {
            permissionsListFileWriter.write(permissionsJson);
        } catch (IOException reason)
        {
            Constants.getLogger().error("Failed to write permissions file. Reason: ", reason);
        }
    }

    public boolean addToShared(String sharedValue, ObjectType sharedObjectType)
    {
        if (!isInShared(sharedValue, sharedObjectType))
        {
            return sharedObjectType == ObjectType.WAYPOINT ? WpData.add(sharedValue) : GpData.add(sharedValue);
        }
        return false;
    }

    public boolean removeFromShared(String sharedValue, ObjectType sharedObjectType)
    {
        return sharedObjectType == ObjectType.WAYPOINT ? WpData.remove(sharedValue) : GpData.remove(sharedValue);
    }

    public boolean isInShared(String sharedValue, ObjectType sharedObjectType)
    {
        return sharedObjectType == ObjectType.WAYPOINT ? WpData.contains(sharedValue) : GpData.contains(sharedValue);
    }

    public List<String> getSharedList(ObjectType sharedObjectType)
    {
        return sharedObjectType == ObjectType.WAYPOINT ? WpData : GpData;
    }

    @Override
    public void close() {
        writeSharedList();
    }
}
