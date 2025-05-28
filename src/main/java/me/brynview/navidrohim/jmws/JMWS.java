package me.brynview.navidrohim.jmws;
import me.brynview.navidrohim.jmws.common.payloads.HandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class JMWS implements ModInitializer {

    public static final String MODID = "jmws";
    public static final String VERSION = "1.1.1-1.21.5-beta.6";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    public static void info(Object message) {
        LOGGER.info("[JMWS %s] %s".formatted(FabricLoader.getInstance().getEnvironmentType(), message));
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
    }
}
