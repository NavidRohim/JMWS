package me.brynview.navidrohim.jmws.server.io;

import me.brynview.navidrohim.jmws.common.enums.FetchType;
import me.brynview.navidrohim.jmws.common.share.io.CommonShareIO;

import java.util.List;
import java.util.UUID;

public class UserSharingFile extends CommonShareIO {
    public UserSharingFile(UUID userUUID) {
        super(JMWSServerIO.Utils.getNewObjectFilename(userUUID, "", FetchType.SHARED));
    }

    @Override
    public void addToShared(String sharedValue)
    {
        super.addToShared(sharedValue);
        writeSharedList();
    }

    @Override
    public void removeFromShared(String sharedValue)
    {
        super.removeFromShared(sharedValue);
        writeSharedList();
    }

    public static void removeObjectFromUser(UUID playerUUID, String objectIdentifier)
    {
        try (UserSharingFile usf = new UserSharingFile(playerUUID))
        {
            usf.removeFromShared(objectIdentifier);
        }
    }
}
