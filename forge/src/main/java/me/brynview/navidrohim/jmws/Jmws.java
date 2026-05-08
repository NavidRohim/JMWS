package me.brynview.navidrohim.jmws;

import me.brynview.navidrohim.jmws.common.CommonClass;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;


@Mod(Constants.MODID)
public class Jmws {

    public Jmws() {
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        //Constants.LOGGER.info("Hello Forge world!");
        boolean isServerSide = FMLEnvironment.dist.toString().equalsIgnoreCase("SERVER")
                || FMLEnvironment.dist.toString().equalsIgnoreCase("DEDICATED_SERVER");
        ModList.getModContainerById("journeymap").ifPresentOrElse(modContainer -> {
            String journeyMapVersion = modContainer.getModInfo().getVersion().toString();
            Constants.updateShouldMakeLocalFromJourneyMapVersion(journeyMapVersion);
            if (isServerSide) {
                Constants.updateServerJourneyMapStatus(journeyMapVersion);
            }
        }, () -> {
            if (isServerSide) {
                Constants.logServerJourneyMapMissing();
            }
        });
        CommonClass.init();

    }
}
