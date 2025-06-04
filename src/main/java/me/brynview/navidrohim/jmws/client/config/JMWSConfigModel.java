package me.brynview.navidrohim.jmws.client.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;
import me.brynview.navidrohim.jmws.JMWS;
import org.spongepowered.asm.mixin.injection.Next;

@Modmenu(modId = JMWS.MODID)
@Config(name = "jmws-config", wrapperName = "JMWSConfig", defaultHook = true)
public class JMWSConfigModel {

    // Client side configuration

    @SectionHeader("upload")
    public boolean enabled = true;
    public boolean uploadWaypoints = true;
    public boolean uploadGroups = true;

    @SectionHeader("personalisation")
    public boolean showAlerts = true;
    public boolean playEffects = false;
    public boolean colouredText = true;

    @SectionHeader("generalConfig")

    @Nest
    public ClientConfiguration clientConfiguration = new ClientConfiguration();

    public static class ClientConfiguration {
        public int updateWaypointFrequency = 800;
        public int serverHandshakeTimeout = 5;
    }

    @Nest
    @Sync(Option.SyncMode.INFORM_SERVER)
    public ServerConfiguration serverConfiguration = new ServerConfiguration();

    public static class ServerConfiguration {
        @RangeConstraint(min=0, max=2000000)
        public int serverPacketLimit = 2000000;

        public boolean serverEnabled = true;
        public boolean serverWaypointsEnabled = true;
        public boolean serverGroupsEnabled = true;
    }
}