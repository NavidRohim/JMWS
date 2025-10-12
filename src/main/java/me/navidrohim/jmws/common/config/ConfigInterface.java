package me.navidrohim.jmws.common.config;

import me.navidrohim.jmws.common.Constants;
import net.minecraftforge.common.config.Config;

@Config(modid = Constants.MODID, name = "JMWSConfig")
@Config.LangKey("text.config.jmws-config.title")
public class ConfigInterface {

    @Config.Comment("Whether JMWS is on or off. On server side, you can set this to false so clients' JMWS is disabled.")
    @Config.Name("JMWS Enabled")
    public static Boolean enabled = true;

    @Config.Comment("Whether to show alerts in the action bar. (client only)")
    @Config.Name("Show alerts")
    public static Boolean showAlerts = true;

    @Config.Comment("Whether to play sound alerts, usually accustomed by text alerts. (client only)")
    @Config.Name("Sound alerts")
    public static Boolean playEffects = true;

    @Config.Comment("Some alerts have colour to signify status (good, bad, etc) you can disable this. (client only)")
    @Config.Name("Show coloured text")
    public static Boolean colouredText = true;

    @Config.Comment("How often to sync waypoints (in seconds, 40 by default) (client only)")
    @Config.Name("Sync frequency")
    public static Integer updateWaypointFrequency = 40;

    @Config.Comment("If the client should sync waypoints with the server. Technically, this can be disabled and nothing would change.")
    @Config.Name("Auto-Sync")
    public static boolean autoSync = true;


    public int getUpdateWaypointFrequencyAsTicks()
    {
        return updateWaypointFrequency * 20;
    }
}
