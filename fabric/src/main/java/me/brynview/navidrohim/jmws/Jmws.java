package me.brynview.navidrohim.jmws;


import commonnetwork.api.Network;
import me.brynview.navidrohim.jmws.client.config.JMWSConfig;
import me.brynview.navidrohim.jmws.payloads.JMWSActionPayload;
import net.fabricmc.api.ModInitializer;

public class Jmws implements ModInitializer {


    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.

        CommonClass.init();
    }
}
