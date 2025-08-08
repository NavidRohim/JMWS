package me.navidrohim.jmws.client;

import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.CommonProxy;
import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.ForgeEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.collection.immutable.Stream;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        Constants.LOGGER.info("postInit on client");
        if (Loader.isModLoaded("journeymap"))
        {
            Constants.LOGGER.info("registering events");
            MinecraftForge.EVENT_BUS.register(ForgeEventHandler.class);
            CommonClass.setupMinecraftClientInstance();
        }
    }

}