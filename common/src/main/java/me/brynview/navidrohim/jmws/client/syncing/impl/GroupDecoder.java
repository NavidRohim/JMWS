package me.brynview.navidrohim.jmws.client.syncing.impl;

import journeymap.api.v2.common.waypoint.WaypointFactory;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import me.brynview.navidrohim.jmws.client.syncing.api.decoder.BaseDecoder;

public final class GroupDecoder implements BaseDecoder<ClientGroupWrapper, WaypointGroup>
{
    @Override
    public ClientGroupWrapper decodeStringToWrapper(String data) {
        WaypointGroup nativeObj = this.decodeStringToNative(data);
        return new ClientGroupWrapper(nativeObj, nativeObj.getModId());
    }

    @Override
    public WaypointGroup decodeStringToNative(String data) {
        return WaypointFactory.fromGroupJsonString(data);
    }
}
