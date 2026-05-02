package me.brynview.navidrohim.jmws.client.syncing.api;

import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class JMObjectWrapper <T> extends ClientBaseObjectWrapper<Object> {

    public JMObjectWrapper(String syncData, @Nullable String jsonRules, T object, String objectName, String objectGuid, String plugin) {
        super(syncData, jsonRules, object, objectName, objectGuid, plugin);
    }

    @Override
    public void setGlobal(boolean global)
    {
        super.setGlobal(global);
        this.update();
    }

    @Override
    public void addSharedTo(UUID sharedTo)
    {
        super.addSharedTo(sharedTo);
        this.update();
    }

    @Override
    public void removeSharedTo(UUID sharedTo)
    {
        super.removeSharedTo(sharedTo);
        this.update();
    }

    @Override
    public void clearSharedTo()
    {
        super.clearSharedTo();
        this.update();
    }

    @Override
    public void updateRemotely()
    {
        ClientNetworkDispatcher.updateObject(this);
    }

    public String getGuid()
    {
        throw new NotImplementedException("Use child class.");
    }

    public boolean isUsableOrNative()
    {
        return List.of(Context.NATIVE, Context.SYNCHRONISE).contains(this.getContext());
    }
}
