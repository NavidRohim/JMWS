package me.brynview.navidrohim.jmws;
import me.brynview.navidrohim.jmws.common.payloads.HandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import me.brynview.navidrohim.jmws.common.helpers.JsonStaticHelper;
import me.brynview.navidrohim.jmws.common.io.JMWSIOInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.network.ServerPlayerEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.*;

public class JMWS implements ModInitializer {

    public static final String MODID = "jmws";
    public static final String VERSION = "1.1.1-1.21.5-beta.2";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    private int _deleteObjectOnClient(ServerPlayerEntity player, JMWSIOInterface.FetchType fetchType)
    {
        JMWSActionPayload refreshPayload = new JMWSActionPayload(
                JsonStaticHelper.makeWaypointSyncRequestJson(false)
        );
        JMWSActionPayload deleteAllClientsidePayload = new JMWSActionPayload(
                JsonStaticHelper.makeDeleteClientObjectRequestJson("*", fetchType) // * = Delete all
        );

        ServerPlayNetworking.send(player, deleteAllClientsidePayload);
        ServerPlayNetworking.send(player, refreshPayload);

        return 1;
    }

    @Override
    public void onInitialize() {
        FabricLoader fabricLoader = FabricLoader.getInstance();
        boolean isJMLoaded = fabricLoader.isModLoaded("journeymap");

        // Check if JourneyMap is installed, and what version
        if (fabricLoader.getEnvironmentType() == EnvType.CLIENT) {
            if (isJMLoaded) {
                Optional<ModContainer> jmModContainer = fabricLoader.getModContainer("journeymap");
                String versionString = jmModContainer.get().getMetadata().getVersion().getFriendlyString();
                if (!(Integer.valueOf(versionString.substring(versionString.length() - 2)) >= 47)) {
                    throw new RuntimeException("JourneyMap is installed (version %s) but it is the wrong version. Need 1.21.5-6.0.0-beta.47".formatted(versionString)); // using translatable string because this could be a common error
                }
                JMWS.LOGGER.info("Good to go. JMWS Version %s with JourneyMap Version %s on client-side.".formatted(JMWS.VERSION, versionString));
            } else {
                throw new RuntimeException("JourneyMap Version 6.0.0 Beta 47 or higher (1.21.5+) is required on the client-side of JMWS.");
            }

        }

        // Packet registering (client)
        PayloadTypeRegistry.playC2S().register(JMWSActionPayload.ID, JMWSActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HandshakePayload.ID, HandshakePayload.CODEC);

        // Packet registering (server)
        PayloadTypeRegistry.playS2C().register(JMWSActionPayload.ID, JMWSActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakePayload.ID, HandshakePayload.CODEC);

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("jmws")
                        .then(literal("sync").executes(
                                context -> {
                                    if (context.getSource().getPlayer() != null) {
                                        String jsonString = JsonStaticHelper.makeServerSyncRequestJson();
                                        JMWSActionPayload waypointActionPayload = new JMWSActionPayload(jsonString);

                                        ServerPlayNetworking.send(context.getSource().getPlayer(), waypointActionPayload);
                                    }
                                    return 1;

                                }))
                        .then(literal("getSyncInterval").executes(intervalContext -> {
                            if (intervalContext.getSource().getPlayer() != null) {
                                JMWSActionPayload payload = new JMWSActionPayload(JsonStaticHelper.makeDisplayIntervalRequestJson());
                                ServerPlayNetworking.send(intervalContext.getSource().getPlayer(), payload);
                            }
                            return 1;
                        }))
                        .then(literal("clearAll")
                                .then(literal("groups").executes(groupClearAllCtx -> {
                                    ServerPlayerEntity player = groupClearAllCtx.getSource().getPlayer();
                                    JMWSIOInterface.deleteAllUserObjects(player.getUuid(), JMWSIOInterface.FetchType.GROUP);
                                    return _deleteObjectOnClient(player, JMWSIOInterface.FetchType.GROUP);
                                }))
                                .then(literal("waypoints").executes(waypointClearAllCtx -> {
                                    ServerPlayerEntity player = waypointClearAllCtx.getSource().getPlayer();
                                    JMWSIOInterface.deleteAllUserObjects(player.getUuid(), JMWSIOInterface.FetchType.WAYPOINT);
                                    return _deleteObjectOnClient(player, JMWSIOInterface.FetchType.WAYPOINT);
                                })))
                        .then(literal("nextSync").executes(updateDisplayContext -> {
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
