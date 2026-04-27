package me.brynview.navidrohim.jmws.client.ui;

import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

import static me.brynview.navidrohim.jmws.client.ui.UIConstants.PLAYER_HEAD_SIZE_HALVED;

public class RenderUtils
{
    public static void renderBorderForList(@NotNull GuiGraphicsExtractor graphics, LayoutElement layout)
    {
        renderBorderForList(graphics, layout, 0);
    }

    public static void renderBorderForList(@NotNull GuiGraphicsExtractor graphics, LayoutElement layout, int padding)
    {
        graphics.outline(layout.getX() - padding, layout.getY() - padding, layout.getWidth() + padding * 2, layout.getHeight() + padding * 2, 0xFF202020);
    }

    public static String shortenObjectName(String name, int maxLength)
    {
        return name.length() > maxLength ? name.substring(0, maxLength / 2) + " ... " +  name.substring(name.length() - 5): name;
    }

    public static void setDimensionsForList(ObjectSelectionList<?> list, int screenWidth, int screenHeight)
    {
        int panelWidth = (int) (screenWidth * 0.70);
        int panelHeight = (int) (screenHeight * 0.65);
        int panelX = screenWidth / 10;
        int panelY = (screenHeight - panelHeight) / 2;

        list.setX(panelX);
        list.setY(panelY);
        list.setWidth(panelWidth);
        list.setHeight(panelHeight);
    }

    public static Vector2i getPositionRelativeToList(ObjectSelectionList<?> list)
    {
        int y = list.getY() + list.getHeight() + 15;
        return new Vector2i(list.getX(), y);
    }

    public static List<PlayerInfo> getPlayers(Minecraft minecraft)
    {
        List<PlayerInfo> onlinePlayers = new ArrayList<>(minecraft.player.connection.getOnlinePlayers().stream().toList());
        onlinePlayers.removeIf(player -> player.getProfile().equals(minecraft.player.getGameProfile()));

        return onlinePlayers;
    }

    public static void renderStatusBarInEntry(GuiGraphicsExtractor renderer, ObjectSelectionList.Entry<?> entry, int colour)
    {
        // minus half-width
        int barWidth = 6;
        int middle = (entry.getContentX() + ((4 + PLAYER_HEAD_SIZE_HALVED) / 2));

        int startX = middle - (barWidth / 2);
        int endX = middle + (barWidth / 2);

        int startY = entry.getContentYMiddle() - PLAYER_HEAD_SIZE_HALVED;
        int endY = entry.getContentYMiddle() + PLAYER_HEAD_SIZE_HALVED;

        renderer.fill(startX, startY, endX, endY, colour);
    }

}
