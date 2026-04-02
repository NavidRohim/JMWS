package me.brynview.navidrohim.jmws.client.syncing.api;

import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import me.brynview.navidrohim.jmws.common.utils.SyncUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class ClientBaseObjectWrapper implements ClientObjectWrapper {

    private final SyncInformation info;
    private final boolean isValid;
    private final boolean isLegacy;

    public ClientBaseObjectWrapper(String syncData)
    {
        this.isValid = SyncUtils.isValidSyncField(syncData);
        this.isLegacy = !this.isValid && SyncUtils.isLegacySyncField(syncData);

        if (this.isLegacy) {
            this.info = SyncInformation.SyncInformationFromString(syncData);
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
}
