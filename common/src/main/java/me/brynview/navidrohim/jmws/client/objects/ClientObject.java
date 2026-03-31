package me.brynview.navidrohim.jmws.client.objects;

import commonnetwork.api.Dispatcher;
import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncingHandler;
import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.common.api.CommonSyncHandler;
import me.brynview.navidrohim.jmws.common.api.PossessesIdentifier;
import me.brynview.navidrohim.jmws.common.api.Synchronizable;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;

import java.util.UUID;

public class ClientObject implements Synchronizable, PossessesIdentifier {

    private String name;
    private String customDataForJMWS;
    private String guid;
    private ObjectType objectType;

    private ClientSyncingHandler clientSyncingHandler;
    private ClientObjectWrapper objectWrapper;

    private ClientObject(
            String name,
            String customDataForJMWS,
            String guid,
            ObjectType objectType,
            ClientObjectWrapper ownerObjectWrapper
        )
    {
        this.name = name;
        this.customDataForJMWS = customDataForJMWS;
        this.guid = guid;
        this.objectType = objectType;

        this.objectWrapper = ownerObjectWrapper;
        this.clientSyncingHandler = new ClientSyncingHandler(ownerObjectWrapper);
    }

    public static ClientObject fromWaypoint(Waypoint waypoint)
    {
        return new ClientObject(
                waypoint.getName(),
                waypoint.getCustomData(Constants.MODID),
                waypoint.getGuid(),
                ObjectType.WAYPOINT,
                new ClientWaypointWrapper(waypoint)
        );
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
        Dispatcher.sendToServer(CommandFactory.makeShareRequestForServer(objectWrapper.getOwner(), toUser, objectWrapper.getIdentifier(), getObjectType()));
    }

    @Override
    public void makeGlobal()
    {
        this.objectWrapper.setGlobal(true);
    }

    @Override
    public void removeGlobal()
    {
        this.objectWrapper.setGlobal(false);
    }

    @Override
    public boolean isGlobal()
    {
        return this.objectWrapper.getGlobal();
    }

    @Override
    public CommonSyncHandler getSyncingHandler()
    {
        return clientSyncingHandler;
    }


}
