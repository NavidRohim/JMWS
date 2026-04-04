package me.brynview.navidrohim.jmws.client.syncing.api;

import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.UUID;

public abstract class JMObjectWrapper <T> extends ClientBaseObjectWrapper<Object> {

    public JMObjectWrapper(String syncData, T object, String objectName, String objectGuid, String plugin) {
        super(syncData, object, objectName, objectGuid, plugin);
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

    public String getGuid()
    {
        throw new NotImplementedException("Use child class.");
    }

    public void update() {
        throw new NotImplementedException("Use child class.");
    }

    public boolean isUsableOrNative()
    {
        return List.of(WrapperContext.NATIVE, WrapperContext.SYNCHRONISE).contains(this.getContext());
    }
}
