package me.brynview.navidrohim.jmws.common.platform.services;

public interface IPlatformHelper {

    enum Side
    {
        SERVER,
        CLIENT
    }

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
    Side side();

    /**
     * If the server has JMWS installed.
     *
     * @return true if the server has JMWS, false if not.
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
