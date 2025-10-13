package me.navidrohim.jmws.client.config;

import me.navidrohim.jmws.common.Constants;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ImmutableButton extends GuiConfigEntries.ButtonEntry {
    public ImmutableButton(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
        this.btnDefault.enabled = false;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public void setToDefault() {

    }

    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public void undoChanges() {

    }

    @Override
    public boolean saveConfigElement() {
        return false;
    }

    @Override
    public Object getCurrentValue() {
        return null;
    }

    @Override
    public Object[] getCurrentValues() {
        return new Object[0];
    }

    @Override
    public void updateValueButtonText() {
        
    }

    @Override
    public void valueButtonPressed(int slotIndex) {
        Constants.getLogger().info("clicked");
    }
}
