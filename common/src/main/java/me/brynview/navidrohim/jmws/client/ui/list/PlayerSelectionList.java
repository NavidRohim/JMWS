package me.brynview.navidrohim.jmws.client.ui.list;

import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.client.ui.generic.list.entry.PlayerHeadEntryWithTitle;
import me.brynview.navidrohim.jmws.client.ui.generic.list.CheckableSelectionList;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.HasScrollableList;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerSelectionList extends CheckableSelectionList<PlayerSelectionList.PlayerEntry> {

    private static final Component EMPTY_STATE_TEXT = Component.translatable("jmws.ui.stop_sharing.empty");

    private final List<PlayerInfo> players = new ArrayList<>();
    private final ClientObjectWrapper<?> object;
    private final HasScrollableList parentScreen;

    public PlayerSelectionList(Minecraft minecraft, int itemHeight, ClientObjectWrapper<?> object, HasScrollableList parentScreen) {
        super(minecraft, 0, 0, 0, 0, itemHeight);

        this.object = object;
        this.players.addAll(RenderUtils.getPlayers(minecraft));
        this.parentScreen = parentScreen;
    }

    @Override
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
    }

    public void addWidgets()
    {
        this.players.forEach(player -> this.addEntryToTop(new PlayerEntry(this, player, this.object)));
    }

    @Override
    public Component getEmptyStateText()
    {
        return EMPTY_STATE_TEXT;
    }

    @Override
    public int getRowWidth()
    {
        return width - 2;
    }

    @Override
    public void entryChanged(PlayerEntry entry)
    {
        this.parentScreen.entryPressed();
    }

    public static class PlayerEntry extends PlayerHeadEntryWithTitle<PlayerEntry> {

        private static final Subtitle SHARING = new Subtitle(Component.translatable("jmws.ui.stop_sharing.sharing"), MessageType.GREY);
        private static final Subtitle NOT_SHARING = new Subtitle(Component.translatable("jmws.ui.stop_sharing.not_sharing"), MessageType.SUCCESS);
        private static final Subtitle SELECTED = new Subtitle(Component.translatable("jmws.ui.generic.selected"), MessageType.PENDING);

        private final ClientObjectWrapper<?> object;
        private boolean didStopSharing = false;

        public PlayerEntry(@NotNull CheckableSelectionList<PlayerEntry> listOwner, PlayerInfo player, ClientObjectWrapper<?> object) {
            super(listOwner, player);

            this.object = object;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
            boolean isSelected = super.mouseClicked(event, doubleClick);
            if (doubleClick && canSelect())
            {
                this.setSelected(false);
                this.object.removeSharedTo(this.user.getProfile().id());
                this.didStopSharing = true;
            }

            return isSelected;
        }

        @Override
        public void extractSelectedState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
            this.setSubtitle(SELECTED);
        }

        @Override
        public void extractUnselectedState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
            if (didStopSharing)
            {
                this.setSubtitle(NOT_SHARING);
            } else {
                this.setSubtitle(SHARING);
            }
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
            super.extractContent(guiGraphicsExtractor, i, i1, b, v);
            RenderUtils.renderStatusBarInEntry(guiGraphicsExtractor, this, this.subtitle.getMessageType().getNumericalColour());


        }

        @Override
        public boolean canSelect() {
            return !didStopSharing;
        }
    }
}
