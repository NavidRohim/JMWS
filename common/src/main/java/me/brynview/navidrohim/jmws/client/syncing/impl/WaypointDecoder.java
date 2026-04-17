package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import me.brynview.navidrohim.jmws.client.syncing.api.decoder.BaseDecoder;

public final class WaypointDecoder implements BaseDecoder<ClientWaypointWrapper, Waypoint> {

    @Override
    public ClientWaypointWrapper decodeStringToWrapper(String data) {
        Waypoint nativeObj = this.decodeStringToNative(data);
        return new ClientWaypointWrapper(nativeObj, nativeObj.getModId());
    }

    @Override
    public Waypoint decodeStringToNative(String data) {
        return WaypointFactory.fromWaypointJsonString(data);
    }
}
