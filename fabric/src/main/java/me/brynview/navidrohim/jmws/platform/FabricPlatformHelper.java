package me.brynview.navidrohim.jmws.platform;

import me.brynview.navidrohim.jmws.CommonClass;
import me.brynview.navidrohim.jmws.client.JMWSClient;
import me.brynview.navidrohim.jmws.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String side() {return FabricLoader.getInstance().getEnvironmentType().toString();}

}
