package me.brynview.navidrohim.jmws;

import net.minecraft.client.multiplayer.ClientLevel;

import static me.brynview.navidrohim.jmws.plugin.JMWSPlugin.updateWaypoints;

public class SyncCounter {

    private ClientLevel oldWorld = null;
    public static int tickCounterUpdateThreshold = 800;
    public static int tickCounter = 0;

    public int getCurrentTickCount()
    {
        return tickCounter;
    }

    public int getTickCounterUpdateThreshold()
    {
        return tickCounterUpdateThreshold;
    }

    public void resetSyncThreshold()
    {
        tickCounterUpdateThreshold = CommonClass.config.updateWaypointFrequency.get();
    }

    public void resetSyncCounter() { tickCounter = 0; }

    public void iterateCounter()
    {
        ClientLevel world = CommonClass.minecraftClientInstance.level;

        if (world != null && CommonClass.getEnabledStatus()) {
            if (world != oldWorld) {
                if (oldWorld == null) {
                    tickCounterUpdateThreshold = 20 * (CommonClass.config.serverHandshakeTimeout.get() + 1); // Add 1 second buffer to not interrupt message
                } else {
                    tickCounterUpdateThreshold = 40; // 2-second delay when switching dimension
                }
                tickCounter = 0;
            } else {

                tickCounter++;
                if (tickCounter >= tickCounterUpdateThreshold) {

                    updateWaypoints(true);
                    resetSyncThreshold();
                    tickCounter = 0;
                }
            }
            oldWorld = CommonClass.minecraftClientInstance.level;
        } else {

            tickCounter = 0;
            oldWorld = null;
        }
    }
}
