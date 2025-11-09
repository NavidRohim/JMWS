package me.brynview.navidrohim.jmws.client;

import me.brynview.navidrohim.jmws.client.share.ShareRequest;
import org.jetbrains.annotations.Nullable;

public class ClientVariables {

    @Nullable
    public static ShareRequest shareRequest = null;
    @Nullable
    public static String clientJMVersion = null;

    public static boolean clientHasJM = false;
    public static boolean serverHasMod = false;

}
