package me.brynview.navidrohim.jmws.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import commonnetwork.api.Network;

import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.server.JMWSServerCommon;
import me.brynview.navidrohim.jmws.server.registry.ServerSyncRegistryEntry;
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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// This class is part of the common project, meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code cannot directly use loader-specific concepts such as Forge events;
// however, it will be compatible with all supported mod loaders.
public class JMWSCommon {

    // The loader-specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader-specific projects. This example has some
    // code that gets invoked by the entry point of the loader-specific projects.

    public static MinecraftServer minecraftServerInstance;

    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static final Gson gson = new Gson();
    public static final Gson gsonExcludeNoExpose = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    public static Minecraft minecraftClientInstance = null;
    //public static final Gson gsonExcludeNoExposeNotPretty = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private static void determinePacketAction(PacketContext<JMWSActionPayload> ctx)
    {
        // This is a bodge fix. This is purely a consequence of me not doing things the right way. But I am
        // so far deep now, I cannot reengineer everything just so I can avoid these 4 lines. (client and server use same packets)
        // also ctx.sender() can usually never be null, but it is here.
        if (
                isInternalServer() &&
                Constants.forgeModLoaders.contains(Services.PLATFORM.getPlatformName()) &&
                (ctx.sender() == null || ctx.sender().getUUID() == PlayerUtils.ourUUID()))
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
            ClientPacketHandler.handleHandshake(ctx.message());
        } else {
            PlayerNetworkingHelper.sendHandshakeAndValidate(ctx.sender());
        }
    }

    public static void createServerResources() {
        for (ServerSyncRegistryEntry type : JMWSServerCommon.REGISTRY.getRegistryValues()) {
            try {
                if (!type.isInternal()) {
                    Files.createDirectories(Path.of(type.getRegistryPath()));
                    Constants.getLogger().info("Created directory for object type -> {}", type.getId());

                }
            } catch (IOException e) {
                Constants.getLogger().error("Failed to create server resources for registry type {}. Reason: {}", type.getId(), e);
            }
        }
    }



    public static boolean isInternalServer() {
        if (minecraftClientInstance != null) {
            return minecraftClientInstance.isLocalServer() && minecraftClientInstance.getSingleplayerServer() instanceof IntegratedServer;
        }
        return false;
    }

    public static void init() {

        Network.registerPacket(JMWSActionPayload.type(), JMWSActionPayload.class, JMWSActionPayload.STREAM_CODEC, JMWSCommon::determinePacketAction);
        Network.registerPacket(JMWSHandshakePayload.type(), JMWSHandshakePayload.class, JMWSHandshakePayload.STREAM_CODEC, JMWSCommon::determineHandshakePacketAction);

        if (Services.PLATFORM.side().equals("CLIENT") && Services.PLATFORM.getPlatformName().equals("Fabric"))
        {
            JMWSClientCommon.setupMinecraftClientInstance();
        }

        Constants.getLogger().info("Creating server resources..");
        ServerConfig.ensureExistence();
        createServerResources();
        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to of around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.
    }
}
