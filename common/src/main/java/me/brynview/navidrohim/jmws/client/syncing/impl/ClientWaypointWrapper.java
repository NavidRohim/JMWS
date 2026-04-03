package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.api.JMObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.ClientObject;

public class ClientWaypointWrapper extends JMObjectWrapper<Waypoint> {

    private final Waypoint object;
    protected final ClientObject<ClientWaypointWrapper> parent;

    public ClientWaypointWrapper(Waypoint waypoint, ClientObject<ClientWaypointWrapper> parent) throws NullPointerException
    {
        super(waypoint.getCustomData(Constants.MODID), waypoint, parent);
        this.object = waypoint;
        this.parent = parent;
    }


    @Override
    public void update()
    {
        this.object.setCustomData(Constants.MODID, this.getInfo().getSyncInformationAsString());
    }

    @Override
    public String getSerialization() {
        return object.toString();
    }

    @Override
    public void createRemotely(boolean silent)
    {
        ClientNetworkDispatcher.makeWaypoint(object, silent);
    }

    @Override
    public void updateRemotely() {
        ClientNetworkDispatcher.updateWaypoint(this.parent);
    }
}
