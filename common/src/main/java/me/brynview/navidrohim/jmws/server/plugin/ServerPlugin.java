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
        Constants.getLogger().info("SERVER INIT JM");
        api = jmServerApi;
    }

    public static ServerPlugin getAPI()
    {
        return INSTANCE;
    }

    public static void migrateWaypoint(UUID playerUUID, String data)
    {
        Waypoint waypoint = WaypointFactory.fromWaypointJsonString(data);
        IServerAPI api = ServerPlugin.getAPI().api;

        if (api.getWaypoint(playerUUID, waypoint.getGuid()) == null)
        {
            api.addPlayerWaypoint(playerUUID, waypoint);
        }
    }

    public static void migrateGroup(UUID playerUUID, String data)
    {
        WaypointGroup group = WaypointFactory.fromGroupJsonString(data);
        IServerAPI api = ServerPlugin.getAPI().api;
        if (api.getGroup(playerUUID, group.getGuid()) == null)
        {
            ServerPlugin.getAPI().api.addPlayerGroup(playerUUID, group);
        }
    }
}
