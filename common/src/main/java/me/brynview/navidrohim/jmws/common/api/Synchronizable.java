package me.brynview.navidrohim.jmws.common.api;

import java.util.UUID;

public interface Synchronizable
{
    void stopSharingWith(UUID user);
    void stopSharingWithAll();

    void makeGlobal();
    void removeGlobal();
    boolean isGlobal();
}
