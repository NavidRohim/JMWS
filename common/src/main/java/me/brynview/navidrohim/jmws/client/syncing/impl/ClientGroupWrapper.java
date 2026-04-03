package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientBaseObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.api.JMObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.ClientObject;

public class ClientGroupWrapper extends JMObjectWrapper<WaypointGroup> {

    private final WaypointGroup group;
    protected final ClientObject<ClientGroupWrapper> parent;

    public ClientGroupWrapper(WaypointGroup group, ClientObject<ClientGroupWrapper> parent) {
        super(group.getCustomData(Constants.MODID), group, parent);
        this.group = group;
        this.parent = parent;
    }

    @Override
    public void update()
    {
        this.group.setCustomData(Constants.MODID, this.getInfo().getSyncInformationAsString());
    }

    @Override
    public String getSerialization() {
        return group.toString();
    }

    @Override
    public void createRemotely(boolean silent) {
        ClientNetworkDispatcher.makeGroup(group, silent);
    }

    @Override
    public void updateRemotely()
    {
        ClientNetworkDispatcher.updateGroup(this.parent);
    }
}
