package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;

public class ClientGroupWrapper extends JMObjectWrapper {
    private final WaypointGroup group;

    public ClientGroupWrapper(WaypointGroup group) {
        super(group.getCustomData(Constants.MODID));
        this.group = group;
    }

    @Override
    public void update()
    {
        this.group.setCustomData(Constants.MODID, this.info.getSyncInformationAsString());
    }

    @Override
    public String getSerialization() {
        return group.toString();
    }
}
