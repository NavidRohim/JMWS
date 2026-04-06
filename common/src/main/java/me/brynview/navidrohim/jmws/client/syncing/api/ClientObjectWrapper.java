package me.brynview.navidrohim.jmws.client.syncing.api;

import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;

import java.util.List;
import java.util.UUID;

public interface ClientObjectWrapper {

    String getSerialization();
    String getIdentifier();
    UUID getOwner();

    boolean getGlobal();
    void setGlobal(boolean global);

    void addSharedTo(UUID sharedTo);
    void removeSharedTo(UUID sharedTo);
    void clearSharedTo();
    List<UUID> getSharedTo();

    void createRemotely(boolean silent);
    void removeRemotely(boolean silent);
    void updateRemotely();

    void setInfo(SyncInformation info);
    void update();

    boolean isValid();
    boolean isLegacy();
    boolean isUsable();
    boolean isNative();
    boolean isInbuilt();


    void setContext(ClientBaseObjectWrapper.WrapperContext context);
    ClientBaseObjectWrapper.WrapperContext getContext();


}
