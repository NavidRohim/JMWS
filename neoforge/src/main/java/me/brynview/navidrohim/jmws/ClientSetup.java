package me.brynview.navidrohim.jmws;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.Minecart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void setupClientMinecraft(FMLLoadCompleteEvent event)
    {
        CommonClass.setupMinecraftClientInstance();
    }
}
