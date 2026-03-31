package me.brynview.navidrohim.jmws.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import commonnetwork.api.Network;

import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ClientCommonClass;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSHandshakePayload;
import me.brynview.navidrohim.jmws.common.platform.Services;

import me.brynview.navidrohim.jmws.client.network.ClientPacketHandler;

import me.brynview.navidrohim.jmws.server.config.ServerConfig;

import me.brynview.navidrohim.jmws.server.network.PlayerNetworkingHelper;
import me.brynview.navidrohim.jmws.server.network.ServerPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;


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
    public static MinecraftServer minecraftServerInstance;

    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static final Gson gson = new Gson();
    public static final Gson gsonExcludeNoExpose = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    public static final Gson gsonExcludeNoExposeNotPretty = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private static void determinePacketAction(PacketContext<JMWSActionPayload> ctx)
    {
        // This is a bodge fix. This is purely a consequence of me not doing things the right way. But I am
        // so far deep now, I cannot reengineer everything just so I can avoid these 4 lines. (client and server use same packets)
        // also ctx.sender() can usually never be null, but it is here.
        if (
                isInternalServer() &&
                Constants.forgeModLoaders.contains(Services.PLATFORM.getPlatformName()) &&
                (ctx.sender() == null || ctx.sender().getUUID() == CommonClass.minecraftClientInstance.player.getUUID()))
        {
            return;
        }

        if (Side.CLIENT.equals(ctx.side()))
        {
            if (!isInternalServer())
            {
                ClientPacketHandler.handlePacket(ctx);
            } else {
                ServerPacketHandler.handleIncomingActionCommand(ctx, ctx.sender());
            }
        } else {
            ServerPacketHandler.handleIncomingActionCommand(ctx, ctx.sender());
        }
    }

    private static void determineHandshakePacketAction(PacketContext<JMWSHandshakePayload> ctx)
    {
        if (Side.CLIENT.equals(ctx.side()))
        {
            ClientPacketHandler.HandshakeHandler(ctx.message());
        } else {
            PlayerNetworkingHelper.sendHandshakeAndValidate(ctx.sender());
        }
    }

    public static void createServerResources() {
        new File("./jmws").mkdir();
        new File("./jmws/groups").mkdir();
        new File("./jmws/users").mkdir();
    }


    public static boolean isInternalServer() {
        if (CommonClass.minecraftClientInstance != null) {
            return CommonClass.minecraftClientInstance.isLocalServer() && CommonClass.minecraftClientInstance.getSingleplayerServer() instanceof IntegratedServer;
        }
        return false;
    }

    public static void init() {

        Network.registerPacket(JMWSActionPayload.type(), JMWSActionPayload.class, JMWSActionPayload.STREAM_CODEC, CommonClass::determinePacketAction);
        Network.registerPacket(JMWSHandshakePayload.type(), JMWSHandshakePayload.class, JMWSHandshakePayload.STREAM_CODEC, CommonClass::determineHandshakePacketAction);

        if (Services.PLATFORM.side().equals("CLIENT") && Services.PLATFORM.getPlatformName().equals("Fabric"))
        {
            ClientCommonClass.setupMinecraftClientInstance();
        }

        Constants.getLogger().info("Creating server resources..");
        ServerConfig.ensureExistence();
        createServerResources();

        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.
    }
}
