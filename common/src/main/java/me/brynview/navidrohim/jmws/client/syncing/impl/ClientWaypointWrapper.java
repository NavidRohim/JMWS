package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.plugin.ObjectIdentifierMap;
import me.brynview.navidrohim.jmws.client.syncing.api.JMObjectWrapper;

public class ClientWaypointWrapper extends JMObjectWrapper<Waypoint> {

    private final Waypoint object;
    public ClientWaypointWrapper(Waypoint waypoint, String plugin) throws NullPointerException
    {
        this.object = waypoint;
        super(waypoint.getCustomData(Constants.MODID), waypoint, waypoint.getName(), waypoint.getGuid(), plugin);


        if (this.getContext() == WrapperContext.SYNCHRONISE || this.getContext() == WrapperContext.NATIVE)
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
    public void removeRemotely(boolean silent)
    {
        ObjectIdentifierMap.removeObjectFromMap(this, silent, true);
    }

    @Override
    public void updateRemotely() {
        if (getContext() == WrapperContext.SYNCHRONISE)
        {
            ClientNetworkDispatcher.updateWaypoint(this);
        } else if (getContext() == WrapperContext.NATIVE)
        {
            createRemotely(false);
        }
    }

}
