package me.navidrohim.jmws.client.helpers;

import me.navidrohim.jmws.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

// Sounds are only used client side so I do not register them
public interface JMWSSounds {
    net.minecraft.util.SoundEvent ACTION_SUCCEED = new SoundEvent(new ResourceLocation(Constants.MODID, "waypoint_sync"));
    net.minecraft.util.SoundEvent ACTION_FAILURE = new SoundEvent(new ResourceLocation(Constants.MODID, "generic_error"));

}
