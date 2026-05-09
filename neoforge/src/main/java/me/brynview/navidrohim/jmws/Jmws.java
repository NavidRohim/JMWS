package me.brynview.navidrohim.jmws;


import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.platform.Services;
import me.brynview.navidrohim.jmws.common.platform.services.IPlatformHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;


@Mod(Constants.MODID)
public class Jmws {

    public Jmws(IEventBus eventBus) {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.

        boolean isServerSide = Services.PLATFORM.side() == IPlatformHelper.Side.SERVER;

        ModList.get().getModContainerById("journeymap").ifPresentOrElse(modContainer -> {
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
