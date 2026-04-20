package me.brynview.navidrohim.jmws.client.syncing.api;

import me.brynview.navidrohim.jmws.client.syncing.ClientSyncInformation;
import me.brynview.navidrohim.jmws.client.syncing.SyncRegistry;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface ClientObjectWrapper<T> {

    String getName();
    String getSerialization();
    String getIdentifier();

    /*
    Note; in ARGB format
     */
    int getColour();

    UUID getOwner();
    T getNativeObject();

    boolean getGlobal();
    void setGlobal(boolean global);

    void addSharedTo(UUID sharedTo);
    void removeSharedTo(UUID sharedTo);
    void sendShareRequest(UUID sharedTo);

    void clearSharedTo();
    Set<UUID> getSharedTo();

    void createRemotely(boolean silent);
    void removeRemotely(boolean silent);
    void updateRemotely();

    void createLocally();
    void removeLocally();

    ClientSyncInformation getInfo();
    void setInfo(ClientSyncInformation info);
    void update();

    boolean isLegacy();
    boolean isUsable();
    boolean isNative();
    boolean isInbuilt();

    void setNativeObject(@NotNull T nativeObject);
    void setContext(Context context);
    Context getContext();
    SyncRegistry getType();

}
