package me.brynview.navidrohim.jmws.client.ui.scroll;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.common.CommonClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ObjectSharePanel <T extends ClientObjectWrapper<?>> extends ObjectSelectionList<ObjectSharePanel.Entry> {

    public final static int PLAYER_HEAD_SIZE = 32;

    private final static int PLAYER_HEAD_SIZE_HALFED = PLAYER_HEAD_SIZE / 2;
    private final List<PlayerInfo> players = new ArrayList<>();
    private final T sharedObject;

    public ObjectSharePanel(Minecraft minecraft, int width, int height, int y, int itemHeight, T clientObjectWrapper) {
        super(minecraft, width, height, y, itemHeight);
        this.players.addAll(getPlayers());
        this.sharedObject = clientObjectWrapper;
    }

    private List<PlayerInfo> getPlayers()
    {
        List<PlayerInfo> onlinePlayers = new ArrayList<>(this.minecraft.player.connection.getOnlinePlayers().stream().toList());
        onlinePlayers.removeIf(player -> player.getProfile().equals(minecraft.player.getGameProfile()));

        return onlinePlayers;
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

        if (this.players.isEmpty())
        {
            this.addEntry(new Entry("No online players.", this));
        } else {
            players.forEach(p -> this.addEntry(new Entry(p, this)));
        }
    }

    public static class Entry extends ObjectSelectionList.Entry<Entry>
    {
        private final @Nullable PlayerInfo user;
        private final @NotNull Component displayName;
        private final @NotNull ObjectSharePanel<?> sharePanel;

        public Entry(@NotNull PlayerInfo user, @NonNull ObjectSharePanel<? extends ClientObjectWrapper<?>> owner)
        {
            super();

            this.user = user;
            this.displayName = Component.literal(user.getProfile().name());
            this.sharePanel = owner;

        }

        public Entry(@NotNull String text, @NonNull ObjectSharePanel<? extends ClientObjectWrapper<?>> owner)
        {
            super();
            this.user = null;
            this.displayName = Component.literal(text);
            this.sharePanel = owner;
        }


        @Override
        public @NonNull Component getNarration()
        {
            return displayName;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
            boolean c = super.mouseClicked(event, doubleClick);

            if (doubleClick && user != null)
            {
                this.sharePanel.sharedObject.addSharedTo(user.getProfile().id());
            }

            return c;
        }


        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
        {
            if (this.user != null)
            {
                PlayerFaceExtractor.extractRenderState(guiGraphicsExtractor, user.getSkin(), PLAYER_HEAD_SIZE_HALFED, this.getContentYMiddle() - PLAYER_HEAD_SIZE_HALFED, PLAYER_HEAD_SIZE);
            }
            guiGraphicsExtractor.horizontalLine(0, getContentRight(), getContentY() , -8355712);
            guiGraphicsExtractor.horizontalLine(0, getContentRight(), getContentY() + getContentHeight() , -8355712);
            guiGraphicsExtractor.text(CommonClass.minecraftClientInstance.font, this.displayName, PLAYER_HEAD_SIZE * 2, this.getContentYMiddle() - 2, -1);
        }
    }
}
