package me.brynview.navidrohim.jmws.server;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.events.CommonEvents;
import me.brynview.navidrohim.jmws.server.config.ServerConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.mixin.command.CommandManagerMixin;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.*;

public class JMWSServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer()
    {
        // no longer lonely
        Constants.getLogger().info("Initialised server on JMWS");

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            CommonEvents.handleJoin(handler.player, false, false);
        }));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CommonClass.minecraftServerInstance = server;
        });
    }
}
