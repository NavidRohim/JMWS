package me.brynview.navidrohim.jmws.client.syncing.api;

import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.UUID;

public class JMObjectWrapper extends ClientBaseObjectWrapper {

    public JMObjectWrapper(String syncData) {
        super(syncData);
    }

    @Override
    public String getIdentifier() {
        return this.getInfo().objectIdentifier;
    }

    @Override
    public List<String> getSharedTo() {
        return this.getInfo().sharedTo;
    }

    @Override
    public UUID getOwner()
    {
        return this.getInfo().owner;
    }

    @Override
    public boolean getGlobal()
    {
        return this.getInfo().global;
    }

    @Override
    public void setGlobal(boolean global)
    {
        this.getInfo().global = global;
        this.update();
    }

    @Override
    public void addSharedTo(String sharedTo)
    {
        this.getInfo().sharedTo.add(sharedTo);
        this.update();
    }

    @Override
    public void removeSharedTo(String sharedTo)
    {
        this.getInfo().sharedTo.remove(sharedTo);
        this.update();
    }

    @Override
    public void clearSharedTo()
    {
        this.getInfo().sharedTo.clear();
        this.update();
    }

    @Override
    public void update() {
        throw new NotImplementedException("Use child class.");
    }

    @Override
    public String getSerialization() {
        return "{}";
    }
}
