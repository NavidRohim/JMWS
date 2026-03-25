package me.brynview.navidrohim.jmws.common.api;

import me.brynview.navidrohim.jmws.common.syncing.Syncing;

import java.util.UUID;

public interface Synchronizable
{
    void stopSharingWith(UUID user);
    void stopSharingWithAll();
    void shareWith(UUID toUser);

    void makeGlobal();
    void removeGlobal();

    Syncing getSyncingHandler();
}
