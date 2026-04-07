package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.SyncObjectType;
import me.brynview.navidrohim.jmws.client.syncing.api.JMObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;

public class ClientGroupWrapper extends JMObjectWrapper<WaypointGroup> {

    private final WaypointGroup group;

    public ClientGroupWrapper(WaypointGroup group, String plugin) {
        this.group = group;
        super(group.getCustomData(Constants.MODID), group, group.getName(), group.getGuid(), plugin);
    }

    @Override
    public boolean isInbuilt()
    {
        return Constants.forbiddenGroups.contains(group.getGuid());
    }

    @Override
    public SyncObjectType getType()
    {
        return SyncObjectType.GROUP;
    }

    @Override
    public void update()
    {
        super.update();
        if (this.info != null)
        {
            Constants.LoggerHolder.debug("GUID %s".formatted(this.group.getGuid()), "GUID CHECK");
            Constants.LoggerHolder.debug("CONTEXT %s".formatted(this.getContext()), "CONTEXT CHECK");
            this.group.setCustomData(Constants.MODID, this.getInfo().getSyncInformationAsString());
        }
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
    public WaypointGroup getNativeObject()
    {
        return group;
    }

    @Override
    public void createRemotely(boolean silent)
    {
        super.createRemotely(silent);
        ClientNetworkDispatcher.makeGroup(group, silent);
    }

    @Override
    public void removeRemotely(boolean silent)
    {
        if (getContext() == Context.SYNCHRONISE)
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
        } else if (getContext() == Context.INBUILT)
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

    @Override
    public void createLocally() {
        JMWSPlugin.getInstance().addGroupFromWrapper(this);
    }

    @Override
    public void removeLocally() {
        JMWSPlugin.getInstance().removeGroupFromWrapper(this);
    }
}
