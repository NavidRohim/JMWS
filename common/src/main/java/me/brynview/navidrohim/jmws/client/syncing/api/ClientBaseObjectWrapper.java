package me.brynview.navidrohim.jmws.client.syncing.api;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.objects.ClientObject;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.common.utils.SyncUtils;
import org.jetbrains.annotations.NotNull;

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
    protected final ClientObject<? extends ClientBaseObjectWrapper<Object>> parent;

    public ClientBaseObjectWrapper(String syncData, T syncedObject, ClientObject<? extends ClientBaseObjectWrapper<Object>> parent, String plugin)
    {
        this.isValid = SyncUtils.isValidSyncField(syncData);
        this.isLegacy = !this.isValid && SyncUtils.isLegacySyncField(syncData);
        this.object = syncedObject;
        this.parent = parent;
        this.pluginId = plugin;

        if (this.isLegacy) {
            this.info = SyncInformation.syncInformationFromString(syncData);
        } else {
            this.info = null;
        }
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
}
