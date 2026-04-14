package me.brynview.navidrohim.jmws.client.syncing.api;

import me.brynview.navidrohim.jmws.client.syncing.SyncObjectType;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ClientObjectWrapper<T> {

    String getName();
    String getSerialization();
    String getIdentifier();
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

    void setInfo(SyncInformation info);
    void update();

    boolean isValid();
    boolean isLegacy();
    boolean isUsable();
    boolean isNative();
    boolean isInbuilt();


    void setContext(Context context);
    Context getContext();
    SyncObjectType getType();

}
