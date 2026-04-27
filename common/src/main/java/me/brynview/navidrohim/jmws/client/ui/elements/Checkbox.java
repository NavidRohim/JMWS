package me.brynview.navidrohim.jmws.client.ui.elements;

import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

public class Checkbox extends AbstractJMWSButton {

    public boolean isChecked = false;
    protected OnCheckboxPress onPress;
    private static final MutableComponent CHECKMARK = Component.literal("✔");

    protected Checkbox(int x, int y, int width, int height, Component message, OnCheckboxPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, (_) -> {}, createNarration);
        this.onPress = onPress;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        super.extractContents(guiGraphicsExtractor, i, i1, v);
        if (isChecked)
        {
            guiGraphicsExtractor.centeredText(Minecraft.getInstance().font, CHECKMARK, this.getX() + (width / 2), this.getY() + (height / 2) - 3, 0xFFFFFFFF);
        }
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        isChecked = !isChecked;
        this.onPress.onPress(this);
    }

    public static Checkbox buildCheckbox(Component message, OnCheckboxPress onPress) {
        return new Checkbox(0, 0, 11, 11, message, onPress, Button.DEFAULT_NARRATION);
    }

    public interface OnCheckboxPress
    {
        void onPress(Checkbox checkboxPressed);
    }
}
