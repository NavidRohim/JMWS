package me.brynview.navidrohim.jmws.client.helpers;



import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

// Sounds are only used client side so I do not register them
public interface JMWSSounds {
    SoundEvent ACTION_SUCCEED = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "waypoint_sync"));
    SoundEvent ACTION_FAILURE = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MODID, "generic_error"));

}
