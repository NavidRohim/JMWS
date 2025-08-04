package me.brynview.navidrohim.jmws;

import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MODID)
public class Jmws {

    public Jmws() {
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Constants.LOGGER.info("Hello Forge world!");
        CommonClass.init();

    }
}
