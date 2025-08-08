package me.navidrohim.jmws;


import me.navidrohim.jmws.payloads.*;
import me.navidrohim.jmws.plugin.ConfigInterface;
import me.navidrohim.jmws.plugin.PacketHandler;
import me.navidrohim.jmws.server.config.ServerConfig;
import me.navidrohim.jmws.server.network.ServerPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;


import java.io.File;

import static me.navidrohim.jmws.client.ClientHandshakeHandler.timeoutTask;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.

    public static Minecraft minecraftClientInstance = null;
    public static ConfigInterface config = null;
    public static SyncCounter syncCounter = null;

    public static boolean serverHasMod = false;

    public static void setServerModStatus(boolean serverModStatus)
    {
        serverHasMod = serverModStatus;

        if (timeoutTask != null && !timeoutTask.isDone()) {
            timeoutTask.cancel(false);
        }

        if (!serverModStatus)
        {
            syncCounter.resetSyncCounter();
        }
    }

    public static int getSyncFrequency()
    {
        return CommonClass.syncCounter.getTickCounterUpdateThreshold() / 20;
    }

    public static int timeUntilNextSync()
    {
        // syncCounter can be null but the chance of it ever being null while this method is being called is none.
        // Same with getSyncFrequency
        return (CommonClass.syncCounter.getTickCounterUpdateThreshold() - CommonClass.syncCounter.getCurrentTickCount()) / 20;
    }

    public static void _createServerResources() {
        ServerConfig.ensureExistence();
        new File("./jmws").mkdir();
    }


    public static boolean getEnabledStatus() {
        return serverHasMod && config.enabled && (config.uploadGroups || config.uploadWaypoints) && !minecraftClientInstance.isSingleplayer();
    }

    public static String side() {
        String side = FMLCommonHandler.instance().getSide().toString();
        if (side.equalsIgnoreCase("SERVER") || side.equalsIgnoreCase("DEDICATED_SERVER"))
        {
            return "SERVER";
        }
        return "CLIENT";
    }

    public static void init() {

        if (side().equals("SERVER"))
        {
            JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSActionMessage.JMWSActionMessageHandler.class, JMWSActionMessage.class, 0, Side.SERVER);
            JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSHandshakeMessage.JMWSHandshakeMessageHandler.class, JMWSHandshakeMessage.class, 1, Side.SERVER);
            JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSHandshakeReplyMessage.JMWSHandshakeReplyMessageHandler.class, JMWSHandshakeReplyMessage.class, 2, Side.SERVER);

            Constants.getLogger().info("Creating server resources..");
            _createServerResources();

        } else {

            JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSActionMessage.JMWSActionMessageHandler.class, JMWSActionMessage.class, 0, Side.CLIENT);
            JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSHandshakeMessage.JMWSHandshakeMessageHandler.class, JMWSHandshakeMessage.class, 1, Side.CLIENT);
            JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSHandshakeReplyMessage.JMWSHandshakeReplyMessageHandler.class, JMWSHandshakeReplyMessage.class, 2, Side.CLIENT);
        }

        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.
    }

    public static void setupMinecraftClientInstance()
    {
        minecraftClientInstance = Minecraft.getMinecraft();
        syncCounter = new SyncCounter();
    }
}
