package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.syncing.api.JMObjectWrapper;

public class ClientGroupWrapper extends JMObjectWrapper<WaypointGroup> {

    private final WaypointGroup group;

    public ClientGroupWrapper(WaypointGroup group, String plugin) {
        this.group = group;

        super(group.getCustomData(Constants.MODID), group, group.getName(), group.getGuid(), plugin);

        if (Constants.forbiddenGroups.contains(group.getGuid())) {
            this.info = null;
            this.setContext(WrapperContext.INBUILT);
        }

    }

    @Override
    public void update()
    {
        super.update();
        this.group.setCustomData(Constants.MODID, this.getInfo().getSyncInformationAsString());
    }

    @Override
    public String getSerialization() {
        return group.toString();
    }

    @Override
    public String getGuid()
    {
        return group.getGuid();
    }

    @Override
    public void createRemotely(boolean silent)
    {
        ClientNetworkDispatcher.makeGroup(group, silent);
    }

    @Override
    public void removeRemotely(boolean silent)
    {
        if (getContext() == WrapperContext.SYNCHRONISE)
        {
            ClientNetworkDispatcher.deleteGroup(
                    getIdentifier(),
                    getGuid(),
                    false,
                    false,
                    true,
                    getGlobal(),
                    false
            );
        } else if (getContext() == WrapperContext.INBUILT)
        {
            ClientNetworkDispatcher.deleteGroup(
                    "null",
                    getGuid(),
                    false,
                    true,
                    false,
                    true,
                    false
            );
        }
    }

    public void removeRemotely(boolean silent, boolean deleteAllWaypoints, boolean removeGroupItself)
    {
        ClientNetworkDispatcher.deleteGroup(
                getIdentifier(),
                getGuid(),
                silent,
                deleteAllWaypoints,
                removeGroupItself,
                getGlobal(),
                false
        );
    }

    @Override
    public void updateRemotely()
    {
        ClientNetworkDispatcher.updateGroup(this);
    }
}
