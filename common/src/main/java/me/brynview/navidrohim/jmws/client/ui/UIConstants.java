package me.brynview.navidrohim.jmws.client.ui;

import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class UIConstants
{
    public static final int NAMED_BUTTON_WIDTH = 50;
    public static final int ICON_BUTTON_WIDTH_HEIGHT = 15;

    public static final int ALERT_DURATION_MS = 4000;
    public static final int ALERT_FADE_DURATION_MS = 800;
    public static final int PLAYER_HEAD_SIZE = 32;
    public static final int PLAYER_HEAD_SIZE_HALVED = PLAYER_HEAD_SIZE / 2;


    public static final Tooltip SELECT_ALL = Tooltip.create(Component.translatable("jmws.ui.generic.select_all"));
    public static final Tooltip DESELECT_ALL = Tooltip.create(Component.translatable("jmws.ui.generic.deselect_all"));
    public static final Component EMPTY_TEXT = Component.translatable("jmws.ui.generic.empty");
}
