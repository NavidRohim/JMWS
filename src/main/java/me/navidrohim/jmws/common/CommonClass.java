package me.navidrohim.jmws.common;


import me.navidrohim.jmws.client.SyncCounter;
import me.navidrohim.jmws.client.config.ClientSideServerConfigObject;
import me.navidrohim.jmws.common.payloads.JMWSActionMessage;
import me.navidrohim.jmws.common.payloads.JMWSHandshakeReplyMessage;
import me.navidrohim.jmws.common.payloads.JMWSNetworkWrapper;
import me.navidrohim.jmws.common.config.ConfigInterface;
import me.navidrohim.jmws.server.config.ServerConfig;
import me.navidrohim.jmws.server.events.ForgeServerEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;


import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.

    public static Minecraft minecraftClientInstance = null;
    public static ConfigInterface config = new ConfigInterface();// null
    public static SyncCounter syncCounter = null;

    public static final boolean debug = true;

    public static boolean serverHasMod = false;
    public static boolean hasMixinBooter = false;
    public static boolean hasJourneyMap;
    public static boolean clientHasJM;

    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public static ClientSideServerConfigObject serverConfig;

    // config

    public static void setServerModStatus(boolean serverModStatus)
    {
        serverHasMod = serverModStatus;

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
        new File("./jmws").mkdir();
    }


    public static boolean getEnabledStatus() {
        return hasJourneyMap && hasMixinBooter && serverHasMod && ConfigInterface.enabled && !minecraftClientInstance.isSingleplayer();
    }

    public static boolean isInternalServer() {
        if (CommonClass.minecraftClientInstance instanceof Minecraft) {
            return CommonClass.minecraftClientInstance.isIntegratedServerRunning() && CommonClass.minecraftClientInstance.getIntegratedServer() instanceof IntegratedServer;
        }
        return false;
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

        Constants.getLogger().info("Creating server resources..");
        _createServerResources();
        ServerConfig.ensureExistence();

        MinecraftForge.EVENT_BUS.register(ForgeServerEvents.class);

        JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSActionMessage.JMWSActionMessageHandler.class, JMWSActionMessage.class, 0, Side.SERVER);
        JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSHandshakeReplyMessage.JMWSHandshakeReplyMessageHandler.class, JMWSHandshakeReplyMessage.class, 1, Side.SERVER);

        if (!side().equals("SERVER"))
        {

            JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSActionMessage.JMWSActionMessageHandler.class, JMWSActionMessage.class, 2, Side.CLIENT);
            JMWSNetworkWrapper.INSTANCE.registerMessage(JMWSHandshakeReplyMessage.JMWSHandshakeReplyMessageHandler.class, JMWSHandshakeReplyMessage.class, 3, Side.CLIENT);
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
