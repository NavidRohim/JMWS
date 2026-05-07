package me.brynview.navidrohim.jmws.common.platform.services;

import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;

import java.nio.file.Path;
import java.util.UUID;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * If current instance is CLIENT or SERVER side.
     *
     * @return CLIENT if client, SERVER if server.
     */
    String side();

    /**
     * Sends an action command payload to an online client-side JMWS mod.
     *
     * @param payload The payload to send.
     * @param playerUuid The target player UUID.
     */
    void sendActionPayloadToClient(JMWSActionPayload payload, UUID playerUuid);

    /**
     * Sends a JMWS handshake payload to an online client-side JMWS mod.
     *
     * @param payload The payload to send.
     * @param playerUuid The target player UUID.
     */
    void sendHandshakePayloadToClient(JMWSHandshakePayload payload, UUID playerUuid);

    /**
     * Checks whether a player UUID is currently considered a server operator.
     *
     * @param playerUuid The player UUID to check.
     * @return true if the player is an operator.
     */
    boolean isOperator(UUID playerUuid);

    /**
     * Base directory for server-side JMWS data.
     *
     * @return The data directory path.
     */
    default Path getServerDataDirectory() {
        return Path.of(".", "jmws");
    }

    /**
     * Server-side JMWS config path.
     *
     * @return The config file path.
     */
    default Path getServerConfigPath() {
        return Path.of(".", "config", "jmws-server.json");
    }

    /**
     * Directory where synced waypoint JSON files are stored.
     *
     * @return The waypoint data directory.
     */
    default Path getWaypointDirectory() {
        return getServerDataDirectory();
    }

    /**
     * Directory where synced group JSON files are stored.
     *
     * @return The group data directory.
     */
    default Path getGroupDirectory() {
        return getServerDataDirectory().resolve("groups");
    }

    /**
     * Directory where user sharing JSON files are stored.
     *
     * @return The user sharing data directory.
     */
    default Path getUserDirectory() {
        return getServerDataDirectory().resolve("users");
    }

    /**
     * Whether the server should echo a client-requested sync alert back to the client.
     *
     * @param requestedByClient true if the client requested a visible sync alert.
     * @param isDeathSync true if the sync was triggered for a death waypoint.
     * @return true if the sync response should ask the client to display an alert.
     */
    default boolean shouldSendSyncAlert(boolean requestedByClient, boolean isDeathSync) {
        return requestedByClient;
    }

    /**
     * The current runtime environment name.
     *
     * @return "development" or "production".
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
