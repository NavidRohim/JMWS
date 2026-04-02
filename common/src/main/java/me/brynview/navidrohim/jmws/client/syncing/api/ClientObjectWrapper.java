package me.brynview.navidrohim.jmws.client.syncing.api;

import java.util.List;
import java.util.UUID;

public interface ClientObjectWrapper {
    String getIdentifier();
    List<String> getSharedTo();
    UUID getOwner();
    boolean getGlobal();

    void setGlobal(boolean global);

    void addSharedTo(String sharedTo);
    void removeSharedTo(String sharedTo);
    void clearSharedTo();

    void update();
    String getSerialization();

    boolean isValid();
    boolean isLegacy();
    boolean isUsable();
}
