package me.brynview.navidrohim.jmws.client.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.network.chat.Component;

public class MissingJourneyMapScreen extends ErrorScreen {
    public MissingJourneyMapScreen(Component title, Component message) {
        super(title, message);
    }

    @Override
    protected void init()
    {
        this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), (button) -> this.minecraft.stop()).bounds(this.width / 2 - 100, 140, 200, 20).build());
    }
}
