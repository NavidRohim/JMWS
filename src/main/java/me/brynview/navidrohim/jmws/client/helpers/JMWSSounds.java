package me.brynview.navidrohim.jmws.client.helpers;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

// Sounds are only used client side so I do not register them
public interface JMWSSounds {
    SoundEvent ACTION_SUCCEED = SoundEvent.of(Identifier.of("jmws:waypoint_sync"));
    SoundEvent ACTION_FAILURE = SoundEvent.of(Identifier.of("jmws:generic_error"));
}
