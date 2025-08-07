package me.navidrohim.jmws.server.io;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.client.objects.SavedWaypoint;
import me.navidrohim.jmws.helper.CommonHelper;
import org.jetbrains.annotations.Nullable;
import scala.actors.threadpool.Arrays;

import javax.vecmath.Vector3d;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static me.navidrohim.jmws.helper.CommonHelper._getWaypointFromRaw;


public class JMWSServerIO {

    public static boolean createWaypoint(JsonObject jsonObject, UUID playerUUID) {
        JsonObject pos = jsonObject.getAsJsonObject().getAsJsonObject("pos");
        String waypointFilePath = _getWaypointFromRaw(new Vector3d(
                pos.get("x").getAsInt(),
                pos.get("y").getAsInt(),
                pos.get("z").getAsInt()
                ),
                jsonObject.get("name").getAsString(),
                playerUUID

        );
        try {

            Path waypointPathObj = Paths.get(waypointFilePath);

            Files.createFile(waypointPathObj);
            FileWriter waypointFileWriter = new FileWriter(waypointFilePath);
            waypointFileWriter.write(jsonObject.toString());
            waypointFileWriter.close();

            return true;

        } catch (NoSuchFileException noSuchFileException) {
            CommonClass._createServerResources();
            Constants.getLogger().warn("`jmws` folder was not found so another was made. All server waypoints and groups have been wiped. (waypoint error)");
            return createWaypoint(jsonObject, playerUUID);

        } catch (FileSystemException missingPerms) {
            Constants.getLogger().error("JMWS is missing write permissions to \"jmws\" folder. (waypoint error)");
            return false;

        } catch (IOException genericIOError) {
            Constants.getLogger().error("Got exception trying to make waypoint -> " + genericIOError);
            return false;
        }
    }

    public static boolean deleteAllUserObjects(UUID playerUUID) {
        List<Boolean> deletionStatusList = new ArrayList<>();

        for (String waypointPath : getFileObjects(playerUUID)) {
            deletionStatusList.add(CommonHelper.deleteFile(waypointPath));
        }

        return deletionStatusList.isEmpty() || deletionStatusList.stream().allMatch(deletionStatusList.get(0)::equals);

    }

    public static List<String> getFileObjects(UUID uuid) {

        List<String> waypointFileList = new ArrayList<>();
        String pathSearch;
        pathSearch = "./jmws";

        try (Stream<Path> files = Files.list(Paths.get(pathSearch))) {
            files.filter(Files::isRegularFile).forEach(path -> {
                if (path.toString().contains(uuid.toString())) {
                    waypointFileList.add(path.toString());
                }
            });
        } catch (IOException err) {
            return Collections.emptyList();
            }
        return waypointFileList;
    }



    @Nullable
    public static JsonObject getObjectDataFromDisk(String objPath) {
        try {
            return new JsonParser().parse(new String(Files.readAllBytes(Paths.get("sample.txt")))).getAsJsonObject();
        } catch (IOException ioException) {
            Constants.getLogger().error("Error retrieving saved object data -> " + ioException);
        }
        return null;
    }

    @Nullable
    private static SavedWaypoint getWaypointFromFile(String waypointPath, UUID playerUUID) {
        JsonObject waypointLocalData = getObjectDataFromDisk(waypointPath);
        if (waypointLocalData != null) {
            return new SavedWaypoint(waypointLocalData, playerUUID);
        }
        return null;
    }
}

