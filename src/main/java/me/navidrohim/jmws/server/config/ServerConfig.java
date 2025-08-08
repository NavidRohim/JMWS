package me.navidrohim.jmws.server.config;

import com.google.gson.Gson;
import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.exceptions.ServerConfigurationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConfig {

    private static final String configPath = "./config/jmws-server.json";

    public static void ensureExistence()
    {
        try
        {
            File configFileObj = new File(configPath);
            boolean didCreateNew = configFileObj.createNewFile();

            if (didCreateNew)
            {
                Gson configJson = new Gson();
                String configJsonString = configJson.toJson(new ServerConfigObject(
                                true,
                                true
                        )
                );

                FileWriter configFileWritableObj = new FileWriter(configPath);
                configFileWritableObj.write(configJsonString);
                configFileWritableObj.close();
            }

        } catch (SecurityException securityException) {
            throw new ServerConfigurationException("Could not create configuration file! There are no write permissions.");
        } catch (IOException ioException) {
            Constants.getLogger().error("JMWS Server got error when creating configuration file; {}", String.valueOf(ioException));
        }
    }

    public static String getConfigJson()
    {
        ensureExistence();
        String content;
        try {
            Path path = Paths.get(configPath);
            content = Files.readAllLines(path).get(0);
        } catch (SecurityException securityException) {
            throw new ServerConfigurationException("Could not read server config file! Please make sure there are read permissions for the config.");
        } catch (IOException ioException) {
            throw new ServerConfigurationException("Server config file is corrupted! Please delete the file and restart.");
        }

        return content;
    }

    public static ServerConfigObject getConfig()
    {
        Gson configJsonObj = new Gson();
        return configJsonObj.fromJson(getConfigJson(), ServerConfigObject.class);
    }

    public static ServerConfigObject getConfig(String data)
    {
        Gson configJsonObj = new Gson();
        return configJsonObj.fromJson(data, ServerConfigObject.class);
    }
}
