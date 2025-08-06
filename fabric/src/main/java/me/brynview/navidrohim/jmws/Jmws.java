package me.brynview.navidrohim.jmws;

import com.mojang.datafixers.kinds.Const;
import me.brynview.navidrohim.jmws.exceptions.Whoopsies;
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
        throw new Whoopsies("JourneyMap might be installed, but the version cannot be detected. Need JourneyMap version %s or higher.".formatted(Constants.JourneyMapVersionString));
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

                        Integer jarVersionString = Integer.valueOf(regexBetaVersionPatternJarMatcher.group(1));
                        Integer minVersionString = Integer.valueOf(regexBetaVersionPatternMinMatcher.group(1));

                        if (!(mcVersionMinor >= minMcVersionMinor && mcVersionPatch == minMcVersionPatch && jarVersionString >= minVersionString)) {
                            throw new Whoopsies("JourneyMap is installed (version %s) but it is the wrong version. Need %s or higher".formatted(versionString, Constants.JourneyMapVersionString)); // using translatable string because this could be a common error
                        }
                        Constants.getLogger().info("Good to go. JMWS Version %s with JourneyMap Version %s on client-side.".formatted(Constants.VERSION, versionString));
                    } else {
                        _handleMissingMod(null);
                    }
                } else {
                    throw new Whoopsies("JourneyMap %s is required on the client-side of JMWS.".formatted(Constants.JourneyMapVersionString));
                }

            }
        } catch (NoSuchElementException | VersionParsingException | IllegalStateException exception) {
            _handleMissingMod(exception);
        }

        CommonClass.init();
    }
}
