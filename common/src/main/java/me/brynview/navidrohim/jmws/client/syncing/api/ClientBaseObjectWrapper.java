package me.brynview.navidrohim.jmws.client.syncing.api;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.plugin.ObjectIdentifierMap;
import me.brynview.navidrohim.jmws.client.syncing.objects.ClientObject;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.common.utils.SyncUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public abstract class ClientBaseObjectWrapper <T> implements ClientObjectWrapper {

    public enum WrapperType
    {
        SYNCHRONISE,
        NATIVE,
        FOREIGN,
        LEGACY
    }

    private final SyncInformation info;
    private final boolean isValid;
    private final boolean isLegacy;
    private final T object;

    public final String pluginId;

    public ClientBaseObjectWrapper(String syncData, T syncedObject, String plugin)
    {
        SyncInformation info1;
        this.isValid = SyncUtils.isValidSyncField(syncData);
        this.isLegacy = !this.isValid && SyncUtils.isLegacySyncField(syncData);
        this.object = syncedObject;
        this.pluginId = plugin;

        if (this.isLegacy) {
            info1 = SyncInformation.syncInformationFromString(syncData);
        } else {
            info1 = null;
        }

        if (getType() == WrapperType.NATIVE)
        {
            info1 = SyncInformation.syncInformationFromString(me.brynview.navidrohim.jmws.common.syncing.SyncUtils.getEmptySyncingInfoString(ObjectIdentifierMap.makeWaypointHash(PlayerUtils.ourUUID(), "g", "name"), UUID.fromString("me"), false));
        }

        this.info = info1;
    }

    @Override
    public final boolean isValid()
    {
        return isValid;
    }

    @Override
    public final boolean isLegacy()
    {
        return isLegacy;
    }

    @Override
    public final boolean isUsable()
    {
        return !isLegacy && isValid;
    }

    @Override
    public final boolean isNative()
    {
        return !isLegacy && !isValid && Constants.allowedMods.contains(this.pluginId);
    }

    public final WrapperType getType()
    {
        if (isUsable()) {
            return WrapperType.SYNCHRONISE;
        } else if (isNative()) {
            return WrapperType.NATIVE;
        } else if (isLegacy()) {
            return WrapperType.LEGACY;
        } else {
            return WrapperType.FOREIGN;
        }
    }

    @NotNull
    public final SyncInformation getInfo()
    {
        if (info != null)
        {
            return info;
        } else {
            throw new IllegalStateException("ClientObjectWrapper is not valid.");
        }
    }

    @NotNull
    public final T getObjectAsClass(Class<T> clazz)
    {
        if (clazz.isAssignableFrom(object.getClass()) && isUsable()) {
            return object;
        }
        throw new IllegalStateException("ClientObjectWrapper is not valid.");
    }

    @Override
    public void setGlobal(boolean global)
    {
        this.getInfo().global = global;
    }

    @Override
    public void addSharedTo(UUID sharedTo)
    {
        this.getInfo().sharedTo.add(sharedTo);
    }

    @Override
    public void removeSharedTo(UUID sharedTo)
    {
        this.getInfo().sharedTo.remove(sharedTo);
    }

    @Override
    public void clearSharedTo()
    {
        this.getInfo().sharedTo.clear();
    }

    @Override
    public String getIdentifier() {
        return this.getInfo().objectIdentifier;
    }

    @Override
    public List<UUID> getSharedTo() {
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
    public String getSerialization() {
        return object.toString();
    }
}
