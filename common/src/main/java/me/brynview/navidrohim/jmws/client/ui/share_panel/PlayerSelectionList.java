package me.brynview.navidrohim.jmws.client.ui.share_panel;

import me.brynview.navidrohim.jmws.client.ui.generic.entry.PlayerHeadEntryWithTitle;
import me.brynview.navidrohim.jmws.client.ui.generic.selection_list.CheckableSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class PlayerSelectionList extends CheckableSelectionList<PlayerSelectionList.PlayerEntry> {

    private static final Component EMPTY_STATE_TEXT = Component.translatable("jmws.ui.stop_sharing.empty");

    public PlayerSelectionList(Minecraft minecraft, int width, int height, int x, int y, int itemHeight) {
        super(minecraft, width, height, x, y, itemHeight);
    }

    @Override
    public Component getEmptyStateText()
    {
        return EMPTY_STATE_TEXT;
    }

    public static class PlayerEntry extends PlayerHeadEntryWithTitle<PlayerEntry> {
        public PlayerEntry(@NotNull CheckableSelectionList<PlayerEntry> listOwner, PlayerInfo player) {
            super(listOwner, player);
        }

        @Override
        public boolean canSelect() {
            return true;
        }
    }
}
