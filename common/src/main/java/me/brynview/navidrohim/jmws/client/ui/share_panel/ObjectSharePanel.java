package me.brynview.navidrohim.jmws.client.ui.share_panel;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.PlayerEntry;
import me.brynview.navidrohim.jmws.client.ui.generic.selection_list.CheckableSelectionList;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ObjectSharePanel <T extends ClientObjectWrapper<?>> extends CheckableSelectionList {

    private final List<PlayerInfo> players = new ArrayList<>();
    public final T sharedObject;

    public int getRowWidth()
    {
        return width;
    }

    public ObjectSharePanel(Minecraft minecraft, int width, int height, int x, int y, int itemHeight, T clientObjectWrapper) {
        super(minecraft, width, height, y, itemHeight);
        this.setX(x);

        this.players.addAll(getPlayers());
        this.sharedObject = clientObjectWrapper;
    }

    private List<PlayerInfo> getPlayers()
    {
        List<PlayerInfo> onlinePlayers = new ArrayList<>(this.minecraft.player.connection.getOnlinePlayers().stream().toList());
        onlinePlayers.removeIf(player -> player.getProfile().equals(minecraft.player.getGameProfile()));

        return onlinePlayers;
    }

    private void refreshPlayers()
    {
        this.players.clear();
        this.players.addAll(getPlayers());
    }

    public void refresh()
    {
        JMWSPlugin.sync(false);
        this.clearEntries();
        this.refreshPlayers();
        this.addWidgets();
        this.unselectAll();
        this.addSelfDebug(5);
    }

    public void addWidgets()
    {
        if (!this.players.isEmpty())
        {
            players.forEach(p -> this.addEntryToTop(new PlayerEntry(p, this)));
        }
    }

    public void addSelfDebug(int times)
    {
        for (int i = 0; i < times; i++)
        {
            Constants.getLogger().info("Adding self to list");
            this.addEntryToTop(new PlayerEntry(minecraft.getConnection().getPlayerInfo(PlayerUtils.ourUUID()), this));
        }
    }

    @Override
    public void extractWidgetRenderState(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a)
    {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        if (this.players.isEmpty())
        {
            graphics.text(JMWSCommon.minecraftClientInstance.font, Component.translatable("jmws.ui.sharing.no_players"), this.getX(), this.getY() / 2, -1);
        }
    }

    @Override
    protected void extractListSeparators(GuiGraphicsExtractor graphics)
    {
        graphics.outline(this.getX() - 2, this.getY() - 2, this.width + 4, this.height + 4, MessageType.GREY.getNumericalColour());
    }

    @Override
    protected void extractListBackground(@NonNull GuiGraphicsExtractor graphics)
    {
        super.extractListBackground(graphics);
    }


    public static class Subtitle
    {
        private final Component displayable;
        private final MessageType messageType;

        public Subtitle(Component component, MessageType messageType)
        {
            this.displayable = Component.literal( messageType.toString() + "§o" + component.getString());
            this.messageType = messageType;
        }

        public Component getDisplayableComponent()
        {
            return displayable;
        }

        public MessageType getMessageType()
        {
            return messageType;
        }
    }

}
