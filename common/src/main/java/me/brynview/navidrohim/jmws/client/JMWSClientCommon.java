package me.brynview.navidrohim.jmws.client;

import me.brynview.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.brynview.navidrohim.jmws.client.config.ConfigInterface;
import me.brynview.navidrohim.jmws.client.share.IncomingShareRequests;
import me.brynview.navidrohim.jmws.client.share.OutgoingShareRequests;
import me.brynview.navidrohim.jmws.client.syncing.SyncCounter;
import me.brynview.navidrohim.jmws.client.ui.screen.NotificationAlertScreen;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class JMWSClientCommon {

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

    public static final OutgoingShareRequests outgoingShareRequests = new OutgoingShareRequests();
    public static final IncomingShareRequests incomingShareRequests = new IncomingShareRequests();

    public static @Nullable NotificationAlertScreen currentShareScreen = null;

    public static void setupMinecraftClientInstance()
    {
        JMWSCommon.minecraftClientInstance = Minecraft.getInstance();
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

    public static void setCurrentUIScreen(NotificationAlertScreen screen)
    {
        currentShareScreen = screen;
        JMWSCommon.minecraftClientInstance.setScreen(screen);
    }
}
