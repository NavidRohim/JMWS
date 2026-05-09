package me.brynview.navidrohim.jmws.server.plugin;

import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import journeymap.api.v2.server.IServerAPI;
import journeymap.api.v2.server.IServerPlugin;
import me.brynview.navidrohim.jmws.Constants;

import java.util.UUID;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class ServerPlugin implements IServerPlugin
{

    private IServerAPI api;
    private static ServerPlugin INSTANCE;

    public ServerPlugin()
    {
        INSTANCE = this;
    }

    @Override
    public String getModId()
    {
        return Constants.MODID;
    }

    @Override
    public void initialize(final IServerAPI jmServerApi)
    {
        api = jmServerApi;
    }

    public static ServerPlugin getPlugin()
    {
        return INSTANCE;
    }

    public static void migrateWaypoint(UUID playerUUID, String data)
    {
        try
        {
            Waypoint waypoint = WaypointFactory.fromWaypointJsonString(data);
            ServerPlugin.getPlugin().api.addPlayerWaypoint(playerUUID, waypoint);

        } catch (Exception e)
        {
            Constants.getLogger().error("Could not migrate waypoint. Error: " + e);
        }
    }

    public static void migrateGroup(UUID playerUUID, String data)
    {
        try
        {
            WaypointGroup group = WaypointFactory.fromGroupJsonString(data);
            ServerPlugin.getPlugin().api.addPlayerGroup(playerUUID, group);
        } catch (Exception e)
        {
            Constants.getLogger().error("Could not migrate group. Error: " + e);
        }
    }
}
