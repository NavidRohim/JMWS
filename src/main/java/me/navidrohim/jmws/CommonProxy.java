package me.navidrohim.jmws;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public abstract class CommonProxy {
    public abstract void preInit(FMLPreInitializationEvent event);

    public abstract void init(FMLInitializationEvent event);

    public abstract void postInit(FMLPostInitializationEvent event);
}
