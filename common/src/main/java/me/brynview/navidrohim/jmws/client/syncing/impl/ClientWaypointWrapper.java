package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;

import java.util.List;
import java.util.UUID;

public class ClientWaypointWrapper extends JMObjectWrapper {

    private final Waypoint waypoint;

    public ClientWaypointWrapper(Waypoint waypoint)
    {
        super(waypoint.getCustomData(Constants.MODID));
        this.waypoint = waypoint;

    }

    @Override
    public void update()
    {
        this.waypoint.setCustomData(Constants.MODID, this.info.getSyncInformationAsString());
    }

    @Override
    public String getSerialization() {
        return waypoint.toString();
    }
}
