package me.navidrohim.jmws.client;


import me.navidrohim.jmws.client.mixinhelper.MixinManager;
import me.navidrohim.jmws.common.CommonClass;
import net.minecraft.client.multiplayer.WorldClient;

import static me.navidrohim.jmws.client.plugin.JMWSPlugin.updateWaypoints;

public class SyncCounter {

    private WorldClient oldWorld = null;
    private static int tickCounterUpdateThreshold = 800;
    private static int tickCounter = 0;

    /**
     * What tick auto-tick is on. 1 second = 20 ticks.
     * @return int -- The tick.
     */
    public int getCurrentTickCount()
    {
        return tickCounter;
    }

    /**
     * Returns at which the threshold auto-sync will sync in ticks. 1 second = 20 ticks.
     * @return int -- How many ticks.
     */
    public int getTickCounterUpdateThreshold()
    {
        return tickCounterUpdateThreshold;
    }

    /**
     * Resets the auto-sync's threshold back to configs default.
     */
    public void resetSyncThreshold()
    {
        tickCounterUpdateThreshold = CommonClass.config.getUpdateWaypointFrequencyAsTicks();
    }

    /**
     * Put's the auto-sync tick counter back to 0.
     */
    public void resetSyncCounter() { tickCounter = 0; }

    /**
     * With each call of this function, the tick counter will increase by 1.
     * Granting player is in a world and has auto-sync enabled.
     */
    public void iterateCounter()
    {
        // Get clients current world
        WorldClient world = CommonClass.minecraftClientInstance.world;

        // Check if player is in a world, if the player is in a valid server and auto-sync is enabled
        if (world != null && CommonClass.getEnabledStatus() && CommonClass.config.autoSync) {
            // Check if the world is still equal to the world of the last counter tick. If not, we have changed dimension.
            if (world != oldWorld) {
                if (oldWorld != null) { // This can be false if this is the first tick being in a new server.
                    tickCounterUpdateThreshold = 40; // 2-second delay when switching dimension
                }
                // Set counter to 0 here, if we reach this state this means we have either switched dimension or have just joined a server.
                tickCounter = 0;
            } else {

                tickCounter++;
                if (tickCounter >= tickCounterUpdateThreshold) { // Check if we have reached auto-sync threshold

                    updateWaypoints(true); // sync
                    resetSyncThreshold();
                    tickCounter = 0;
                }
            }
            oldWorld = CommonClass.minecraftClientInstance.world;
        } else {

            tickCounter = 0;
            oldWorld = null;
        }
    }
}
