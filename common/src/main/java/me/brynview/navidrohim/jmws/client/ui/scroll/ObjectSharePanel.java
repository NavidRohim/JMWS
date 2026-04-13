package me.brynview.navidrohim.jmws.client.ui.scroll;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.share.OutgoingShareRequests;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.common.CommonClass;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ObjectSharePanel <T extends ClientObjectWrapper<?>> extends ObjectSelectionList<ObjectSharePanel.Entry> {

    public final static int PLAYER_HEAD_SIZE = 32;

    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("widget/scroller_background");

    private final static int PLAYER_HEAD_SIZE_HALFED = PLAYER_HEAD_SIZE / 2;
    private final List<PlayerInfo> players = new ArrayList<>();
    private final T sharedObject;

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
        this.clearEntries();
        this.refreshPlayers();
        this.addWidgets();
    }

    public void addSelf(int x)
    {
        if (Constants.DEBUG)
        {
            // Loop for x times
            for (int i = 0; i < x; i++)
            {
                players.add(minecraft.getConnection().getPlayerInfo(minecraft.player.getGameProfile().id()));
            }
        }
    }

    public void addWidgets()
    {

        if (!this.players.isEmpty())
        {
            players.forEach(p -> this.addEntryToTop(new PlayerEntry(p, this)));
        }
    }

    @Override
    public void extractWidgetRenderState(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a)
    {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        if (this.players.isEmpty())
        {
            graphics.text(CommonClass.minecraftClientInstance.font, "No online players!", this.getX(), this.getY() / 2, -1);
        }
    }

    public static class Entry extends ObjectSelectionList.Entry<Entry>
    {
        protected final @NotNull Component displayName;
        protected final @NotNull Component pendingText;
        protected final @NotNull Component alreadyShared;

        protected final @NotNull ObjectSharePanel<?> sharePanel;

        public Entry(@NotNull Component displayName, @NotNull ObjectSharePanel<?> sharePanel) {
            super();

            this.displayName = displayName;
            this.sharePanel = sharePanel;
            this.pendingText = Component.literal("%s %s".formatted(displayName.getString(), MessageType.PENDING + "(pending)"));
            this.alreadyShared = Component.literal("%s %s".formatted(displayName.getString(), MessageType.SUCCESS + "(already shared)"));
        }

        @Override
        public @NonNull Component getNarration() {
            return displayName;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
        }
    }

    public static class PlayerEntry extends Entry
    {
        private final @NonNull PlayerInfo user;
        private final @NotNull UUID userUuid;

        public PlayerEntry(@NotNull PlayerInfo user, @NonNull ObjectSharePanel<? extends ClientObjectWrapper<?>> owner)
        {
            super(Component.literal(user.getProfile().name()), owner);
            this.user = user;
            this.userUuid = user.getProfile().id();
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
            boolean c = super.mouseClicked(event, doubleClick);

            if (doubleClick)
            {
                this.sharePanel.sharedObject.addSharedTo(user.getProfile().id());
            }

            return c;
        }


        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            super.extractContent(guiGraphicsExtractor, i, i1, b, v);
            Component display;

            if (this.sharePanel.sharedObject.getSharedTo().contains(userUuid))
            {
                display = this.alreadyShared;
            } else if (OutgoingShareRequests.hasShareRequestFor(userUuid))
            {
                display = this.pendingText;
            } else {
                display = this.displayName;
            }

            guiGraphicsExtractor.text(CommonClass.minecraftClientInstance.font, display, this.getContentX() + PLAYER_HEAD_SIZE * 2, this.getContentYMiddle() - 2, -1);
            PlayerFaceExtractor.extractRenderState(guiGraphicsExtractor, user.getSkin(), this.getContentX() + PLAYER_HEAD_SIZE_HALFED + 4, this.getContentYMiddle() - PLAYER_HEAD_SIZE_HALFED, PLAYER_HEAD_SIZE);
        }
    }
}
