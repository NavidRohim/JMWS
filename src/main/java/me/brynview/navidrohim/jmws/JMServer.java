package me.brynview.navidrohim.jmws;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.option.Config;
import me.brynview.navidrohim.jmws.common.payloads.WaypointActionPayload;
import me.brynview.navidrohim.jmws.common.utils.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.payloads.RegisterUserPayload;
import me.brynview.navidrohim.jmws.common.utils.WaypointIOInterface;
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
    public static final String VERSION = "1.0.4-beta1";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    @Override
    public void onInitialize() {
        // Packet registering (client)
        PayloadTypeRegistry.playC2S().register(RegisterUserPayload.ID, RegisterUserPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WaypointActionPayload.ID, WaypointActionPayload.CODEC);

        // Packet registering (server)
        PayloadTypeRegistry.playS2C().register(WaypointActionPayload.ID, WaypointActionPayload.CODEC);

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("jmws").then(literal("update").executes(
                                context -> {
                                    if (context.getSource().getPlayer() != null) {
                                        String jsonString = JsonStaticHelper.makeServerUpdateRequestJson();
                                        WaypointActionPayload waypointActionPayload = new WaypointActionPayload(jsonString);

                                        ServerPlayNetworking.send(context.getSource().getPlayer(), waypointActionPayload);
                                    }
                                    return 1;

                                }))
                        .then(literal("getUpdateInterval").executes(intervalContext -> {
                            if (intervalContext.getSource().getPlayer() != null) {
                                WaypointActionPayload payload = new WaypointActionPayload(JsonStaticHelper.makeEmptyServerCommandRequestJson("display_interval"));
                                ServerPlayNetworking.send(intervalContext.getSource().getPlayer(), payload);
                            }
                            return 1;
                        }))
                        .then(literal("clearAll").executes(clearallContext -> {
                            ServerPlayerEntity player = clearallContext.getSource().getPlayer();
                            if (player != null) {
                                 WaypointIOInterface.deleteAllUserWaypoints(player.getUuid());

                                 WaypointActionPayload refreshPayload = new WaypointActionPayload(
                                         JsonStaticHelper.makeServerUpdateRequestJson()
                                 );
                                 WaypointActionPayload deleteAllClientsidePayload = new WaypointActionPayload(
                                         JsonStaticHelper.makeDeleteClientWaypointRequestJson("*") // * = Delete all
                                 );

                                 ServerPlayNetworking.send(player, refreshPayload);
                                ServerPlayNetworking.send(player, deleteAllClientsidePayload);
                            }
                            return 1;
                        }))
                )
        ));
    }
}
