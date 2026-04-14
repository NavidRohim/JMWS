package me.brynview.navidrohim.jmws.client.ui.scroll;

import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.utils.PlayerUtils;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ObjectSharePanel <T extends ClientObjectWrapper<?>> extends ObjectSelectionList<ObjectSharePanel.Entry> {

    public final static int PLAYER_HEAD_SIZE = 32;

    private final static int PLAYER_HEAD_SIZE_HALVED = PLAYER_HEAD_SIZE / 2;
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
            graphics.text(JMWSCommon.minecraftClientInstance.font, "No online players!", this.getX(), this.getY() / 2, -1);
        }
    }

    public record Subtitle(Component component, MessageType messageType)
    {
        public Component getDisplayableComponent()
        {
            return Component.literal( messageType.toString() + "§o" + component.getString());
        }
    }

    public static class Entry extends ObjectSelectionList.Entry<Entry>
    {
        protected @NotNull Component title;
        protected @NotNull Subtitle subtitle;

        public Entry(@NotNull Component title, @NotNull Subtitle subtitle) {
            super();
            this.title = title;
            this.subtitle = subtitle;
        }

        public void setSubtitle(@NotNull Subtitle subtitle)
        {
            this.subtitle = subtitle;
        }

        @Override
        public @NonNull Component getNarration() {
            return title;
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            extractMainlineString(guiGraphicsExtractor, i, i1, b, v);
            extractSubtitleText(guiGraphicsExtractor, i, i1, b, v);
            extractThumbnailImage(guiGraphicsExtractor, i, i1, b, v);
        }

        public void extractThumbnailImage(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {

        }

        public void extractSubtitleText(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            guiGraphicsExtractor.text(JMWSCommon.minecraftClientInstance.font, subtitle.getDisplayableComponent(), this.getContentX() + PLAYER_HEAD_SIZE * 2, this.getContentYMiddle() + 3, -1);
        }

        public void extractMainlineString(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            guiGraphicsExtractor.text(JMWSCommon.minecraftClientInstance.font, title, this.getContentX() + PLAYER_HEAD_SIZE * 2, this.getContentYMiddle() - 8, -1);
        }
    }

    public static class PlayerEntry extends Entry
    {
        protected final @NotNull ObjectSharePanel<?> sharePanel;

        private final @NonNull PlayerInfo user;
        private final @NotNull UUID userUuid;

        protected final static @NotNull Subtitle PENDING_SHARE = new Subtitle(Component.literal("pending"), MessageType.PENDING);
        protected final static @NotNull Subtitle ALREADY_SHARED = new Subtitle(Component.literal("already shared"), MessageType.SUCCESS);
        protected final static @NotNull Subtitle EMPTY = new Subtitle(Component.literal("not shared"), MessageType.GREY);

        public PlayerEntry(@NotNull PlayerInfo user, @NonNull ObjectSharePanel<? extends ClientObjectWrapper<?>> owner)
        {
            super(Component.literal(user.getProfile().name()), EMPTY);

            this.sharePanel = owner;
            this.user = user;
            this.userUuid = user.getProfile().id();

            setSubtitle(getSubtitleText());
        }

        public Subtitle getSubtitleText()
        {
            Subtitle display;
            if (JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(userUuid))
            {
                display = PENDING_SHARE;
            } else if (this.sharePanel.sharedObject.getSharedTo().contains(userUuid))
            {
                display = ALREADY_SHARED;
            } else {
                display = EMPTY;
            }

            return display;
        }

        public void extractSharingStatusBar(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            // minus half width
            int barWidth = 6;
            int middle = (this.getContentX() + ((4 + PLAYER_HEAD_SIZE_HALVED) / 2));

            int startX = middle - (barWidth / 2);
            int endX = middle + (barWidth / 2);

            int startY = this.getContentYMiddle() - PLAYER_HEAD_SIZE_HALVED;
            int endY = this.getContentYMiddle() + PLAYER_HEAD_SIZE_HALVED;

            guiGraphicsExtractor.fill(startX, startY, endX, endY, this.subtitle.messageType.getNumericalColour());
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
            boolean c = super.mouseClicked(event, doubleClick);

            if (doubleClick)
            {
                if (!JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(this.userUuid))
                {
                    if (!sharePanel.sharedObject.getSharedTo().contains(this.userUuid))
                    {
                        this.sharePanel.sharedObject.sendShareRequest(this.userUuid);
                    } else {
                        PlayerUtils.sendUserAlert(Component.literal("You are already sharing %s with %s".formatted(this.sharePanel.sharedObject.getName(), this.user.getProfile().name())), true, true, MessageType.WARNING);
                    }
                } else {
                    PlayerUtils.sendUserAlert(Component.translatable("sharing.jmws.share_busy", this.user.getProfile().name()), true, true, MessageType.PENDING);
                }
            }
            return c;
        }

        @Override
        public void extractThumbnailImage(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            int headPlacementX = this.getContentX() + PLAYER_HEAD_SIZE_HALVED + 4;
            int headPlacementY = this.getContentYMiddle() - PLAYER_HEAD_SIZE_HALVED;
            PlayerFaceExtractor.extractRenderState(guiGraphicsExtractor, user.getSkin(), headPlacementX, headPlacementY, PLAYER_HEAD_SIZE);
            guiGraphicsExtractor.outline(headPlacementX - 1, headPlacementY - 1, PLAYER_HEAD_SIZE + 2, PLAYER_HEAD_SIZE + 2, this.sharePanel.sharedObject.getColour());
        }

        @Override
        public void extractSubtitleText(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
            setSubtitle(getSubtitleText());
            super.extractSubtitleText(guiGraphicsExtractor, i, i1, b, v);
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            super.extractContent(guiGraphicsExtractor, i, i1, b, v);
            this.extractSharingStatusBar(guiGraphicsExtractor, i, i1, b, v);
        }
    }
}
