package me.brynview.navidrohim.jmws;

import commonnetwork.api.Dispatcher;
import commonnetwork.api.Network;

import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import me.brynview.navidrohim.jmws.payloads .JMWSActionPayload;
import me.brynview.navidrohim.jmws.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.platform.Services;
import me.brynview.navidrohim.jmws.plugin.ConfigInterface;
import me.brynview.navidrohim.jmws.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.plugin.PacketHandler;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.item.Items;

import java.io.File;

import static me.brynview.navidrohim.jmws.client.ClientHandshakeHandler.timeoutTask;

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

    private static void _determinePacketAction(PacketContext<JMWSActionPayload> ctx)
    {
        if (Side.CLIENT.equals(ctx.side()))
        {
            PacketHandler.handlePacket(ctx);
        } else {
            ServerPacketHandler.handleIncomingActionCommand(ctx, ctx.sender());
        }
    }

    private static void _determineHandshakePacketAction(PacketContext<JMWSHandshakePayload> ctx)
    {
        if (Side.CLIENT.equals(ctx.side()))
        {
            PacketHandler.HandshakeHandler(ctx.message());
        } else {
            Dispatcher.sendToClient(ctx.message(), ctx.sender());
        }
    }

    public static void _createServerResources() {
        new File("./jmws").mkdir();
        new File("./jmws/groups").mkdir();
    }


    public static boolean getEnabledStatus() {
        return serverHasMod && config.enabled.get() && (config.uploadGroups.get() || config.uploadWaypoints.get()) && !minecraftClientInstance.isSingleplayer();
    }

    public static void init() {

        Network.registerPacket(JMWSActionPayload.type(), JMWSActionPayload.class, JMWSActionPayload.STREAM_CODEC, CommonClass::_determinePacketAction);
        Network.registerPacket(JMWSHandshakePayload.type(), JMWSHandshakePayload.class, JMWSHandshakePayload.STREAM_CODEC, CommonClass::_determineHandshakePacketAction);

        if (Services.PLATFORM.side().equals("CLIENT") && Services.PLATFORM.getPlatformName().equals("Fabric"))
        {
            CommonClass.setupMinecraftClientInstance();
        }

        if (Services.PLATFORM.side().equals("SERVER"))
        {
            _createServerResources();
        }

        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.
    }

    public static void setupMinecraftClientInstance()
    {
        minecraftClientInstance = Minecraft.getInstance();
        syncCounter = new SyncCounter();
    }
}
