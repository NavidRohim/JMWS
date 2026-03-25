package me.brynview.navidrohim.jmws.client.objects;

import commonnetwork.api.Dispatcher;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.api.PossessesIdentifier;
import me.brynview.navidrohim.jmws.common.api.Synchronizable;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.helper.CommandFactory;
import me.brynview.navidrohim.jmws.common.syncing.SyncUtils;
import me.brynview.navidrohim.jmws.common.syncing.Syncing;

import java.util.UUID;

public class ClientObject implements Synchronizable, PossessesIdentifier {

    private String name;
    private String customDataForJMWS;
    private String guid;
    private ObjectType objectType;

    private Syncing syncingHandler;

    private ClientObject(
            String name,
            String customDataForJMWS,
            String guid,
            ObjectType objectType
        )
    {
        this.name = name;
        this.customDataForJMWS = customDataForJMWS;
        this.guid = guid;
        this.objectType = objectType;
        this.syncingHandler = SyncUtils.getSyncingInfo(customDataForJMWS);
    }

    public static ClientObject fromWaypoint(Waypoint waypoint)
    {
        return new ClientObject(
                waypoint.getName(),
                waypoint.getCustomData(Constants.MODID),
                waypoint.getGuid(),
                ObjectType.WAYPOINT
        );
    }

    public static ClientObject fromGroup(WaypointGroup group)
    {
        return new ClientObject();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSyncedCustomData() {
        return customDataForJMWS;
    }

    @Override
    public String getGroupIdentifier() {
        return guid;
    }

    @Override
    public ObjectType getObjectType() {
        return objectType;
    }

    @Override
    public void stopSharingWith(UUID user)
    {

    }

    @Override
    public void stopSharingWithAll()
    {

    }

    @Override
    public void shareWith(UUID toUser)
    {
        Dispatcher.sendToServer(CommandFactory.makeShareRequestForServer(syncingHandler.getOwner(), toUser, syncingHandler.objectIdentifier, getObjectType()));
    }

    @Override
    public void makeGlobal()
    {

    }

    @Override
    public void removeGlobal()
    {

    }

    @Override
    public Syncing getSyncingHandler()
    {
        return syncingHandler;
    }
}
