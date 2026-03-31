package me.brynview.navidrohim.jmws.client.objects;

import commonnetwork.api.Dispatcher;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.common.api.PossessesIdentifier;
import me.brynview.navidrohim.jmws.common.api.Synchronizable;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.utils.CommandFactory;

import java.util.UUID;

public class ClientObject <T extends ClientObjectWrapper> implements Synchronizable, PossessesIdentifier {

    private final String name;
    private final String customDataForJMWS;
    private final String guid;
    private final ObjectType objectType;

    private final T objectWrapper;

    public ClientObject(
            String name,
            String customDataForJMWS,
            String guid,
            ObjectType objectType,
            T ownerObjectWrapper
    )
    {
        this.name = name;
        this.customDataForJMWS = customDataForJMWS;
        this.guid = guid;
        this.objectType = objectType;

        this.objectWrapper = ownerObjectWrapper;
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
    public ObjectType getObjectType()
    {
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

    public ClientObjectWrapper getObjectWrapper()
    {
        return objectWrapper;
    }
}
