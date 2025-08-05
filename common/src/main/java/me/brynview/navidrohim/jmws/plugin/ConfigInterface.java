package me.brynview.navidrohim.jmws.plugin;

import journeymap.api.v2.client.event.RegistryEvent;
import journeymap.api.v2.client.option.*;
import journeymap.api.v2.common.event.impl.Event;
import journeymap.api.v2.common.event.impl.EventFactory;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.helper.CommonHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.shapes.BooleanOp;

public class ConfigInterface {

    private OptionCategory category = new OptionCategory(
            "jmapi",
            "text.config.jmws-config.section.upload",
            "text.config.jmws-config.section.upload.tooltip"
    );

    private OptionCategory personalisation = new OptionCategory(
            "jmapi",
            "text.config.jmws-config.section.personalisation",
            "text.config.jmws-config.section.personalisation.tooltip"
    );

    private OptionCategory technical = new OptionCategory(
            "jmapi",
            "text.config.jmws-config.section.generalConfig",
            "text.config.jmws-config.section.generalConfig.tooltip"
    );

    public final BooleanOption enabled;
    public final BooleanOption uploadWaypoints;
    public final BooleanOption uploadGroups;

    public final BooleanOption showAlerts;
    public final BooleanOption playEffects;
    public final BooleanOption colouredText;

    public final IntegerOption updateWaypointFrequency;
    public final IntegerOption serverHandshakeTimeout;

    public ConfigInterface() {
        this.enabled = new BooleanOption(category, "master", "text.config.jmws-config.option.enabled", true, true);
        this.uploadWaypoints = new BooleanOption(category, "uploadWaypoints", "text.config.jmws-config.option.uploadWaypoints", true);
        this.uploadGroups = new BooleanOption(category, "uploadGroups", "text.config.jmws-config.option.uploadGroups", true);

        this.showAlerts = new BooleanOption(personalisation, "showAlerts", "text.config.jmws-config.option.showAlerts", true);
        this.playEffects = new BooleanOption(personalisation, "playEffects", "text.config.jmws-config.option.playEffects", true);
        this.colouredText = new BooleanOption(personalisation, "colouredText", "text.config.jmws-config.option.colouredText", true);

        this.updateWaypointFrequency = new IntegerOption(technical, "updateWaypointFrequency", "text.config.jmws-config.option.clientConfiguration.updateWaypointFrequency", 800, 80, Integer.MAX_VALUE);
        this.serverHandshakeTimeout = new IntegerOption(technical, "serverHandshakeTimeout", "text.config.jmws-config.option.clientConfiguration.serverHandshakeTimeout", 5, 1, Integer.MAX_VALUE);
    }
}
