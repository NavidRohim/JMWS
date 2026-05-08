package me.brynview.navidrohim.jmws.client.ui.generic.screen;

import me.brynview.navidrohim.jmws.client.ui.generic.elements.AbstractJMWSElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractJMWSScreen extends Screen {

    protected List<AbstractJMWSElement> elements = new ArrayList<>();

    protected AbstractJMWSScreen(Component title) {
        super(title);
    }

    public final void addChildToLayout(LinearLayout layout, AbstractJMWSElement element, @Nullable Consumer<LayoutSettings> consumer)
    {
        elements.add(element);
        if (consumer != null)
        {
            layout.addChild(element, consumer);
        } else {
            layout.addChild(element);
        }
    }

    public final void addChildToLayout(LinearLayout layout, AbstractJMWSElement element)
    {
        this.addChildToLayout(layout, element, null);
    }

    @Override
    protected void init()
    {
        super.init();
        this.elements.clear();
    }

    protected void refresh()
    {
        this.elements.forEach(AbstractJMWSElement::refresh);
    }
}
