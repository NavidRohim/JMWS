package me.navidrohim.jmws.client.mixin;

import journeymap.client.model.Waypoint;
import journeymap.client.render.ingame.RenderWaypointBeacon;
import journeymap.client.waypoint.WaypointStore;
import me.navidrohim.jmws.client.mixinhelper.MixinManager;
import me.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.navidrohim.jmws.common.CommonClass;
import me.navidrohim.jmws.common.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderWaypointBeacon.class)
public abstract class RenderWaypointBeaconMixin {

    @Redirect(
            // the method this function is called in
            method = "doRender",
            // target the invocation of System.out.println
            at = @At(
                    value = "INVOKE",
                    target = "Ljourneymap/client/waypoint/WaypointStore;remove(Ljourneymap/client/model/Waypoint;)V"
            ),
            remap = false
    )
    private static void remove(WaypointStore instance, Waypoint waypoint)
    {
        if (CommonClass.getEnabledStatus() && !MixinManager.didRemoveDeathpointRecently) {
            Constants.LOGGER.info("testVicinity");
            MixinManager.didRemoveDeathpointRecently = true;

            JMWSPlugin.getInstance().deleteAction(waypoint, false);
            JMWSPlugin.updateWaypoints(false);
        }
    }
}
