package me.brynview.navidrohim.jmws.server.io;

import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistryEntry;
import me.brynview.navidrohim.jmws.common.syncing.share.io.CommonShareIO;

import java.util.UUID;

public class UserSharingFile extends CommonShareIO {
    public UserSharingFile(UUID userUUID) {
        super(JMWSServerIO.PathUtils.getObjectFilename(userUUID, "SHARED", ServerSyncRegistry.SHARED, false));
    }

    public static void removeUserFromShare(UUID with, String objectIdentifier, ServerSyncRegistryEntry objectType) {
    }

    @Override
    public boolean addToShared(String sharedValue, ServerSyncRegistryEntry sharedServerSyncRegistry)
    {
        boolean b = super.addToShared(sharedValue, sharedServerSyncRegistry);
        writeSharedList();
        return b;
    }

    @Override
    public boolean removeFromShared(String sharedValue, ServerSyncRegistryEntry sharedServerSyncRegistry)
    {
        boolean b = super.removeFromShared(sharedValue, sharedServerSyncRegistry);
        writeSharedList();
        return b;
    }

    public static void removeObjectFromUser(UUID playerUUID, String objectIdentifier, ServerSyncRegistryEntry<?> sharedServerSyncRegistry)
    {
        try (UserSharingFile usf = new UserSharingFile(playerUUID))
        {
            usf.removeFromShared(objectIdentifier, sharedServerSyncRegistry);
        }
    }
}
