package me.brynview.navidrohim.jmws.platform;

import me.brynview.navidrohim.jmws.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public String side() {
        String side = FMLEnvironment.dist.toString();
        if (side.equalsIgnoreCase("SERVER") || side.equalsIgnoreCase("DEDICATED_SERVER"))
        {
            return "SERVER";
        }
        return "CLIENT";
    }
}
