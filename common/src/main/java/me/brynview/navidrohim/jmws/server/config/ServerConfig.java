package me.brynview.navidrohim.jmws.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.server.exceptions.ServerConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConfig {

    private static final Path configPath = Path.of("./config/jmws-server.json");

    public static final String rawServerConfigData = getConfigJson();
    public static final ServerConfigObject serverConfig = new Gson().fromJson(rawServerConfigData, ServerConfigObject.class);

    public static void ensureExistence()
    {
        try
        {
            Files.createDirectories(configPath.getParent());

            File configFileObj = new File(configPath.toString());
            boolean didCreateNew = configFileObj.createNewFile();

            if (didCreateNew)
            {
                Gson configJson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String configJsonString = configJson.toJson(new ServerConfigObject(
                                true,
                                true,
                                true,
                                true
                        )
                );

                FileWriter configFileWritableObj = new FileWriter(configPath.toFile());
                configFileWritableObj.write(configJsonString);
                configFileWritableObj.close();

            }

        } catch (SecurityException securityException) {
            throw new ServerConfigurationException("Could not create configuration file! There are no write permissions.");
        } catch (IOException ioException) {
            Constants.getLogger().error("JMWS Server got error when creating configuration file; {}", String.valueOf(ioException));
        }
    }

    public static boolean deleteConfig()
    {
        return CommonHelper.deleteFile(configPath);
    }

    public static String getConfigJson()
    {
        ensureExistence();
        String content;
        try {
            content = Files.readString(configPath, StandardCharsets.UTF_8);
        } catch (SecurityException securityException) {
            throw new ServerConfigurationException("Could not read server config file! Please make sure there are read permissions for the config.");
        } catch (IOException ioException) {
            throw new ServerConfigurationException("Server config file is corrupted! Please delete the file and restart.");
        }

        return content;
    }

    public static ServerConfigObject getConfig()
    {
        return serverConfig;
    }
}
