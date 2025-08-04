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
        return FMLEnvironment.dist.name();
    }

    @Override
    public int getSyncInTicks() {
        return 0;
    }

    @Override
    public int timeUntilNextSyncInTicks() {
        return 0;
    }

    @Override
    public boolean serverHasMod() {
        return false;
    }

    @Override
    public void setServerModStatus(boolean serverModStatus) {

    }
}
