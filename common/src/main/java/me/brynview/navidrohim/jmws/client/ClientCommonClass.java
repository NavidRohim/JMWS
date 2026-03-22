package me.brynview.navidrohim.jmws.client;

import me.brynview.navidrohim.jmws.client.commands.ClientCommands;
import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.client.config.ConfigInterface;
import me.brynview.navidrohim.jmws.client.helper.PlayerHelper;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.client.share.OutgoingShareRequests;
import me.brynview.navidrohim.jmws.common.CommonClass;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class ClientCommonClass {

    @Nullable
    public static String clientJMVersion = null;

    public static boolean clientHasJM = false;
    public static boolean serverHasMod = false;

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

    public static void clearCache()
    {
        ClientCommands.sync();
        setServerModStatus(false);
        serverConfig = ClientSideServerConfigObject.empty();
        PlayerHelper.clearWarningAlertCache();

        IncomingShareRequests.clearAll();
        OutgoingShareRequests.clearAll();
    }

}
