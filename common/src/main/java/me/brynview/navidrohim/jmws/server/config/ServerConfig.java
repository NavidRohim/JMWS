package me.brynview.navidrohim.jmws.server.config;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.helper.CommonHelper;
import me.brynview.navidrohim.jmws.server.exceptions.ServerConfigurationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerConfig {

    private static final Path configPath = Path.of("./config/jmws-server.json");

    public static String rawServerConfigData;
    public static ServerConfigObject serverConfig;

    public static void ensureExistence()
    {
        try
        {
            Files.createDirectories(configPath.getParent());

            File configFileObj = new File(configPath.toString());
            boolean didCreateNew = configFileObj.createNewFile();

            if (didCreateNew)
            {
                String configJsonString = CommonClass.gsonExcludeNoExpose.toJson(new ServerConfigObject(
                                true,
                                true,
                                true,
                                true,
                                250
                        )
                );

                FileWriter configFileWritableObj = new FileWriter(configPath.toFile());
                configFileWritableObj.write(configJsonString);
                configFileWritableObj.close();
                ensureExistence();

            } else {
                rawServerConfigData = getConfigJson();
                serverConfig = CommonClass.gson.fromJson(rawServerConfigData, ServerConfigObject.class);

                if (!serverConfig.isValid())
                {
                    deleteConfig();
                    ensureExistence();
                    Constants.getLogger().error("JMWS config was corrupted or from an older version. Created new config, so you may have to set your old config values.");
                }
            }

        } catch (SecurityException securityException) {
            throw new ServerConfigurationException("Could not create configuration file! There are no write permissions.");
        } catch (IOException ioException) {
            Constants.getLogger().error("JMWS Server got error when creating configuration file: {}", String.valueOf(ioException));
        }
    }

    public static void deleteConfig()
    {
        CommonHelper.deleteFile(configPath);
    }

    public static String getConfigJson()
    {
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
