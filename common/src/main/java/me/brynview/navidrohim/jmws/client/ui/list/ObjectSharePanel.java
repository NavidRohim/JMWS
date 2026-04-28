package me.brynview.navidrohim.jmws.client.ui.list;

import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
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
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ObjectSharePanel <T extends ClientObjectWrapper<?>> extends CheckableSelectionList<PlayerEntry<T>> {

    private static final Component NO_PLAYERS_TEXT = Component.translatable("jmws.ui.sharing.no_players");
    private final static Component PLAYERS_OFFLINE_SHARING_WITH = Component.translatable("jmws.ui.sharing.offline_sharing");
    private final List<PlayerInfo> players = new ArrayList<>();
    public final List<UUID> offlinePlayers = new ArrayList<>();

    private final HasScrollableList parentScreen;

    public final T sharedObject;

    public ObjectSharePanel(Minecraft minecraft, int width, int height, int x, int y, int itemHeight, T clientObjectWrapper, HasScrollableList parentScreen) {
        super(minecraft, width, height, x, y, itemHeight);

        this.players.addAll(RenderUtils.getPlayers(minecraft));
        this.sharedObject = clientObjectWrapper;
        this.parentScreen = parentScreen;
        this.offlinePlayers.add(UUID.randomUUID());
    }

    @Override
    public void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);
    }

    protected void addEntryUnderText(@NonNull PlayerEntry<T> entry) {
        entry.setX(this.getRowLeft());
        entry.setWidth(this.getRowWidth());
        entry.setY(this.getNextY() + 20);
        entry.setHeight(this.defaultEntryHeight);
        this.addEntry(entry);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean updateScrolling(@NonNull MouseButtonEvent event) {
        //return super.updateScrolling(event);
        return true;
    }

    private void refreshPlayers()
    {
        this.players.clear();
        this.players.addAll(RenderUtils.getPlayers(this.minecraft));
    }



    public void refresh()
    {
        JMWSPlugin.sync(false);
        this.unselectAll();
        this.clearEntries();
        this.refreshPlayers();
        this.addWidgets();
        this.addSelf(5);
    }

    private void addSelf(int i)
    {
        for (int j = 0; j < i; ++j)
        {
            this.addEntry(new PlayerEntry<>(PlayerUtils.getOurPlayerInfo(), this, this.sharedObject));
        }
    }

    public void addWidgets()
    {
        players.forEach(p -> this.addEntryToTop(new PlayerEntry<>(p, this, this.sharedObject)));
        if (!this.offlinePlayers.isEmpty())
        {
            for (UUID player : offlinePlayers) {
                PlayerEntry<T> offlinePlayer = new PlayerEntry<>(player, this, this.sharedObject, false);
                this.addEntryUnderText(offlinePlayer);
            }
        }

    }

    private void extractOfflinePlayersTextLabel(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, PlayerEntry<?> lastPlayer)
    {
        int x = lastPlayer.getContentX() + lastPlayer.getContentWidth() / 2;
        int y = lastPlayer.getContentY() + lastPlayer.getContentHeight();

        if (y < this.getY() + this.getHeight())
        {
            guiGraphicsExtractor.centeredText(this.minecraft.font, PLAYERS_OFFLINE_SHARING_WITH, x, y, 0xFFFFFFFF);
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
        if (!this.offlinePlayers.isEmpty() && !this.children().isEmpty())
        {
            this.extractOfflinePlayersTextLabel(graphics, this.children().getLast());
        }
    }
}
