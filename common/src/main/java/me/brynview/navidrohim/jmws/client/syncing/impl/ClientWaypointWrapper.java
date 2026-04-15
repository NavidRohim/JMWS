package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.SyncObjectType;
import me.brynview.navidrohim.jmws.client.syncing.api.JMObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;

public class ClientWaypointWrapper extends JMObjectWrapper<Waypoint> {

    public ClientWaypointWrapper(Waypoint waypoint, String plugin) throws NullPointerException
    {
        super(waypoint.getCustomData(Constants.MODID), waypoint, waypoint.getName(), waypoint.getGuid(), plugin);


        if (this.getContext() == Context.SYNCHRONISE || this.getContext() == Context.NATIVE)
        {
            getNativeObject().setPersistent(false);
        }
    }


    @Override
    public void update()
    {
        super.update();
        if (this.info != null) {
            getNativeObject().setCustomData(Constants.MODID, this.getInfo().getSyncInformationAsString());
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
        return getNativeObject().getGuid();
    }

    @Override
    public Waypoint getNativeObject()
    {
        return (Waypoint) super.getNativeObject();
    }

    @Override
    public String getName() {
        return getNativeObject().getName();
    }

    @Override
    public int getColour() {
        return getNativeObject().getColor();
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
        ClientNetworkDispatcher.makeWaypoint(getNativeObject(), silent);
    }

    @Override
    public void removeRemotely(boolean silent)
    {
        ClientNetworkDispatcher.deleteWaypoint(this.getIdentifier(), silent, false);
    }
}
