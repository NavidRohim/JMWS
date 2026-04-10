package me.brynview.navidrohim.jmws.client.ui.screen;

import me.brynview.navidrohim.jmws.client.syncing.impl.ClientWaypointWrapper;
import me.brynview.navidrohim.jmws.client.ui.scroll.WaypointSharePanel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import static me.brynview.navidrohim.jmws.common.CommonClass.minecraftClientInstance;

public class ShareScreen extends Screen {

    private final Screen parent;
    private final ServerStatus.Players players;
    private final ClientWaypointWrapper waypoint;

    private @Nullable LinearLayout layout;
    private @Nullable WaypointSharePanel sharePanel;

    public final HeaderAndFooterLayout headerAndFooterLayout = new HeaderAndFooterLayout(this);

    public ShareScreen(Screen parent, ServerStatus.Players players, ClientWaypointWrapper waypoint, Component title) {
        super(title);
        this.parent = parent;
        this.players = players;
        this.waypoint = waypoint;
    }


    @Override
    protected void init()
    {
        this.sharePanel = new WaypointSharePanel(minecraftClientInstance,  200, 50, 70, 50);

        this.layout = headerAndFooterLayout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.layout.addChild(Button.builder(CommonComponents.GUI_DONE, (bnt) -> this.onClose()).width(50).build());
        this.layout.addChild(this.sharePanel);

        this.headerAndFooterLayout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
    public static void openShare(ClientWaypointWrapper waypoint)
    {
        minecraftClientInstance.setScreen(new ShareScreen(minecraftClientInstance.screen, minecraftClientInstance.getCurrentServer().players, waypoint, Component.empty()));
    }
}
