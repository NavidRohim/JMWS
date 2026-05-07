package me.brynview.navidrohim.jmws.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CommonClass {
    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static final Gson gson = new Gson();
    public static final Gson gsonExcludeNoExpose = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    public static final Gson gsonExcludeNoExposeNotPretty = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static void createServerResources() {
        createServerDirectory(Services.PLATFORM.getWaypointDirectory());
        createServerDirectory(Services.PLATFORM.getGroupDirectory());
        createServerDirectory(Services.PLATFORM.getUserDirectory());
    }

    private static void createServerDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException | SecurityException error) {
            Constants.getLogger().error("Could not create JMWS server directory {}: {}", directory, error.getMessage());
        }
    }

    public static boolean isInternalServer() {
        return false;
    }

    public static void init() {
        Constants.getLogger().info("Creating server resources..");
        createServerResources();
    }
}
