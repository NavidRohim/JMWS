package me.brynview.navidrohim.jmws.common.api;

import com.google.gson.annotations.Expose;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class ServerSyncInformationImpl extends SyncInformation
{
    // TODO: Extend from SyncInformation? And no need for expose I don't believe.

    public ObjectType syncRegistryType;

    public ServerSyncInformationImpl(String identifier, UUID owner, Set<UUID> sharedTo, boolean isGlobal, ObjectType syncRegistryType) {
        super(identifier, owner, sharedTo, isGlobal);
        this.syncRegistryType = syncRegistryType;
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

    public UUID getOwner()
    {
        return this.owner;
    }

    public void setRegistry(@Nullable ObjectType registry)
    {
        this.syncRegistryType = registry;
    }

    public static ServerSyncInformationImpl getFromString(String data)
    {
        return JMWSCommon.gson.fromJson(data, ServerSyncInformationImpl.class);
    }

    @Override
    public String toString() {
        return "<%s, %s, %s, %s, %s, %s>".formatted(this.getClass().getName(), this.objectIdentifier, this.owner, this.sharedTo, this.isGlobal, this.syncRegistryType);
    }
}
