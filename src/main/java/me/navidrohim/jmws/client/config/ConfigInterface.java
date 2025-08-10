package me.navidrohim.jmws.client.config;

import me.navidrohim.jmws.Constants;
import me.navidrohim.jmws.helper.CommonHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Config;

@Config(modid = Constants.MODID, name = "JMWSConfig")
@Config.LangKey("text.config.jmws-config.title")
public class ConfigInterface {

    @Config.Comment("Whether JMWS is on or off.")
    @Config.Name("JMWS Enabled")
    public static Boolean enabled = true;

    @Config.Comment("Whether to show alerts in the action bar.")
    @Config.Name("Show alerts")
    public static Boolean showAlerts = true;

    @Config.Comment("Whether to play sound alerts, usually accustomed by text alerts.")
    @Config.Name("Sound alerts")
    public static Boolean playEffects = true;

    @Config.Comment("Some alerts have colour to signify status (good, bad, etc) you can disable this.")
    @Config.Name("Show coloured text")
    public static Boolean colouredText = true;

    @Config.Comment("How often to sync waypoints (in seconds, 40 by default")
    @Config.Name("Sync frequency")
    public static Integer updateWaypointFrequency = 40;

    @Config.Comment("How long until JMWS handshake timeout.")
    @Config.Name("Server handshake timeout")
    public static Integer serverHandshakeTimeout = 3;

    public int getUpdateWaypointFrequencyAsTicks()
    {
        return updateWaypointFrequency * 20;
    }
}
