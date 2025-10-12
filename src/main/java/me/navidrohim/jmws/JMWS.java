package me.navidrohim.jmws;

import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.common.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;


@Mod(modid = JMWS.MODID, name = JMWS.NAME, version = JMWS.VERSION, acceptableRemoteVersions = "*")
public class JMWS
{
    public static final String MODID = "jmws";
    public static final String NAME = "JourneyMap Waypoint Syncing";
    public static final String VERSION = "1.1.7-1.12.2-beta.2";

    public static Logger logger;

    @SidedProxy(clientSide = "me.navidrohim.jmws.client.ClientProxy", serverSide = "me.navidrohim.jmws.server.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        CommonClass.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }
}
