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

public class ClientObject <T extends ClientObjectWrapper> implements Synchronizable, PossessesIdentifier {

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

    public final void setWrapper(T wrapper)
    {
        if (this.objectWrapper != null) {
            throw new IllegalStateException("Cannot set object wrapper after object has been set");
        }
        this.objectWrapper = wrapper;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSyncedCustomData() {
        return customDataForJMWS;
    }

    public final void setSyncedCustomData()
    {
        customDataForJMWS = ObjectIdentifierMap.makeWaypointHash(PlayerUtils.ourUUID(), guid, name);
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

    @NotNull
    public ClientObjectWrapper getObjectWrapper()
    {
        if (this.objectWrapper == null) {
            throw new RuntimeException("objectWrapper is null. Usually means a bat implementation.");
        }
        return objectWrapper;
    }

    public final boolean isValid()
    {
        return objectWrapper.isValid();
    }

    public final boolean isLegacy(boolean transitionIfLegacy)
    {
        boolean isLegacy = objectWrapper.isValid();
        if (isLegacy && transitionIfLegacy) {
            //LegacyUtils.transitionObject(); // TODO: TRANSITION FOR GENERICS
        }
        return isLegacy;
    }

    public final boolean isUsable(boolean transitionIfLegacy)
    {
        boolean isValid = objectWrapper.isValid();
        boolean isLegacy = objectWrapper.isLegacy();
        boolean isUsable = isValid && !isLegacy;

        if (isLegacy && transitionIfLegacy) {
            //LegacyUtils.transitionObject();
            return false;
        }

        if (!isUsable) {
            Constants.getLogger().warn("ClientObject<{}> is not valid. isValid={}, isLegacy={}, transitionIfLegacy={}. This warning is a sign of a bad implementation.", objectWrapper.getClass().getName(), isValid, isLegacy, transitionIfLegacy);
        }
        return isUsable;
    }

    public void createRemotely(boolean silent)
    {
        this.objectWrapper.createRemotely(silent);
    }
}
