package me.navidrohim.jmws.client.mixin;

import journeymap.client.model.Waypoint;
import journeymap.client.ui.waypoint.WaypointManagerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = WaypointManagerItem.class, remap = false)
public interface WaypointManagerItemMixin {
    @Accessor("waypoint") Waypoint getWaypoint();
}
