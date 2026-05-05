package me.brynview.navidrohim.jmws.client.syncing.rules.registry;

import me.brynview.navidrohim.jmws.common.syncing.rules.CommonRule;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public interface ClientShareRule extends CommonRule
{
    Component getFailureMessage();
    Component getDescription();
    Component getDisplayName();

    default @NotNull List<RuleElement<?>> getDisplayableElements() {return List.of();}

    record RuleElement<T extends LayoutElement>(@NotNull T widget, @NotNull String valueName, InputTypes<?> valueType, Function<T, ?> getValueCallback)
    {

        public Object getValueFromWidget()
        {
            return this.getValueCallback().apply(this.widget());
        }

        public @NotNull T widget()
        {
            return widget;
        }
    }
}
