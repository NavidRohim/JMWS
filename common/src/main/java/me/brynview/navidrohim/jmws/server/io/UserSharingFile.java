package me.brynview.navidrohim.jmws.server.io;

import me.brynview.navidrohim.jmws.common.enums.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.common.syncing.share.io.CommonShareIO;

import java.util.UUID;

public class UserSharingFile extends CommonShareIO {
    public UserSharingFile(UUID userUUID) {
        super(JMWSServerIO.PathUtils.getObjectFilename(userUUID, "SHARED", ServerSyncRegistry.SHARED, false));
    }

    @Override
    public boolean addToShared(String sharedValue, ServerSyncRegistry sharedServerSyncRegistry)
    {
        boolean b = super.addToShared(sharedValue, sharedServerSyncRegistry);
        writeSharedList();
        return b;
    }

    @Override
    public boolean removeFromShared(String sharedValue, ServerSyncRegistry sharedServerSyncRegistry)
    {
        boolean b = super.removeFromShared(sharedValue, sharedServerSyncRegistry);
        writeSharedList();
        return b;
    }

    public static void removeObjectFromUser(UUID playerUUID, String objectIdentifier, ServerSyncRegistry sharedServerSyncRegistry)
    {
        try (UserSharingFile usf = new UserSharingFile(playerUUID))
        {
            usf.removeFromShared(objectIdentifier, sharedServerSyncRegistry);
        }
    }
}
