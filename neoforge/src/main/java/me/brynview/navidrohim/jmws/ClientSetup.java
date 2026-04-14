package me.brynview.navidrohim.jmws;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void setupClientMinecraft(FMLLoadCompleteEvent event)
    {
        JMWSClientCommon.setupMinecraftClientInstance();
    }
}
