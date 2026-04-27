package me.brynview.navidrohim.jmws.client.ui.list;

import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.generic.screen.HasScrollableList;
import me.brynview.navidrohim.jmws.client.ui.list.entry.PlayerEntry;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.generic.list.CheckableSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ObjectSharePanel <T extends ClientObjectWrapper<?>> extends CheckableSelectionList<PlayerEntry<T>> {

    private static final Component NO_PLAYERS_TEXT = Component.translatable("jmws.ui.sharing.no_players");
    private final List<PlayerInfo> players = new ArrayList<>();
    private final HasScrollableList parentScreen;

    public final T sharedObject;

    public ObjectSharePanel(Minecraft minecraft, int width, int height, int x, int y, int itemHeight, T clientObjectWrapper, HasScrollableList parentScreen) {
        super(minecraft, width, height, x, y, itemHeight);

        this.players.addAll(RenderUtils.getPlayers(minecraft));
        this.sharedObject = clientObjectWrapper;
        this.parentScreen = parentScreen;
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
    }

    public void addWidgets()
    {
        players.forEach(p -> this.addEntryToTop(new PlayerEntry<>(p, this, this.sharedObject)));
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
}
