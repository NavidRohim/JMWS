package me.brynview.navidrohim.jmws.client.ui.list;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.UIConstants;
import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSElement;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.HasScrollableList;
import me.brynview.navidrohim.jmws.client.ui.list.entry.PlayerEntry;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.generic.list.CheckableSelectionList;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ObjectSharePanel <T extends ClientObjectWrapper<?>> extends CheckableSelectionList<PlayerEntry<T>> {

    private static final Component NO_PLAYERS_TEXT = Component.translatable("jmws.ui.sharing.no_players");
    private static final Component PLAYERS_OFFLINE_SHARING_WITH = Component.translatable("jmws.ui.sharing.offline_sharing");
    private final int playersOfflineSharingWithWidth;

    public final List<PlayerInfo> players = new ArrayList<>();
    public final List<UUID> offlinePlayers = new ArrayList<>();

    public final List<PlayerEntry<T>> onlineEntries = new ArrayList<>();
    public final List<PlayerEntry<T>> offlineEntries = new ArrayList<>();

    private static final int OFFLINE_LABEL_Y_SPACING = 20;
    private final HasScrollableList parentScreen;
    private boolean shouldRenderOfflinePlayers = false;

    public final T sharedObject;

    public ObjectSharePanel(Minecraft minecraft, int width, int height, int x, int y, int itemHeight, T clientObjectWrapper, HasScrollableList parentScreen) {
        super(minecraft, width, height, x, y, itemHeight);

        this.sharedObject = clientObjectWrapper;
        this.parentScreen = parentScreen;

        this.playersOfflineSharingWithWidth = minecraft.font.width(PLAYERS_OFFLINE_SHARING_WITH);
        this.refreshPlayers();
        this.addSelf(2);
    }

    @Override
    public void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);

        if (shouldRenderOfflinePlayers)
        {
            for (PlayerEntry<T> offlineEntry : offlineEntries) {
                offlineEntry.setY(getEntryOfflineEntryYPos(offlineEntry));
            }
        }
    }

    private int getEntryOfflineEntryYPos(PlayerEntry<T> entry)
    {
        return (OFFLINE_LABEL_Y_SPACING * 2) + entry.getY();
    }

    @Override
    protected int contentHeight() {
        int normalHeight = super.contentHeight();
        if (shouldRenderOfflinePlayers)
        {
            return (normalHeight) + (OFFLINE_LABEL_Y_SPACING * 2);
        }
        return normalHeight;
    }

    private void refreshPlayers()
    {

        this.players.clear();
        this.offlinePlayers.clear();

        this.offlinePlayers.addAll(RenderUtils.getOfflinePlayers(this.sharedObject.getSharedTo()));
        this.players.addAll(RenderUtils.getPlayers(this.minecraft));
    }

    @Override
    public void renderBorder(@NotNull GuiGraphicsExtractor graphics) {
        RenderUtils.renderBorderForList(graphics, this);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public void refresh()
    {
        this.offlineEntries.clear();
        this.onlineEntries.clear();

        JMWSPlugin.sync(false);
        this.unselectAll();
        this.clearEntries();
        this.refreshPlayers();
        //this.addSelf(5);
        this.addWidgets();
    }

    private void addSelf(int i)
    {
        for (int j = 0; j < i; ++j)
        {
            this.players.add(PlayerUtils.getOurPlayerInfo());
        }
    }

    public void addWidgets()
    {
        for (UUID player : offlinePlayers) {
            PlayerEntry<T> offlinePlayer = new PlayerEntry<>(player, this, this.sharedObject, false);
            this.addEntry(offlinePlayer);
            this.offlineEntries.add(offlinePlayer);
        }

        for (PlayerInfo player : players) {
            PlayerEntry<T> onlinePlayer = new PlayerEntry<>(player, this, this.sharedObject);
            this.addEntryToTop(onlinePlayer);
            this.onlineEntries.add(onlinePlayer);
        }

        shouldRenderOfflinePlayers = !this.offlineEntries.isEmpty() && !this.onlineEntries.isEmpty();
        this.setScrollAmount(0);
    }

    private void extractOfflinePlayersTextLabel(@NonNull GuiGraphicsExtractor guiGraphicsExtractor)
    {
        if (shouldRenderOfflinePlayers)
        {
            PlayerEntry<T> firstEntry = this.onlineEntries.getFirst();

            int bottomBoundary = this.getY() + this.getHeight();
            int offlineLabelY = (firstEntry.getContentY() + firstEntry.getContentHeight() + (OFFLINE_LABEL_Y_SPACING / 2));
            int offlineLabelBottom = offlineLabelY + minecraft.font.lineHeight;
            int offlineLabelYLine = offlineLabelBottom + 4;

            int x = this.getX() + this.getWidth() / 2;

            if (offlineLabelYLine < bottomBoundary)
            {
                guiGraphicsExtractor.horizontalLine(x - this.playersOfflineSharingWithWidth / 2, x + this.playersOfflineSharingWithWidth / 2, offlineLabelYLine, 0xFFFFFFFF);
            }
            if (offlineLabelBottom < bottomBoundary)
            {
                guiGraphicsExtractor.centeredText(this.minecraft.font, PLAYERS_OFFLINE_SHARING_WITH, x, offlineLabelY, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public int getRowWidth()
    {
        return width - 4;
    }

    @Override
    public void entryChanged(PlayerEntry<T> entry)
    {
        this.parentScreen.entryPressed();
    }

    @Override
    public Component getEmptyStateText()
    {
        return NO_PLAYERS_TEXT;
    }

    @Override
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        this.extractOfflinePlayersTextLabel(graphics);
    }
}
