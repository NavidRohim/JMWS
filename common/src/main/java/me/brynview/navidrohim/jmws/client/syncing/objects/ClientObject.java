package me.brynview.navidrohim.jmws.client.syncing.objects;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.plugin.ObjectIdentifierMap;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.api.PossessesIdentifier;
import me.brynview.navidrohim.jmws.common.api.Synchronizable;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
/*
public class ClientObject <T extends ClientObjectWrapper>{

    private final String name;
    private String customDataForJMWS;
    private final String guid;
    private final ObjectType objectType;

    private T objectWrapper = null;

    public ClientObject(
            String name,
            String customDataForJMWS,
            String guid,
            ObjectType objectType
    ) {
        this.name = name;
        this.customDataForJMWS = customDataForJMWS;
        this.guid = guid;
        this.objectType = objectType;
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
        //ClientNetworkDispatcher.shareWith(toUser, this);
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

    public final boolean isLegacy(boolean transitionIfLegacy)
    {
        boolean isLegacy = objectWrapper.isValid();
        if (isLegacy && transitionIfLegacy) {
            //LegacyUtils.transitionObject(); // TODO: TRANSITION FOR GENERICS
        }
        return isLegacy;
    }


    public void createRemotely(boolean silent)
    {
        this.objectWrapper.createRemotely(silent);
    }

}*/
public class ClientObject <T extends ClientObjectWrapper>{}
