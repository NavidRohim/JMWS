package me.brynview.navidrohim.jmws.common.utils;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncInformation;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncUtils;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncRegistry;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.ServerSyncRegistry;
import me.brynview.navidrohim.jmws.server.syncing.ServerSyncingInformation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class SyncUtils {

    private static final String[] syncFieldKeys = {
            "objectIdentifier",
            "sharedTo",
            "owner",
            "isGlobal"
    };

    public static boolean isLegacySyncField(@Nullable String field) {
        if (field != null && field.length() == 64)
        {
            for (int i = 0; i < field.length(); i++) {
                char c = field.charAt(i);
                if (!Character.isLetterOrDigit(c))
                    return false;
            }

            return true;
        }
        return false;
    }

    public static boolean isValidSyncField(@Nullable String input) {

        if (input == null) { return true; }
        return Arrays.stream(syncFieldKeys).allMatch(input::contains);
    }

    @Nullable
    public static ClientSyncInformation getIdentifier(Waypoint waypoint)
    {
        return ClientSyncUtils.syncInformationFromString(waypoint.getCustomData(Constants.MODID), ClientSyncRegistry.WAYPOINT);
    }

    public static String getEmptySyncingInfoString(String objectIdentifier, UUID owner, boolean isGlobal, ServerSyncRegistry registryType) {
        return JMWSCommon.gson.toJson(new ServerSyncingInformation(objectIdentifier, owner, Set.of(), isGlobal, registryType));
    }
}
