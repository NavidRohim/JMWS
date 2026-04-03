package me.brynview.navidrohim.jmws.common.utils;

import journeymap.api.v2.common.waypoint.Waypoint;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.syncing.SyncInformation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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
    public static SyncInformation getIdentifier(Waypoint waypoint)
    {
        return SyncInformation.syncInformationFromString(waypoint.getCustomData(Constants.MODID));
    }
}
