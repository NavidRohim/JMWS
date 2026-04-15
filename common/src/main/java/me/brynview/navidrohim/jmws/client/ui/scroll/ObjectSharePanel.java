package me.brynview.navidrohim.jmws.client.ui.scroll;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.plugin.JMWSPlugin;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
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

import java.util.*;

public class ObjectSharePanel <T extends ClientObjectWrapper<?>> extends ObjectSelectionList<ObjectSharePanel.SelectableLabelEntry> {

    public final static int PLAYER_HEAD_SIZE = 32;

    private final static int PLAYER_HEAD_SIZE_HALVED = PLAYER_HEAD_SIZE / 2;
    private final List<PlayerInfo> players = new ArrayList<>();
    private final T sharedObject;

    protected final Set<SelectableLabelEntry> highlightedEntries = new HashSet<>();

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

    public Set<SelectableLabelEntry> getSelectedPlayers()
    {
        return this.highlightedEntries;
    }

    public void unselectAll()
    {
        this.highlightedEntries.forEach(entry -> entry.selectedEntry = false);
        this.highlightedEntries.clear();
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
        graphics.outline(this.getX() - 2, this.getY() - 2, this.width + 4, this.height + 4, this.sharedObject.getColour());
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

    public abstract static class SelectableLabelEntry extends ObjectSelectionList.Entry<SelectableLabelEntry>
    {
        protected final @NotNull ObjectSharePanel<?> listOwner;
        protected boolean selectedEntry = false;
        protected @NotNull Component title;

        public @NotNull Subtitle subtitle;

        protected final static @NotNull Subtitle PENDING_SHARE = new Subtitle(Component.translatable("jmws.ui.sharing.pending"), MessageType.PENDING);
        protected final static @NotNull Subtitle ALREADY_SHARED = new Subtitle(Component.translatable("jmws.ui.sharing.already_shared"), MessageType.SUCCESS);
        protected final static @NotNull Subtitle EMPTY = new Subtitle(Component.translatable("jmws.ui.sharing.not_shared"), MessageType.GREY);
        protected static final @NotNull Subtitle SELECTED = new Subtitle(Component.translatable("jmws.ui.sharing.selected"), MessageType.FAILURE);

        public SelectableLabelEntry(@NotNull ObjectSharePanel<?> listOwner, @NotNull Component title, @NotNull Subtitle subtitle) {
            super();
            this.listOwner = listOwner;
            this.title = title;
            this.subtitle = subtitle;
        }

        public void setSubtitle(@NotNull Subtitle subtitle)
        {
            this.subtitle = subtitle;
        }

        public void setSelected(boolean selected)
        {
            if (selected)
            {
                if (canSelect())
                {
                    this.selectedEntry = true;
                    this.listOwner.highlightedEntries.add(this);
                }
            } else {
                this.selectedEntry = false;
                this.listOwner.highlightedEntries.remove(this);
            }
        }

        public boolean canSelect()
        {
            return true;
        }

        @Override
        public @NonNull Component getNarration() {
            return title;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
            boolean clicked = super.mouseClicked(event, doubleClick);

            if (doubleClick)
            {
                this.setSelected(false);
            } else {
                this.setSelected(!selectedEntry);
            }
            return clicked;
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            extractMainlineString(guiGraphicsExtractor, i, i1, b, v);
            extractSubtitleText(guiGraphicsExtractor, i, i1, b, v);
            extractThumbnailImage(guiGraphicsExtractor, i, i1, b, v);
            extractSelectedBackground(guiGraphicsExtractor, i, i1, b, v);
        }

        public void extractSelectedBackground(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            if (this.selectedEntry)
            {
                this.setSubtitle(new Subtitle(Component.translatable("jmws.ui.sharing.selected"), MessageType.of(null, this.listOwner.sharedObject.getColour())));
            }
        }

        public void extractThumbnailImage(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {

        }

        public void extractSubtitleText(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            guiGraphicsExtractor.text(JMWSCommon.minecraftClientInstance.font, subtitle.getDisplayableComponent(), this.getContentX() + PLAYER_HEAD_SIZE * 2, this.getContentYMiddle() + 3, subtitle.getMessageType().getNumericalColour());
        }

        public void extractMainlineString(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            guiGraphicsExtractor.text(JMWSCommon.minecraftClientInstance.font, title, this.getContentX() + PLAYER_HEAD_SIZE * 2, this.getContentYMiddle() - 8, -1);
        }
    }

    // Can't extend multiple classes... that is fucking annoying.
    public static class PlayerEntry extends SelectableLabelEntry
    {
        public final @NonNull PlayerInfo user;
        private final @NotNull UUID userUuid;

        public PlayerEntry(@NotNull PlayerInfo user, @NonNull ObjectSharePanel<? extends ClientObjectWrapper<?>> owner)
        {
            super(owner, Component.literal(user.getProfile().name()), EMPTY);
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
            } else if (this.listOwner.sharedObject.getSharedTo().contains(userUuid))
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
        public boolean canSelect()
        {
            return !JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(userUuid);
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
            boolean c = super.mouseClicked(event, doubleClick);

            if (doubleClick)
            {
                OutgoingShareRequest.sendShareRequest(this.listOwner.sharedObject, this.user.getProfile());
            }
            return c;
        }

        @Override
        public void extractThumbnailImage(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            int headPlacementX = this.getContentX() + PLAYER_HEAD_SIZE_HALVED + 4;
            int headPlacementY = this.getContentYMiddle() - PLAYER_HEAD_SIZE_HALVED;
            PlayerFaceExtractor.extractRenderState(guiGraphicsExtractor, user.getSkin(), headPlacementX, headPlacementY, PLAYER_HEAD_SIZE);
        }

        @Override
        public void extractSubtitleText(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
            super.extractSubtitleText(guiGraphicsExtractor, i, i1, b, v);
            if (!selectedEntry)
            {
                setSubtitle(getSubtitleText());
                super.extractSubtitleText(guiGraphicsExtractor, i, i1, b, v);
            }
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            super.extractContent(guiGraphicsExtractor, i, i1, b, v);
            this.extractSharingStatusBar(guiGraphicsExtractor, i, i1, b, v);
        }
    }
}
