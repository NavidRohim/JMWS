package me.brynview.navidrohim.jmws.client.config;

import journeymap.api.v2.client.option.*;
import me.brynview.navidrohim.jmws.Constants;
import net.minecraft.world.phys.shapes.BooleanOp;

public class ConfigInterface {

    // Category for general sync settings
    private OptionCategory category = new OptionCategory(
            Constants.MODID,
            "text.config.jmws-config.section.upload",
            "text.config.jmws-config.section.upload.tooltip"
    );

    // Category for personalisation settings
    private OptionCategory personalisation = new OptionCategory(
            Constants.MODID,
            "text.config.jmws-config.section.personalisation",
            "text.config.jmws-config.section.personalisation.tooltip"
    );

    // Category for technical settings, usually best if left alone
    private OptionCategory technical = new OptionCategory(
            Constants.MODID,
            "text.config.jmws-config.section.general",
            "text.config.jmws-config.section.general.tooltip"
    );

    // Category for server settings, for display only and cannot be changed
    private OptionCategory server = new OptionCategory(
            Constants.MODID,
            "text.config.jmws-config.section.server",
            "text.config.jmws-config.section.server.tooltip"
    );

    private OptionCategory sharing = new OptionCategory(
            Constants.MODID,
            "text.config.jmws-config.section.sharing",
            "text.config.jmws-config.section.sharing.tooltip"
    );

    // All client side options

    public final BooleanOption enabled; // If JMWS is enabled
    public final BooleanOption uploadWaypoints; // If to sync waypoints
    public final BooleanOption uploadGroups; // If to sync groups
    public final BooleanOption autoSync; // If to auto-sync, can be disabled with little effect.

    public final BooleanOption showAlerts; // If to show chat / action bar alerts
    public final BooleanOption playEffects; // If to play sound effects, usually alongside text alerts
    public final BooleanOption colouredText; // If text alerts should be coloured, usually by importance

    public final IntegerOption updateWaypointFrequency; // How often auto-sync should sync

    // Server side display options

    public final BooleanOption serverEnabled; // If the server has JMWS enabled
    public final BooleanOption serverUploadWaypoints; // If the server syncs waypoints
    public final BooleanOption serverUploadGroups; // if the server syncs groups
    public final BooleanOption serverAllowsSharing;

    public final BooleanOption enableSharing;
    public final BooleanOption showSharingLabels;
    public final BooleanOption showGlobalLabels;

    public ConfigInterface() {

        // These follow the same order as the uninitialised definitions
        this.enabled = new BooleanOption(category, "master", "text.config.jmws-config.option.enabled", true, true);
        this.uploadWaypoints = new BooleanOption(category, "uploadWaypoints", "text.config.jmws-config.option.uploadWaypoints", true);
        this.uploadGroups = new BooleanOption(category, "uploadGroups", "text.config.jmws-config.option.uploadGroups", true);
        this.autoSync = new BooleanOption(category, "autoSync", "text.config.jmws-config.option.autoSync", true);

        this.showAlerts = new BooleanOption(personalisation, "showAlerts", "text.config.jmws-config.option.showAlerts", true);
        this.playEffects = new BooleanOption(personalisation, "playEffects", "text.config.jmws-config.option.playEffects", true);
        this.colouredText = new BooleanOption(personalisation, "colouredText", "text.config.jmws-config.option.colouredText", true);

        this.updateWaypointFrequency = new IntegerOption(technical, "updateWaypointFrequency", "text.config.jmws-config.option.clientConfiguration.updateWaypointFrequency", 40, 2, 120);

        this.serverEnabled = new BooleanOption(server, "serverEnabled", "text.config.jmws-config.option.serverEnabled", false);
        this.serverUploadWaypoints = new BooleanOption(server, "serverUploadWaypoints", "text.config.jmws-config.option.serverUploadWaypoints", false);
        this.serverUploadGroups = new BooleanOption(server, "serverUploadGroups", "text.config.jmws-config.option.serverUploadGroups", false);
        this.serverAllowsSharing = new BooleanOption(server, "serverAllowsSharing", "text.config.jmws-config.option.serverAllowsSharing", false);

        this.enableSharing = new BooleanOption(sharing, "enableSharing", "text.config.jmws-config.option.enableSharing", true);
        this.showSharingLabels = new BooleanOption(sharing, "showSharingLabels", "text.config.jmws-config.option.showSharingLabels", true);
        this.showGlobalLabels = new BooleanOption(sharing, "showGlobalLabels", "text.config.jmws-config.option.showGlobalLabels", true);
    }

    /**
     * How often auto-sync syncs with server in seconds.
     * @return int -- How often auto-sync syncs with server in seconds.
     */
    public int getUpdateWaypointFrequencyAsTicks()
    {
        return updateWaypointFrequency.get() * 20;
    }

    /**
     * If the user has JMWS enabled in any capacity.
     * @return boolean -- If the user has JMWS enabled in any capacity (at least must have either waypoint or group syncing on to return `true`)
     */
    public boolean clientEnabled()
    {
        return (this.enabled.get() && (this.uploadWaypoints.get() || this.uploadGroups.get()));
    }

    /**
     * If waypoints should be synced.
     * @return boolean -- If waypoints should be synced.
     */
    public boolean waypointsEnabled()
    {
        return (this.enabled.get() && this.uploadWaypoints.get());
    }

    /**
     * If groups should be synced.
     * @return boolean -- If groups should be synced.
     */
    public boolean groupsEnabled()
    {
        return (this.enabled.get() && this.uploadGroups.get());
    }


}
