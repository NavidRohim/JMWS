package me.brynview.navidrohim.jmws.client.ui.list.entry;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.JMWSClientCommon;
import me.brynview.navidrohim.jmws.client.share.request.OutgoingShareRequest;
import me.brynview.navidrohim.jmws.client.syncing.api.ClientObjectWrapper;
import me.brynview.navidrohim.jmws.client.ui.RenderUtils;
import me.brynview.navidrohim.jmws.client.ui.generic.Subtitle;
import me.brynview.navidrohim.jmws.client.ui.generic.list.entry.PlayerHeadEntryWithTitle;
import me.brynview.navidrohim.jmws.client.ui.list.ObjectSharePanel;
import me.brynview.navidrohim.jmws.common.enums.MessageType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public final class PlayerEntry<T extends ClientObjectWrapper<?>> extends PlayerHeadEntryWithTitle<PlayerEntry<T>> {

    private final static @NotNull Subtitle EMPTY = new Subtitle(Component.translatable("jmws.ui.sharing.not_shared"), MessageType.GREY);
    private final static @NotNull Subtitle ALREADY_SHARED = new Subtitle(Component.translatable("jmws.ui.sharing.already_shared"), MessageType.SUCCESS);
    private final static @NotNull Subtitle PENDING_SHARE = new Subtitle(Component.translatable("jmws.ui.sharing.pending"), MessageType.PENDING);
    private final static @NotNull Subtitle SELECTED = new Subtitle(Component.translatable("jmws.ui.generic.selected"), MessageType.SUCCESS);
    private final static @NotNull Subtitle STOPPED_SHARING = new Subtitle(Component.literal("stopped sharing"), MessageType.FAILURE);

    public enum EntryState
    {
        SHARED(ALREADY_SHARED),
        NOT_SHARED(EMPTY),
        PENDING(PENDING_SHARE),
        STOPPED_SHARING(PlayerEntry.STOPPED_SHARING);

        private final Subtitle associated;

        EntryState(Subtitle associated)
        {
            this.associated = associated;
        }

        public Subtitle getAssociated()
        {
            return associated;
        }
    }

    public final @Nullable PlayerInfo user;
    public final boolean isOnline;

    public final @NotNull UUID userUuid;
    private final @NotNull T sharedObject;

    private EntryState entryState = EntryState.NOT_SHARED;
    private boolean didStopSharing;

    public PlayerEntry(@NotNull PlayerInfo user, @NonNull ObjectSharePanel<T> owner, @NonNull T sharedObject) {
        super(owner, user);

        this.sharedObject = sharedObject;
        this.user = user;
        this.userUuid = user.getProfile().id();
        this.isOnline = true;

        setSelected(false);
        setSubtitle(getSubtitleText());
    }

    public PlayerEntry(@NotNull UUID user, @NonNull ObjectSharePanel<T> owner, @NonNull T sharedObject, boolean didStopSharing) {
        super(owner, user);

        this.sharedObject = sharedObject;
        this.user = null;
        this.userUuid = user;
        this.didStopSharing = didStopSharing;
        this.isOnline = false;

        setSubtitle(getSubtitleText());
    }

    public Subtitle getSubtitleText()
    {
        return entryState.getAssociated();
    }

    @Override
    public void setSelected(boolean selected) {
        if (JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(userUuid)) {
            entryState = EntryState.PENDING;
        } else if (sharedObject.getSharedTo().contains(userUuid)) {
            entryState = EntryState.SHARED;
        } else if (didStopSharing) {
            entryState = EntryState.STOPPED_SHARING;
        } else {
            entryState = EntryState.NOT_SHARED;
        }

        super.setSelected(selected);

        //setSubtitle(STOPPED_SHARING);
    }

    public EntryState getState()
    {
        return entryState;
    }

    public void setStoppedSharing()
    {
        if (!didStopSharing)
        {
            didStopSharing = true;
            sharedObject.removeSharedTo(userUuid);
            this.setSelected(false);
        }
    }

    @Override
    public boolean canSelect() {
        return !didStopSharing && !JMWSClientCommon.outgoingShareRequests.hasShareRequestFor(userUuid);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean c = super.mouseClicked(event, doubleClick);

        if (doubleClick && canSelect() && this.user != null) {
            this.setSelected(false);
            OutgoingShareRequest.sendShareRequest(sharedObject, this.user.getProfile());
        }
        return c;
    }


    @Override
    public void extractSelectedState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v)
    {
        setSubtitle(SELECTED);
    }

    @Override
    public void extractUnselectedState(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        if (!isSelected) {
            setSubtitle(getSubtitleText());
        }
    }

    @Override
    public void extractContent(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, boolean b, float v) {
        super.extractContent(guiGraphicsExtractor, i, i1, b, v);
        RenderUtils.renderStatusBarInEntry(guiGraphicsExtractor, this, this.subtitle.getMessageType().getNumericalColour());
    }
}
