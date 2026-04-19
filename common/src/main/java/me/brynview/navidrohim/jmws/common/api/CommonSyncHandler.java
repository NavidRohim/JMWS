package me.brynview.navidrohim.jmws.common.api;

import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CommonSyncHandler
{
    // TODO: Extend from SyncInformation? And no need for expose I don't believe.
    @Expose
    public String objectIdentifier;

    @Expose
    public Set<UUID> sharedTo;

    @Expose
    protected UUID owner;

    @Expose
    protected boolean isGlobal;

    public CommonSyncHandler(Set<UUID> sharedTo, String identifier, UUID owner, boolean isGlobal) {
        this.objectIdentifier = identifier;
        this.sharedTo = sharedTo;
        this.owner = owner;
        this.isGlobal = isGlobal;
    }

    public void addUserToShare(UUID userUUID)
    {
        this.sharedTo.add(userUUID);
    }

    public void removeUserFromShare(UUID userUUID)
    {
        this.sharedTo.remove(userUUID);
    }

    public void removeAllFromShare()
    {
        this.sharedTo.clear();
    }

    public boolean isGlobal()
    {
        return this.isGlobal;
    }

    public void setGlobal(boolean global)
    {
        this.isGlobal = global;
    }

    public boolean isOwner(UUID supposedOwner)
    {
        return this.owner.equals(supposedOwner);
    }

    public UUID getOwner()
    {
        return this.owner;
    }
}
