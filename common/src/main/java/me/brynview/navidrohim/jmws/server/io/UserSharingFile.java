package me.brynview.navidrohim.jmws.server.io;

import me.brynview.navidrohim.jmws.common.enums.ObjectType;
import me.brynview.navidrohim.jmws.common.syncing.share.io.CommonShareIO;

import java.util.UUID;

public class UserSharingFile extends CommonShareIO {
    public UserSharingFile(UUID userUUID) {
        super(JMWSServerIO.Utils.getNewObjectFilename(userUUID, "SHARED", ObjectType.SHARED));
    }

    @Override
    public boolean addToShared(String sharedValue, ObjectType sharedObjectType)
    {
        boolean b = super.addToShared(sharedValue, sharedObjectType);
        writeSharedList();
        return b;
    }

    @Override
    public boolean removeFromShared(String sharedValue, ObjectType sharedObjectType)
    {
        boolean b = super.removeFromShared(sharedValue, sharedObjectType);
        writeSharedList();
        return b;
    }

    public static void removeObjectFromUser(UUID playerUUID, String objectIdentifier, ObjectType sharedObjectType)
    {
        try (UserSharingFile usf = new UserSharingFile(playerUUID))
        {
            usf.removeFromShared(objectIdentifier, sharedObjectType);
        }
    }
}
