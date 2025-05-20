package me.brynview.navidrohim.jmws;
import me.brynview.navidrohim.jmws.common.payloads.HandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.utils.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.payloads.RegisterUserPayload;
import me.brynview.navidrohim.jmws.common.utils.JMWSIOInterface;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.server.command.CommandManager.*;

public class JMServer implements ModInitializer {

    public static final String MODID = "jmws";
    public static final String VERSION = JMServer.class.getPackage().getImplementationVersion();
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    @Override
    public void onInitialize() {

        // Packet registering (client)
        PayloadTypeRegistry.playC2S().register(RegisterUserPayload.ID, RegisterUserPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(JMWSActionPayload.ID, JMWSActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HandshakePayload.ID, HandshakePayload.CODEC);

        // Packet registering (server)
        PayloadTypeRegistry.playS2C().register(JMWSActionPayload.ID, JMWSActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakePayload.ID, HandshakePayload.CODEC);

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("jmws").then(literal("update").executes(
                                context -> {
                                    if (context.getSource().getPlayer() != null) {
                                        String jsonString = JsonStaticHelper.makeServerSyncRequestJson();
                                        JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonString);

                                        ServerPlayNetworking.send(context.getSource().getPlayer(), waypointActionPayload);
                                    }
                                    return 1;

                                }))
                        .then(literal("getUpdateInterval").executes(intervalContext -> {
                            if (intervalContext.getSource().getPlayer() != null) {
                                JMWSActionPayload payload = new JMWSActionPayload(JsonStaticHelper.makeDisplayIntervalRequestJson());
                                ServerPlayNetworking.send(intervalContext.getSource().getPlayer(), payload);
                            }
                            return 1;
                        }))
                        .then(literal("clearAll").executes(clearallContext -> {
                            ServerPlayerEntity player = clearallContext.getSource().getPlayer();
                            if (player != null) {
                                 JMWSIOInterface.deleteAllUserWaypoints(player.getUuid());

                                 JMWSActionPayload refreshPayload = new JMWSActionPayload(
                                         JsonStaticHelper.makeWaypointSyncRequestJson()
                                 );
                                 JMWSActionPayload deleteAllClientsidePayload = new JMWSActionPayload(
                                         JsonStaticHelper.makeDeleteClientWaypointRequestJson("*") // * = Delete all
                                 );

                                 ServerPlayNetworking.send(player, refreshPayload);
                                ServerPlayNetworking.send(player, deleteAllClientsidePayload);
                            }
                            return 1;
                        }))
                        .then(literal("nextUpdate").executes(updateDisplayContext -> {
                            if (updateDisplayContext.getSource().getPlayer() != null) {
                                JMWSActionPayload payload = new JMWSActionPayload(JsonStaticHelper.makeDisplayNextUpdateRequestJson());
                                ServerPlayNetworking.send(updateDisplayContext.getSource().getPlayer(), payload);
                            }
                            return 1;
                        }))
                )
        ));
    }
}
