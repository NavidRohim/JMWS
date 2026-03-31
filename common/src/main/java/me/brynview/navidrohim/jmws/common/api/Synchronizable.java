package me.brynview.navidrohim.jmws.common.api;

import me.brynview.navidrohim.jmws.server.syncing.ServerSyncingHandler;

import java.util.UUID;

public interface Synchronizable
{
    void stopSharingWith(UUID user);
    void stopSharingWithAll();
    void shareWith(UUID toUser);

    void makeGlobal();
    void removeGlobal();
    boolean isGlobal();

    CommonSyncHandler getSyncingHandler();
}
