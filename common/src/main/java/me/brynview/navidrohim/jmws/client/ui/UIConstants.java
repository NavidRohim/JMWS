package me.brynview.navidrohim.jmws.client.ui;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class UIConstants
{
    public static final int NAMED_BUTTON_WIDTH = 50;

    public static final int ALERT_DURATION_MS = 4000;
    public static final int ALERT_FADE_DURATION_MS = 800;
    public static final int PLAYER_HEAD_SIZE = 32;
    public static final int PLAYER_HEAD_SIZE_HALVED = PLAYER_HEAD_SIZE / 2;

    public static final int BORDER_COLOUR = 0xFF202020;

    public static final Component REFRESHING = Component.translatable("jmws.ui.generic.refreshing");
    public static final Component EMPTY_TEXT = Component.translatable("jmws.ui.generic.empty");

    public static final Tooltip SELECT_ALL = Tooltip.create(Component.translatable("jmws.ui.generic.select_all"));
    public static final Tooltip DESELECT_ALL = Tooltip.create(Component.translatable("jmws.ui.generic.deselect_all"));

    public static final Identifier REFRESH = Identifier.fromNamespaceAndPath(Constants.MODID, "refresh");
    public static final Tooltip REFRESH_TOOLTIP = Tooltip.create(Component.translatable("selectServer.refresh"));
}
