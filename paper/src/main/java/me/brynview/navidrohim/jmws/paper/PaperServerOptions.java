package me.brynview.navidrohim.jmws.paper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.brynview.navidrohim.jmws.common.platform.Services;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import me.brynview.navidrohim.jmws.server.config.ServerConfigObject;
import me.brynview.navidrohim.jmws.server.exceptions.ServerConfigurationException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class PaperServerOptions {
    private static final String JMWS_ENABLED = "jmwsEnabled";
    private static final String WAYPOINTS_ENABLED = "waypointsEnabled";
    private static final String GROUPS_ENABLED = "groupsEnabled";
    private static final String SHARING_ENABLED = "sharingEnabled";
    private static final String HANDSHAKE_DELAY = "handshakeDelay";
    private static final String SYNC_ALERTS_ENABLED = "syncAlertsEnabled";
    private static final String SYNC_ON_QUIT_ENABLED = "syncOnQuitEnabled";
    private static final String LEGACY_CONFIG_FILE = "jmws-server.json";

    private PaperServerOptions() {
    }

    static void reload() {
        Path configPath = Services.PLATFORM.getServerConfigPath();
        YamlConfiguration config = loadYamlConfig(configPath);
        ensureDefaults(config);
        applyRuntimeConfig(config);
        saveConfig(configPath, config);
    }

    static Path getConfigPath() {
        return Services.PLATFORM.getServerConfigPath();
    }

    static Path getLegacyConfigPath() {
        return Services.PLATFORM.getServerDataDirectory().resolve(LEGACY_CONFIG_FILE);
    }

    static boolean syncAlertsEnabled() {
        JsonObject config = getRuntimeConfig();
        return config.has(SYNC_ALERTS_ENABLED) && config.get(SYNC_ALERTS_ENABLED).getAsBoolean();
    }

    static boolean syncOnQuitEnabled() {
        JsonObject config = getRuntimeConfig();
        return !config.has(SYNC_ON_QUIT_ENABLED) || config.get(SYNC_ON_QUIT_ENABLED).getAsBoolean();
    }

    private static YamlConfiguration loadYamlConfig(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException | SecurityException error) {
            throw new ServerConfigurationException("Could not create JMWS plugin config directory: " + error.getMessage());
        }

        YamlConfiguration config = new YamlConfiguration();
        config.options().header("JMWS Paper adapter configuration. Restart or run /jmws reload after editing.");

        if (Files.exists(configPath)) {
            try {
                config.load(configPath.toFile());
                return config;
            } catch (IOException | InvalidConfigurationException error) {
                throw new ServerConfigurationException("Could not load JMWS Paper config.yml: " + error.getMessage());
            }
        }

        loadLegacyJsonConfig(config);
        return config;
    }

    private static void loadLegacyJsonConfig(YamlConfiguration config) {
        Path legacyPath = getLegacyConfigPath();
        if (!Files.exists(legacyPath)) {
            return;
        }

        try {
            JsonObject legacyConfig = JsonParser.parseString(Files.readString(legacyPath)).getAsJsonObject();
            copyBoolean(legacyConfig, config, JMWS_ENABLED);
            copyBoolean(legacyConfig, config, WAYPOINTS_ENABLED);
            copyBoolean(legacyConfig, config, GROUPS_ENABLED);
            copyBoolean(legacyConfig, config, SHARING_ENABLED);
            if (legacyConfig.has(HANDSHAKE_DELAY)) {
                config.set(HANDSHAKE_DELAY, legacyConfig.get(HANDSHAKE_DELAY).getAsInt());
            }
            copyBoolean(legacyConfig, config, SYNC_ALERTS_ENABLED);
            copyBoolean(legacyConfig, config, SYNC_ON_QUIT_ENABLED);

            JmwsPaperPlugin plugin = JmwsPaperPlugin.getInstance();
            if (plugin != null) {
                plugin.getLogger().info("Migrated JMWS Paper config values from " + legacyPath + " to " + getConfigPath() + ".");
            }
        } catch (IOException | IllegalStateException | NumberFormatException error) {
            throw new ServerConfigurationException("Could not migrate old JMWS Paper JSON config: " + error.getMessage());
        }
    }

    private static void ensureDefaults(YamlConfiguration config) {
        config.addDefault(JMWS_ENABLED, true);
        config.addDefault(WAYPOINTS_ENABLED, true);
        config.addDefault(GROUPS_ENABLED, true);
        config.addDefault(SHARING_ENABLED, true);
        config.addDefault(HANDSHAKE_DELAY, 250);
        config.addDefault(SYNC_ALERTS_ENABLED, false);
        config.addDefault(SYNC_ON_QUIT_ENABLED, true);
        config.options().copyDefaults(true);
    }

    private static void applyRuntimeConfig(YamlConfiguration config) {
        boolean jmwsEnabled = requireBoolean(config, JMWS_ENABLED);
        boolean waypointsEnabled = requireBoolean(config, WAYPOINTS_ENABLED);
        boolean groupsEnabled = requireBoolean(config, GROUPS_ENABLED);
        boolean sharingEnabled = requireBoolean(config, SHARING_ENABLED);
        int handshakeDelay = requirePositiveInt(config, HANDSHAKE_DELAY);
        boolean syncAlertsEnabled = requireBoolean(config, SYNC_ALERTS_ENABLED);
        boolean syncOnQuitEnabled = requireBoolean(config, SYNC_ON_QUIT_ENABLED);

        ServerConfigObject serverConfig = new ServerConfigObject(jmwsEnabled, waypointsEnabled, groupsEnabled, sharingEnabled, handshakeDelay);
        ServerConfig.serverConfig = serverConfig;

        JsonObject runtimeConfig = new JsonObject();
        runtimeConfig.addProperty(JMWS_ENABLED, serverConfig.jmwsEnabled);
        runtimeConfig.addProperty(WAYPOINTS_ENABLED, serverConfig.waypointsEnabled);
        runtimeConfig.addProperty(GROUPS_ENABLED, serverConfig.groupsEnabled);
        runtimeConfig.addProperty(SHARING_ENABLED, serverConfig.sharingEnabled);
        runtimeConfig.addProperty(HANDSHAKE_DELAY, serverConfig.handshakeDelay);
        runtimeConfig.addProperty(SYNC_ALERTS_ENABLED, syncAlertsEnabled);
        runtimeConfig.addProperty(SYNC_ON_QUIT_ENABLED, syncOnQuitEnabled);
        ServerConfig.rawServerConfigData = runtimeConfig.toString();
    }

    private static boolean requireBoolean(YamlConfiguration config, String path) {
        if (!config.isBoolean(path)) {
            throw new ServerConfigurationException("JMWS Paper config option '" + path + "' must be true or false.");
        }
        return config.getBoolean(path);
    }

    private static int requirePositiveInt(YamlConfiguration config, String path) {
        if (!config.isInt(path)) {
            throw new ServerConfigurationException("JMWS Paper config option '" + path + "' must be a whole number.");
        }
        int value = config.getInt(path);
        if (value <= 0) {
            throw new ServerConfigurationException("JMWS Paper config option '" + path + "' must be greater than 0.");
        }
        return value;
    }

    private static void saveConfig(Path configPath, YamlConfiguration config) {
        try {
            config.save(configPath.toFile());
        } catch (IOException error) {
            throw new ServerConfigurationException("Could not write JMWS Paper config.yml: " + error.getMessage());
        }
    }

    private static JsonObject getRuntimeConfig() {
        if (ServerConfig.rawServerConfigData == null || ServerConfig.rawServerConfigData.isBlank()) {
            reload();
        }
        return JsonParser.parseString(ServerConfig.rawServerConfigData).getAsJsonObject();
    }

    private static void copyBoolean(JsonObject from, YamlConfiguration to, String path) {
        if (from.has(path)) {
            to.set(path, from.get(path).getAsBoolean());
        }
    }
}
