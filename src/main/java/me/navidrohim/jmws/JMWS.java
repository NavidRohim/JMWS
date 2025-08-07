package me.navidrohim.jmws;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = JMWS.MODID, name = JMWS.NAME, version = JMWS.VERSION)
public class JMWS
{
    public static final String MODID = "jmws";
    public static final String NAME = "JourneyMap Waypoint Syncing";
    public static final String VERSION = "1.1.4-1.12.2-alpha.1";

    public static Logger logger;

    @SidedProxy(clientSide = "navidrohim.jmws.client.ClientProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }
}
