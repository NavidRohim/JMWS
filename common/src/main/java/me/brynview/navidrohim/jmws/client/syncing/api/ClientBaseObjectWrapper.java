package me.brynview.navidrohim.jmws.client.syncing.api;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.plugin.ObjectIdentifierMap;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.common.utils.SyncUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class ClientBaseObjectWrapper <T> implements ClientObjectWrapper {

    public enum WrapperContext
    {
        SYNCHRONISE,
        NATIVE,
        FOREIGN,
        LEGACY,
        INBUILT
    }

    @Nullable protected SyncInformation info;
    private WrapperContext wrapperContext;

    private boolean isValid;
    private boolean isLegacy;
    private final T object;

    public final String pluginId;
    private final String objectName;
    private final String objectGuid;

    public ClientBaseObjectWrapper(String syncData, T syncedObject, String objectName, String objectGuid, String plugin)
    {
        this.objectName = objectName;
        this.objectGuid = objectGuid;

        boolean isInbuilt = this.isInbuilt();
        if (isInbuilt) {
            this.setContext(WrapperContext.INBUILT);
        }

        SyncInformation info1;
        Constants.LoggerHolder.debug(syncData, "SYNC DATA");
        this.isValid = syncData != null && SyncUtils.isValidSyncField(syncData);
        this.isLegacy = !this.isValid && SyncUtils.isLegacySyncField(syncData);

        Constants.getLogger().info("valid: " + this.isValid);
        Constants.getLogger().info("legacy: " + this.isLegacy);
        Constants.getLogger().info("sync data" + syncData);

        this.object = syncedObject;
        this.pluginId = plugin;

        if (this.getContext() == WrapperContext.SYNCHRONISE) {
            info1 = SyncInformation.syncInformationFromString(syncData);
        } else {
            info1 = null;
        }

        this.setInfo(info1);
    }

    @Override
    public void createRemotely(boolean silent)
    {
        if (getContext() == WrapperContext.NATIVE) {
            this.setInfo(SyncInformation.syncInformationFromString(SyncUtils.getEmptySyncingInfoString(ObjectIdentifierMap.makeWaypointHash(objectGuid, objectName), PlayerUtils.ourUUID(), false)));
        }
    }

    @Override
    public void update()
    {
        @Nullable String syncData = this.info != null ? this.info.getSyncInformationAsString() : null;
        this.isValid = syncData != null && SyncUtils.isValidSyncField(syncData);
        this.isLegacy = !this.isValid && SyncUtils.isLegacySyncField(syncData);

        this.getContext();
    }

    @Override
    public void setInfo(@Nullable SyncInformation info)
    {
        this.info = info;

        this.update();
    }

    @Override
    public boolean isValid()
    {
        return isValid;
    }

    @Override
    public boolean isLegacy()
    {
        return isLegacy;
    }

    @Override
    public boolean isUsable()
    {
        Constants.LoggerHolder.debug("legacy: %s valid: %s".formatted(isLegacy, isValid), "USABLE");
        return !isLegacy && isValid;
    }

    @Override
    public final boolean isNative()
    {
        return !isLegacy && !isValid && Constants.allowedMods.contains(this.pluginId);
    }

    @Override
    public final void setContext(WrapperContext context)
    {
        if (wrapperContext == WrapperContext.INBUILT)
        {
            return;
        }
        this.wrapperContext = context;

    }

    @Override
    public final WrapperContext getContext()
    {
        WrapperContext type;

        if (wrapperContext == WrapperContext.INBUILT)
        {
            return WrapperContext.INBUILT;
        }

        if (isUsable()) {
            type = WrapperContext.SYNCHRONISE;
        } else if (isNative()) {
            type = WrapperContext.NATIVE;
        } else if (isLegacy()) {
            type = WrapperContext.LEGACY;
        } else {
            type = WrapperContext.FOREIGN;
        }

        this.setContext(type);
        return type;
    }

    @Nullable
    public final SyncInformation getInfo()
    {
        return info;
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
        this.getInfo().isGlobal = global;
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
        if (this.info != null) {
            return this.getInfo().objectIdentifier;
        }
        throw new IllegalStateException("getInfo() is null. Was the object created using createRemotely() before calling this?");
    }

    @Override
    public List<UUID> getSharedTo() {
        if (this.info != null) {
            return this.getInfo().sharedTo;
        }
        throw new IllegalStateException("getInfo() is null. Was the object created using createRemotely() before calling this?");
    }

    @Override
    public UUID getOwner()
    {
        if (this.info != null) {
            return this.getInfo().owner;
        }
        throw new IllegalStateException("getInfo() is null. Was the object created using createRemotely() before calling this?");
    }

    @Override
    public boolean getGlobal()
    {
        if (this.info != null) {
            return this.getInfo().isGlobal;
        }
        throw new IllegalStateException("getInfo() is null. Was the object created using createRemotely() before calling this?");
    }

    @Override
    public String getSerialization() {
        return object.toString();
    }
}
