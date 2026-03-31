package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;

import java.util.List;
import java.util.UUID;

public class ClientWaypointWrapper implements ClientObjectWrapper {

    private final SyncInformation info;
    private final Waypoint waypoint;

    public ClientWaypointWrapper(Waypoint waypoint)
    {
        this.waypoint = waypoint;
        this.info = SyncInformation.SyncInformationFromString(waypoint.getCustomData(Constants.MODID));
    }

    @Override
    public String getIdentifier() {
        return this.info.objectIdentifier;
    }

    @Override
    public List<String> getSharedTo() {
        return this.info.sharedTo;
    }

    @Override
    public UUID getOwner() {
        return this.info.owner;
    }

    @Override
    public boolean getGlobal() {
        return this.info.global;
    }

    @Override
    public void setGlobal(boolean global)
    {
        this.info.global = global;
        this.update();
    }

    @Override
    public void addSharedTo(String sharedTo)
    {
        this.info.sharedTo.add(sharedTo);
        this.update();
    }

    @Override
    public void removeSharedTo(String sharedTo)
    {
        this.info.sharedTo.remove(sharedTo);
        this.update();
    }

    @Override
    public void clearSharedTo()
    {
        this.info.sharedTo.clear();
        this.update();
    }

    @Override
    public void update()
    {
        this.waypoint.setCustomData(Constants.MODID, this.info.getSyncInformationAsString());
    }
}
