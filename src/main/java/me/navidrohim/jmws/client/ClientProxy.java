package me.navidrohim.jmws.client;

import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.CommonProxy;
import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.client.command.ClientCommandBase;
import me.navidrohim.jmws.client.events.ForgeEventHandler;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        ClientCommandHandler.instance.registerCommand(new ClientCommandBase());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        Constants.LOGGER.info("postInit on client");
        if (Loader.isModLoaded("journeymap"))
        {
            CommonClass.hasJourneyMap = true;
            if (Loader.isModLoaded("mixinbooter")) {
                CommonClass.hasMixinBooter = true;

                Constants.LOGGER.info("registering events");
                MinecraftForge.EVENT_BUS.register(ForgeEventHandler.class);
                CommonClass.setupMinecraftClientInstance();
            } else {

                Constants.LOGGER.error("MixinBooter mod is not present. Disabled JMWS.");
            }
        } else {
            Constants.LOGGER.error("JourneyMap is not present. Disabled JMWS.");
        }
    }

}