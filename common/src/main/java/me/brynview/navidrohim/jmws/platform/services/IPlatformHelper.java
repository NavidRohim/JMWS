package me.brynview.navidrohim.jmws.platform.services;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
     * Threshold in ticks when the client syncs with the server. 800 ticks by default
     *
     * @return Integer that is specified in mod config.
     */
    int getSyncInTicks();

    /**
     * How many ticks the client has been counting for, if more than or equal to threshold, a sync is performed.
     *
     * @return Integer between 0 and configured threshold in config.
     */
    int timeUntilNextSyncInTicks();

    /**
     * If the server has JMWS installed.
     *
     * @return true if the server has JMWS, false if not.
     */
    boolean serverHasMod();

    void setServerModStatus(boolean serverModStatus);

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    void resetSyncThreshold();
}
