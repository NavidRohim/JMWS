package me.brynview.navidrohim.jmws.client;

import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.client.config.ConfigInterface;
import me.brynview.navidrohim.jmws.client.syncing.SyncCounter;
import me.brynview.navidrohim.jmws.common.CommonClass;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class ClientCommonClass {

    public static boolean clientHasJM = false;
    public static boolean serverHasMod = false;
    public static boolean didHandshake = false;
    public static boolean isMapping = false;
    public static boolean isBusy = false;

    @Nullable
    public static String clientJMVersion = null;
    public static ClientSideServerConfigObject serverConfig = ClientSideServerConfigObject.empty();
    public static SyncCounter syncCounter = null;
    public static ConfigInterface config = null;

    public static void setupMinecraftClientInstance()
    {
        CommonClass.minecraftClientInstance = Minecraft.getInstance();
        syncCounter = new SyncCounter();
    }

    public static void setServerModStatus(boolean serverModStatus)
    {
        serverHasMod = serverModStatus;

        if (!serverModStatus)
        {
            syncCounter.resetSyncCounter();
        }
    }

}
