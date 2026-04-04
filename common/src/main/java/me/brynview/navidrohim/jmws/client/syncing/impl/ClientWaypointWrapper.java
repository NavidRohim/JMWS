package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.api.JMObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.ClientObject;

public class ClientWaypointWrapper extends JMObjectWrapper<Waypoint> {

    private final Waypoint object;


    public ClientWaypointWrapper(Waypoint waypoint, String plugin) throws NullPointerException
    {
        super(waypoint.getCustomData(Constants.MODID), waypoint, plugin);
        this.object = waypoint;

        if (this.getType() == WrapperType.SYNCHRONISE || this.getType() == WrapperType.NATIVE)
        {
            waypoint.setPersistent(false);
        }
    }


    @Override
    public void update()
    {
        this.object.setCustomData(Constants.MODID, this.getInfo().getSyncInformationAsString());
    }

    @Override
    public String getGuid()
    {
        return object.getGuid();
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
        if (getType() == ClientBaseObjectWrapper.WrapperType.SYNCHRONISE)
        {
            ClientNetworkDispatcher.updateWaypoint(this);
        } else if (getType() == ClientBaseObjectWrapper.WrapperType.NATIVE)
        {
            createRemotely(false);
        }
    }

}
