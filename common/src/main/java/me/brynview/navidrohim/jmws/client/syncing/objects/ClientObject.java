package me.brynview.navidrohim.jmws.client.syncing.objects;

import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.common.api.PossessesIdentifier;
import me.brynview.navidrohim.jmws.common.api.Synchronizable;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;

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
        ClientNetworkDispatcher.removeShareWith(user, this);
    }

    @Override
    public void stopSharingWithAll()
    {
        ClientNetworkDispatcher.removeShareFromAll(this);
    }

    @Override
    public void shareWith(UUID toUser)
    {
        ClientNetworkDispatcher.shareWith(toUser, this);
    }

    @Override
    public void makeGlobal()
    {
        this.objectWrapper.setGlobal(true);
        ClientNetworkDispatcher.makeGlobal(this, true);
    }

    @Override
    public void removeGlobal()
    {
        this.objectWrapper.setGlobal(false);
        ClientNetworkDispatcher.makeGlobal(this, false);
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
