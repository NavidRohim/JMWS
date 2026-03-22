package me.brynview.navidrohim.jmws.client.screens;

import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.client.ClientCommonClass;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * Screen that is displayed if JourneyMap is missing on Fabric.
 * We extend ErrorScreen because the default ErrorScreen's button is to just close the screen
 * instead of closing the game which we want here.
 */
public class MissingJourneyMapScreen extends ErrorScreen {
    public MissingJourneyMapScreen(Component title, Component message) {
        super(title, message);
    }

    @Override
    protected void init()
    {
        // Button that is used to quit game. Placed in the middle of the screen, lower half vertically if I am remembering correctly.
        this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), (button) -> this.minecraft.stop()).bounds(this.width / 2 - 100, 160, 200, 20).build());
    }

    // was: void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        Component renderedString = Component.translatable("error.jmws.screen.needing_got", Constants.JourneyMapVersionString, !ClientCommonClass.clientHasJM && ClientCommonClass.clientJMVersion instanceof String ? ClientCommonClass.clientJMVersion : "Nothing!");
        guiGraphics.centeredText(this.font, renderedString, this.width / 2, 130, -1);
    }
}
