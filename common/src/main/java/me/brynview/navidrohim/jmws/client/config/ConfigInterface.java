package me.brynview.navidrohim.jmws.client.config;

import journeymap.api.v2.client.option.*;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.CommonClass;

public class ConfigInterface {

    private OptionCategory category = new OptionCategory(
            Constants.MODID,
            "text.config.jmws-config.section.upload",
            "text.config.jmws-config.section.upload.tooltip"
    );

    private OptionCategory personalisation = new OptionCategory(
            Constants.MODID,
            "text.config.jmws-config.section.personalisation",
            "text.config.jmws-config.section.personalisation.tooltip"
    );

    private OptionCategory technical = new OptionCategory(
            Constants.MODID,
            "text.config.jmws-config.section.generalConfig",
            "text.config.jmws-config.section.generalConfig.tooltip"
    );

    private OptionCategory server = new OptionCategory(
            Constants.MODID,
            "text.config.jmws-config.section.server",
            "text.config.jmws-config.section.server.tooltip"
    );

    public final BooleanOption enabled;
    public final BooleanOption uploadWaypoints;
    public final BooleanOption uploadGroups;
    public final BooleanOption autoSync;

    public final BooleanOption showAlerts;
    public final BooleanOption playEffects;
    public final BooleanOption colouredText;

    public final IntegerOption updateWaypointFrequency;

    public final BooleanOption serverEnabled;
    public final BooleanOption serverUploadWaypoints;
    public final BooleanOption serverUploadGroups;

    public ConfigInterface() {
        this.enabled = new BooleanOption(category, "master", "text.config.jmws-config.option.enabled", true, true);
        this.uploadWaypoints = new BooleanOption(category, "uploadWaypoints", "text.config.jmws-config.option.uploadWaypoints", true);
        this.uploadGroups = new BooleanOption(category, "uploadGroups", "text.config.jmws-config.option.uploadGroups", true);
        this.autoSync = new BooleanOption(category, "autoSync", "text.config.jmws-config.option.autoSync", true);

        this.showAlerts = new BooleanOption(personalisation, "showAlerts", "text.config.jmws-config.option.showAlerts", true);
        this.playEffects = new BooleanOption(personalisation, "playEffects", "text.config.jmws-config.option.playEffects", true);
        this.colouredText = new BooleanOption(personalisation, "colouredText", "text.config.jmws-config.option.colouredText", true);

        this.updateWaypointFrequency = new IntegerOption(technical, "updateWaypointFrequency", "text.config.jmws-config.option.clientConfiguration.updateWaypointFrequency", 40, 2, 120);

        this.serverEnabled = new BooleanOption(server, "serverEnabled", "text.config.jmws-config.option.serverEnabled", true);
        this.serverUploadWaypoints = new BooleanOption(server, "serverUploadWaypoints", "text.config.jmws-config.option.serverUploadWaypoints", true);
        this.serverUploadGroups = new BooleanOption(server, "serverUploadGroups", "text.config.jmws-config.option.serverUploadGroups", true);
    }

    public int getUpdateWaypointFrequencyAsTicks()
    {
        return updateWaypointFrequency.get() * 20;
    }

    public boolean clientEnabled()
    {
        return (this.enabled.get() && (this.uploadWaypoints.get() || this.uploadGroups.get()));
    }

    public boolean waypointsEnabled()
    {
        return (this.enabled.get() && this.uploadWaypoints.get());
    }

    public boolean groupsEnabled()
    {
        return (this.enabled.get() && this.uploadGroups.get());
    }


}
