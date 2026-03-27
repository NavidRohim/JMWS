package me.brynview.navidrohim.jmws.client.assets;



import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * SoundEvents ready to be used. Sounds are only used client-side, so I do not register them
 */
public interface JMWSSounds {
    SoundEvent ACTION_SUCCEED = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Constants.MODID, "waypoint_sync"));
    SoundEvent ACTION_FAILURE = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Constants.MODID, "generic_error"));
}
