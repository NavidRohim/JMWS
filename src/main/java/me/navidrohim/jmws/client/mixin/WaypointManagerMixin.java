package me.navidrohim.jmws.client.mixin;

import journeymap.client.model.Waypoint;
import journeymap.client.ui.waypoint.WaypointManager;
import journeymap.client.ui.waypoint.WaypointManagerItem;
import me.navidrohim.jmws.CommonClass;
import me.navidrohim.jmws.client.plugin.JMWSPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WaypointManager.class)
public abstract class WaypointManagerMixin {

    @Inject(method = "removeWaypoint(Ljourneymap/client/ui/waypoint/WaypointManagerItem;)V", at = @At("HEAD"), remap = false)
    private void rmWaypointFromDialogue(WaypointManagerItem waypointManagerItem, CallbackInfo callbackInfo)
    {
        if (CommonClass.getEnabledStatus()) {
            Waypoint wp = ((WaypointManagerItemMixin) waypointManagerItem).getWaypoint();
            JMWSPlugin.getInstance().deleteAction(wp, false);
        }
    }
}
