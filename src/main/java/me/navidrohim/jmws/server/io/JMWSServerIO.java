package me.navidrohim.jmws.server.io;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.common.Constants;
import me.navidrohim.jmws.client.objects.SavedWaypoint;
import me.navidrohim.jmws.common.helper.CommonHelper;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


public class JMWSServerIO {
    public static boolean createWaypoint(String waypointFilename, JsonObject jsonObject, EntityPlayerMP player) {

        try {
            Path waypointPathObj = Paths.get(waypointFilename);

            Files.createFile(waypointPathObj);
            FileWriter waypointFileWriter = new FileWriter(waypointFilename);
            waypointFileWriter.write(jsonObject.toString());
            waypointFileWriter.close();

            return true;

        } catch (NoSuchFileException noSuchFileException) {
            CommonClass._createServerResources();
            Constants.getLogger().warn("`jmws` folder was not found so another was made. All server waypoints have been wiped. (waypoint error)");
            return createWaypoint(waypointFilename, jsonObject, player);

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
}

