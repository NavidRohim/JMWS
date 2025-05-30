package me.brynview.navidrohim.jmws;
import me.brynview.navidrohim.jmws.common.JMWSConstants;
import me.brynview.navidrohim.jmws.common.payloads.HandshakePayload;
import me.brynview.navidrohim.jmws.common.payloads.JMWSActionPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;

public class JMWS implements ModInitializer {

    public static final String MODID = "jmws";
    public static final String VERSION = "1.1.1-1.21.1-RC";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    public static void info(Object message) {
        LOGGER.info("[JMWS %s] %s".formatted(FabricLoader.getInstance().getEnvironmentType(), message));
    }

    private void _handleMissingMod(@Nullable Exception exc) {
        JMWS.LOGGER.error("Got error checking JM version; %s".formatted(exc));
        throw new RuntimeException("JourneyMap might be installed, but the version cannot be detected. Need JourneyMap version %s or higher.".formatted(JMWSConstants.JourneyMapVersionString));
    }

    @Override
    public void onInitialize() {
        FabricLoader fabricLoader = FabricLoader.getInstance();
        boolean isJMLoaded = fabricLoader.isModLoaded("journeymap");

        // Check if JourneyMap is installed, and what version
        try {
            if (fabricLoader.getEnvironmentType() == EnvType.CLIENT) {
                if (isJMLoaded) {
                    Optional<ModContainer> jmModContainer = fabricLoader.getModContainer("journeymap");
                    if (jmModContainer.isPresent()) {
                        String versionString = jmModContainer.get().getMetadata().getVersion().getFriendlyString();

                        SemanticVersion minAllowedVersion = SemanticVersion.parse(JMWSConstants.JourneyMapVersionString);
                        SemanticVersion betaVersion = SemanticVersion.parse(versionString);

                        // JM version from jar
                        int mcVersionMinor = betaVersion.getVersionComponent(1);
                        int mcVersionPatch = betaVersion.getVersionComponent(2);
                        int jmVersionBetaPatch = betaVersion.getVersionComponent(6);

                        int minMcVersionMinor = minAllowedVersion.getVersionComponent(1);
                        int minMcVersionPatch = minAllowedVersion.getVersionComponent(2);
                        int minMcVersionBeta = minAllowedVersion.getVersionComponent(6);

                        if (!(mcVersionMinor >= minMcVersionMinor && mcVersionPatch == minMcVersionPatch && jmVersionBetaPatch >= minMcVersionBeta)) {
                            throw new RuntimeException("JourneyMap is installed (version %s) but it is the wrong version. Need %s or higher".formatted(versionString, JMWSConstants.JourneyMapVersionString)); // using translatable string because this could be a common error
                        }
                        JMWS.LOGGER.info("Good to go. JMWS Version %s with JourneyMap Version %s on client-side.".formatted(JMWS.VERSION, versionString));
                    } else {
                        _handleMissingMod(null);
                    }
                } else {
                    throw new RuntimeException("JourneyMap %s is required on the client-side of JMWS.".formatted(JMWSConstants.JourneyMapVersionString));
                }

            }
        } catch (NoSuchElementException | VersionParsingException exception) {
            _handleMissingMod(exception);
        }

        // Packet registering (client)
        PayloadTypeRegistry.playC2S().register(JMWSActionPayload.ID, JMWSActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HandshakePayload.ID, HandshakePayload.CODEC);

        // Packet registering (server)
        PayloadTypeRegistry.playS2C().register(JMWSActionPayload.ID, JMWSActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakePayload.ID, HandshakePayload.CODEC);
    }
}
