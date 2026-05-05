package me.brynview.navidrohim.jmws.client.syncing.api;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.exceptions.NoInfoException;
import me.brynview.navidrohim.jmws.client.network.ClientNetworkDispatcher;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncInformation;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncUtils;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.syncing.SyncUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ClientBaseObjectWrapper <T> implements ClientObjectWrapper<T> {

    @Nullable protected ClientSyncInformation info;
    @Nullable protected ClientShareRuleset shareRules = null;

    private Context context;

    private boolean isValid;
    private boolean isLegacy;
    private T object;

    public final String pluginId;
    private final String objectName;
    private final String objectGuid;

    /**
     * Creates a universal identifier from the players UUID, the waypoint's GUID, and name of the object.
     * @param waypointGUID -- GUID of the object being created.
     * @param objectName -- The name of the waypoint or group.
     * @return String -- The universal identifier.
     */

    private static String makeWaypointHash(String waypointGUID, String objectName)
    {
        return DigestUtils.sha256Hex(PlayerUtils.ourUUID() + waypointGUID + objectName);
    }

    public ClientBaseObjectWrapper(String syncData, @Nullable String jsonFormattedRuleset, T syncedObject, String objectName, String objectGuid, String plugin)
    {
        this.object = syncedObject;
        this.objectName = objectName;
        this.objectGuid = objectGuid;

        boolean isInbuilt = this.isInbuilt();
        if (isInbuilt) {
            this.setContext(Context.INBUILT);
        }

        Constants.LoggerHolder.debug(syncData, "SYNC DATA");
        this.isValid = syncData != null && SyncUtils.isValidSyncField(syncData);
        this.isLegacy = !this.isValid && SyncUtils.isLegacySyncField(syncData);

        Constants.getLogger().info("valid: " + this.isValid);
        Constants.getLogger().info("legacy: " + this.isLegacy);
        Constants.getLogger().info("sync data" + syncData);

        this.pluginId = plugin;

        ClientSyncInformation info1 = null;
        if (this.getContext() == Context.SYNCHRONISE) {
            info1 = ClientSyncUtils.syncInformationFromString(syncData, getType());
            this.setShareRules(jsonFormattedRuleset);
        }

        this.setInfo(info1);
    }

    @Override
    public void createRemotely(boolean silent)
    {
        if (getContext() == Context.NATIVE) {
            this.setShareRules(null);
            this.setInfo(ClientSyncUtils.getEmptySyncInformation(makeWaypointHash(objectGuid, objectName), false, getType()));
        }
    }

    @Override
    public void update()
    {
        @Nullable String syncData = this.info != null ? this.info.serialize() : null;
        this.isValid = syncData != null && SyncUtils.isValidSyncField(syncData);
        this.isLegacy = !this.isValid && SyncUtils.isLegacySyncField(syncData);

        this.getContext();
    }

    @Override
    public void setInfo(@Nullable ClientSyncInformation info)
    {
        if (getContext() != Context.INBUILT) {
            this.info = info;
            this.update();
        }
    }

    @Override
    public String getRegistryTypeName()
    {
        return this.getType().getId();
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
    public final void setContext(Context context)
    {
        if (this.context == Context.INBUILT)
        {
            return;
        }
        this.context = context;

    }

    @Override
    public final Context getContext()
    {
        Context type;

        if (context == Context.INBUILT)
        {
            return Context.INBUILT;
        }

        if (isUsable()) {
            type = Context.SYNCHRONISE;
        } else if (isNative()) {
            type = Context.NATIVE;
        } else if (isLegacy()) {
            type = Context.LEGACY;
        } else {
            type = Context.FOREIGN;
        }

        this.setContext(type);
        return type;
    }

    @Nullable
    public final ClientSyncInformation getInfo()
    {
        return info;
    }

    @Override
    public @Nullable ClientShareRuleset getShareRules()
    {
        return shareRules;
    }

    private void setShareRules(@Nullable String jsonFormattedRuleset)
    {
        if (jsonFormattedRuleset != null)
        {
            this.shareRules = new ClientShareRuleset(jsonFormattedRuleset);
        } else {
            this.shareRules = new ClientShareRuleset(null);
        }
    }

    @Override
    public T getNativeObject()
    {
        return object;
    }

    @Override
    public void setGlobal(boolean global)
    {
        if (this.info != null)
        {
            this.getInfo().isGlobal = global;
            ClientNetworkDispatcher.makeGlobal(this, global);
        } else {
            throw new NoInfoException();
        }
    }

    @Override
    public void setNativeObject(@NonNull T nativeObject)
    {
        this.object = nativeObject;
    }

    @Override
    public void sendShareRequest(UUID sharedTo)
    {
        JMWSClientCommon.outgoingShareRequests.sendRequest(sharedTo, this);
    }

    @Override
    public void addSharedTo(UUID sharedTo)
    {
        if (this.info != null)
        {
            this.getInfo().sharedTo.add(sharedTo);
        } else {
            throw new NoInfoException();
        }

    }

    @Override
    public void removeSharedTo(UUID sharedTo)
    {
        if (this.info != null)
        {
            this.getInfo().sharedTo.remove(sharedTo);
            ClientNetworkDispatcher.PeerToPeer.removeShare(sharedTo, this.info);
        } else {
            throw new NoInfoException();
        }

    }

    @Override
    public void clearSharedTo()
    {
        if (this.info != null)
        {
            this.getInfo().sharedTo.clear();
            //ClientNetworkDispatcher.removeShareFromAll( this); TODO
        } else {
            throw new NoInfoException();
        }
    }

    @Override
    public final String getIdentifier() {
        if (this.info != null) {
            return this.getInfo().objectIdentifier;
        }
        throw new NoInfoException();
    }

    @Override
    public final Set<UUID> getSharedTo() {
        if (this.info != null) {
            return new HashSet<>(this.getInfo().sharedTo);
        }
        throw new NoInfoException();
    }

    @Override
    public final boolean isSharing() {
        if (this.info != null) {
            return !this.getInfo().sharedTo.isEmpty();
        }
        throw new NoInfoException();
    }

    @Override
    public final UUID getOwner()
    {
        if (this.info != null) {
            return this.getInfo().owner;
        }
        throw new NoInfoException();
    }

    @Override
    public final boolean getGlobal()
    {
        if (this.info != null) {
            return this.getInfo().isGlobal;
        }
        throw new NoInfoException();
    }

    @Override
    public String getSerialization() {
        return getNativeObject().toString();
    }
}
