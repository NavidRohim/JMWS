package me.brynview.navidrohim.jmws.common.utils;

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
}
