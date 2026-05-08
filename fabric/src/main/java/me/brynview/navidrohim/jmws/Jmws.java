package me.brynview.navidrohim.jmws;


import me.brynview.navidrohim.jmws.client.ClientCommonClass;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.server.commands.ServerDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jmws implements ModInitializer {

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        FabricLoader fabricLoader = FabricLoader.getInstance();
        boolean isJMLoaded = fabricLoader.isModLoaded("journeymap");

        // Check if JourneyMap is installed, and what version (I hate this solution by the way, will change eventually)
        // This has to be here because if it is not the client will crash when connecting to a server when JM is not installed.
        // Why cant I just specify JM needs to be installed in fabric.mod.json? Well because fabric is lacking a feature to specify if a dependency is on
        // client, server or both sides. (on server, only commonnetworking is needed. On the client, CommonNetworking and JourneyMap is required but CommonNetworking
        // is bundled with JM)
        // TLDR; Version checking is required because server has to have CommonNetworking and client doesn't implicitly need it but JMWS needs JourneyMap.

        try {

            if (fabricLoader.getEnvironmentType() == EnvType.CLIENT) {
                if (isJMLoaded) {
                    Optional<ModContainer> jmModContainer = fabricLoader.getModContainer("journeymap");
                    if (jmModContainer.isPresent()) {
                        String versionString = jmModContainer.get().getMetadata().getVersion().getFriendlyString();

                        SemanticVersion minAllowedVersion = SemanticVersion.parse(Constants.JourneyMapVersionString);
                        SemanticVersion betaVersion = SemanticVersion.parse(versionString);

                        int mcVersionMinor = betaVersion.getVersionComponent(0);
                        int mcVersionPatch = betaVersion.getVersionComponent(1);

                        int minMcVersionMinor = minAllowedVersion.getVersionComponent(0);
                        int minMcVersionPatch = minAllowedVersion.getVersionComponent(1);

                        Matcher regexBetaVersionPatternMinMatcher = Pattern.compile("beta\\.([0-9]+)").matcher(minAllowedVersion.toString());
                        Matcher regexBetaVersionPatternJarMatcher = Pattern.compile("beta\\.([0-9]+)").matcher(betaVersion.toString());

                        regexBetaVersionPatternMinMatcher.find();
                        regexBetaVersionPatternJarMatcher.find();

                        int jarVersionString = Integer.parseInt(regexBetaVersionPatternJarMatcher.group(1));
                        int minVersionString = Integer.parseInt(regexBetaVersionPatternMinMatcher.group(1));

                        ClientCommonClass.clientJMVersion = versionString;
                        Constants.updateShouldMakeLocalFromJourneyMapVersion(versionString);
                        if ((mcVersionMinor == minMcVersionMinor && mcVersionPatch >= minMcVersionPatch && jarVersionString >= minVersionString)) {
                            Constants.getLogger().info("Good to go. JMWS Version %s with JourneyMap Version %s on client-side.".formatted(Constants.VERSION, versionString));
                            ClientCommonClass.clientHasJM = true;
                            CommonClass.init();
                        }
                    }
                }

            } else {
                if (isJMLoaded) {
                    Optional<ModContainer> jmModContainer = fabricLoader.getModContainer("journeymap");
                    if (jmModContainer.isPresent()) {
                        Constants.updateServerJourneyMapStatus(jmModContainer.get().getMetadata().getVersion().getFriendlyString());
                    }
                } else {
                    Constants.logServerJourneyMapMissing();
                }
                Constants.getLogger().info("JourneyMap is optional on the server. If you get a warning about it, you can safely ignore it.");
                CommonClass.init();
            }
        } catch (NoSuchElementException | VersionParsingException | IllegalStateException ignored) {

        }

        CommandRegistrationCallback.EVENT.register((dispatcher, context, commandSelection) -> {
            ServerDispatcher.addCommandsToDispatcher(dispatcher);
        });
    };
}
