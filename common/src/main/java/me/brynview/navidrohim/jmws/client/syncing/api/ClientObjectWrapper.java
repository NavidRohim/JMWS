package me.brynview.navidrohim.jmws.client.syncing.api;

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
    void updateRemotely();

    boolean isValid();
    boolean isLegacy();
    boolean isUsable();
    boolean isNative();
    ClientBaseObjectWrapper.WrapperType getType();


}
