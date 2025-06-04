package me.brynview.navidrohim.jmws.server.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@Config(name = "jmws-config", wrapperName = "JMWSServerConfig", defaultHook = true)
public class JMWSServerConfigModel {

    // Server side configuration

    @Nest
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public ServerSideConfiguration serverConfiguration = new ServerSideConfiguration();

    public static class ServerSideConfiguration {
        @RangeConstraint(min=0, max=2000000)
        public int serverPacketLimit = 2000000;

        public boolean serverEnabled = true;
        public boolean serverWaypointsEnabled = true;
        public boolean serverGroupsEnabled = true;
    }

}