package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.SyncObjectType;
import me.brynview.navidrohim.jmws.client.syncing.api.JMObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;

public class ClientWaypointWrapper extends JMObjectWrapper<Waypoint> {

    private final Waypoint object;

    public ClientWaypointWrapper(Waypoint waypoint, String plugin) throws NullPointerException
    {
        this.object = waypoint;
        super(waypoint.getCustomData(Constants.MODID), waypoint, waypoint.getName(), waypoint.getGuid(), plugin);


        if (this.getContext() == Context.SYNCHRONISE || this.getContext() == Context.NATIVE)
        {
            waypoint.setPersistent(false);
        }
    }


    @Override
    public void update()
    {
        super.update();
        if (this.info != null) {
            this.object.setCustomData(Constants.MODID, this.getInfo().getSyncInformationAsString());
        }
    }

    @Override
    public boolean isInbuilt()
    {
        return false;
    }

    @Override
    public SyncObjectType getType()
    {
        return SyncObjectType.WAYPOINT;
    }

    @Override
    public String getGuid()
    {
        return object.getGuid();
    }

    @Override
    public Waypoint getNativeObject()
    {
        return object;
    }

    @Override
    public String getName() {
        return object.getName();
    }

    @Override
    public String getSerialization() {
        return object.toString();
    }

    @Override
    public int getColour() {
        return object.getColor();
    }

    @Override
    public void createLocally()
    {
        JMWSPlugin.getInstance().addWaypointFromWrapper(this);
    }

    @Override
    public void removeLocally()
    {
        JMWSPlugin.getInstance().removeWaypointFromWrapper(this);
    }
    @Override
    public void createRemotely(boolean silent)
    {
        super.createRemotely(silent);
        ClientNetworkDispatcher.makeWaypoint(object, silent);
    }

    @Override
    public void removeRemotely(boolean silent)
    {
        ClientNetworkDispatcher.deleteWaypoint(this.getIdentifier(), silent, false);
    }

    @Override
    public void updateRemotely() {
        if (getContext() == Context.SYNCHRONISE)
        {
            ClientNetworkDispatcher.updateWaypoint(this);
        } else if (getContext() == Context.NATIVE)
        {
            createRemotely(false);
        }
    }

}
