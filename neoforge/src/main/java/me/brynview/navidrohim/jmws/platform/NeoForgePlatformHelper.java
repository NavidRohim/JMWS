package me.brynview.navidrohim.jmws.platform;

import me.brynview.navidrohim.jmws.common.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public String side() {
        String side = FMLEnvironment.getDist().toString();
        if (side.equalsIgnoreCase("SERVER") || side.equalsIgnoreCase("DEDICATED_SERVER"))
        {
            return "SERVER";
        }
        return "CLIENT";
    }
}
