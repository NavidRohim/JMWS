package me.brynview.navidrohim.jmws;

import me.brynview.navidrohim.jmws.client.exceptions.Whoopsies;
import me.brynview.navidrohim.jmws.common.CommonClass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jmws implements ModInitializer {

    private void _handleMissingMod(@Nullable Exception exc) {
        Constants.getLogger().error("Got error checking JM version; %s".formatted(exc));
        Constants.getLogger().error("JourneyMap might be installed, but the version cannot be detected. Need JourneyMap version %s or higher.".formatted(Constants.JourneyMapVersionString));
        CommonClass.clientHasJM = false;
    }

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        FabricLoader fabricLoader = FabricLoader.getInstance();
        boolean isJMLoaded = fabricLoader.isModLoaded("journeymap");

        // Check if JourneyMap is installed, and what version (I hate this solution by the way, will change eventually(
        try {

            if (fabricLoader.getEnvironmentType() == EnvType.CLIENT) {
                if (isJMLoaded) {
                    Optional<ModContainer> jmModContainer = fabricLoader.getModContainer("journeymap");
                    if (jmModContainer.isPresent()) {
                        String versionString = jmModContainer.get().getMetadata().getVersion().getFriendlyString();

                        SemanticVersion minAllowedVersion = SemanticVersion.parse(Constants.JourneyMapVersionString);
                        SemanticVersion betaVersion = SemanticVersion.parse(versionString);

                        int mcVersionMinor = betaVersion.getVersionComponent(1);
                        int mcVersionPatch = betaVersion.getVersionComponent(2);

                        int minMcVersionMinor = minAllowedVersion.getVersionComponent(1);
                        int minMcVersionPatch = minAllowedVersion.getVersionComponent(2);

                        Matcher regexBetaVersionPatternMinMatcher = Pattern.compile("beta\\.([0-9]+)").matcher(minAllowedVersion.toString());
                        Matcher regexBetaVersionPatternJarMatcher = Pattern.compile("beta\\.([0-9]+)").matcher(betaVersion.toString());

                        regexBetaVersionPatternMinMatcher.find();
                        regexBetaVersionPatternJarMatcher.find();

                        int jarVersionString = Integer.parseInt(regexBetaVersionPatternJarMatcher.group(1));
                        int minVersionString = Integer.parseInt(regexBetaVersionPatternMinMatcher.group(1));

                        if (!(mcVersionMinor >= minMcVersionMinor && mcVersionPatch == minMcVersionPatch && jarVersionString >= minVersionString)) {
                            Constants.getLogger().error("JourneyMap is installed (version %s) but it is the wrong version. Need %s or higher but will continue with loading anyway.".formatted(versionString, Constants.JourneyMapVersionString));
                            CommonClass.clientHasJM = false;
                        }
                        else {
                            Constants.getLogger().info("Good to go. JMWS Version %s with JourneyMap Version %s on client-side.".formatted(Constants.VERSION, versionString));
                            CommonClass.clientHasJM = true;
                        }
                    } else {
                        _handleMissingMod(null);
                    }
                } else {
                    Constants.getLogger().error("JourneyMap %s is required on the client-side of JMWS. Will continue with loading anyway.".formatted(Constants.JourneyMapVersionString));
                    CommonClass.clientHasJM = false;
                }

            } else {
                Constants.getLogger().info("JourneyMap is not needed on the server-side. If you get a warning about it on the server, you can safely ignore it.");
            }
        } catch (NoSuchElementException | VersionParsingException | IllegalStateException exception) {
            _handleMissingMod(exception);
        }

        CommonClass.init();
    }
}
