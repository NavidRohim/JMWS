package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.SyncObjectType;
import me.brynview.navidrohim.jmws.client.syncing.api.JMObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;
import net.minecraft.util.ARGB;

public class ClientGroupWrapper extends JMObjectWrapper<WaypointGroup> {

    public ClientGroupWrapper(WaypointGroup group, String plugin) {
        super(group.getCustomData(Constants.MODID), group, group.getName(), group.getGuid(), plugin);
    }

    @Override
    public boolean isInbuilt()
    {
        return Constants.forbiddenGroups.contains(getNativeObject().getGuid());
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
            Constants.LoggerHolder.debug("GUID %s".formatted(getNativeObject().getGuid()), "GUID CHECK");
            Constants.LoggerHolder.debug("CONTEXT %s".formatted(this.getContext()), "CONTEXT CHECK");
            getNativeObject().setCustomData(Constants.MODID, this.getInfo().getSyncInformationAsString());
        }
    }

    @Override
    public String getName()
    {
        return getNativeObject().getName();
    }

    @Override
    public int getColour()
    {
        return getNativeObject().getColor();
    }

    @Override
    public String getGuid()
    {
        return getNativeObject().getGuid();
    }

    @Override
    public WaypointGroup getNativeObject()
    {
        return (WaypointGroup) super.getNativeObject();
    }

    @Override
    public void createRemotely(boolean silent)
    {
        super.createRemotely(silent);
        ClientNetworkDispatcher.makeGroup(getNativeObject(), silent);
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
    public void createLocally() {
        JMWSPlugin.getInstance().addGroupFromWrapper(this);
    }

    @Override
    public void removeLocally() {
        JMWSPlugin.getInstance().removeGroupFromWrapper(this);
    }
}
