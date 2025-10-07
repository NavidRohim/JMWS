package me.brynview.navidrohim.jmws.client.config;

import journeymap.api.v2.client.option.*;

public class ConfigInterface {

    public OptionCategory category = new OptionCategory(
            "jmapi",
            "text.config.jmws-config.section.upload",
            "text.config.jmws-config.section.upload.tooltip"
    );

    public OptionCategory personalisation = new OptionCategory(
            "jmapi",
            "text.config.jmws-config.section.personalisation",
            "text.config.jmws-config.section.personalisation.tooltip"
    );

    public OptionCategory technical = new OptionCategory(
            "jmapi",
            "text.config.jmws-config.section.generalConfig",
            "text.config.jmws-config.section.generalConfig.tooltip"
    );

    public final BooleanOption enabled;
    public final BooleanOption uploadWaypoints;
    public final BooleanOption uploadGroups;
    public final BooleanOption autoSync;
    public final BooleanOption lookAltTeleporting;

    public final BooleanOption showAlerts;
    public final BooleanOption playEffects;
    public final BooleanOption colouredText;

    public final IntegerOption updateWaypointFrequency;


    public ConfigInterface() {
        this.enabled = new BooleanOption(category, "master", "text.config.jmws-config.option.enabled", true, true);
        this.uploadWaypoints = new BooleanOption(category, "uploadWaypoints", "text.config.jmws-config.option.uploadWaypoints", true);
        this.uploadGroups = new BooleanOption(category, "uploadGroups", "text.config.jmws-config.option.uploadGroups", true);
        this.autoSync = new BooleanOption(category, "autoSync", "text.config.jmws-config.option.autoSync", true);
        this.lookAltTeleporting = new BooleanOption(category, "lookAltTeleporting", "text.config.jmws-config.option.lookAltTeleporting", true);

        this.showAlerts = new BooleanOption(personalisation, "showAlerts", "text.config.jmws-config.option.showAlerts", true);
        this.playEffects = new BooleanOption(personalisation, "playEffects", "text.config.jmws-config.option.playEffects", true);
        this.colouredText = new BooleanOption(personalisation, "colouredText", "text.config.jmws-config.option.colouredText", true);

        this.updateWaypointFrequency = new IntegerOption(technical, "updateWaypointFrequency", "text.config.jmws-config.option.clientConfiguration.updateWaypointFrequency", 40, 2, 120);
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
