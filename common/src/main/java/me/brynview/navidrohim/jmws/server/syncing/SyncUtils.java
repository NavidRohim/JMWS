package me.brynview.navidrohim.jmws.server.syncing;

import me.brynview.navidrohim.jmws.server.io.UserSharingFile;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import me.brynview.navidrohim.jmws.server.objects.ServerObject;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SyncUtils {
    public static void stopSharing(ServerPlayer user, ServerObject object) {
        SyncUtils.stopSharingWithOfflineUser(user.getUUID(), object);
        ServerPacketHandler.sendUserSync(user, false, false, false);
    }

    public static void stopSharingWithOfflineUser(UUID user, ServerObject object)
    {
        object.stopSharingWith(user);
        UserSharingFile.removeObjectFromUser(user, object.getServerSyncingHandler().info.objectIdentifier, object.getObjectType());
    }
}
