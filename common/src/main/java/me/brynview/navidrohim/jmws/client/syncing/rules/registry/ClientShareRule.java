package me.brynview.navidrohim.jmws.client.syncing.rules.registry;

import me.brynview.navidrohim.jmws.common.syncing.rules.CommonRule;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Function;

public interface ClientShareRule extends CommonRule
{
    Component getFailureMessage();
    Component getDescription();
    Component getDisplayName();

    default @NotNull List<RuleSetting<?>.RuleSettingWrapper<?>> getDisplayableElements() {return List.of();}

    class RuleSetting<B>
    {
        public final @NotNull ClientShareRule parentRule;
        public final @NotNull String valueName;
        public final @NotNull InputTypes<B> valueType;
        private @Nullable B value;

        public RuleSetting(@NotNull ClientShareRule parentRule, @NotNull String valueName, @NonNull InputTypes<B> valueType)
        {
            this.parentRule = parentRule;
            this.valueName = valueName;
            this.valueType = valueType;
        }

        public @Nullable B getValue()
        {
            return value;
        }

        public @NonNull ClientShareRule getParentRule()
        {
            return parentRule;
        }

        public String toString()
        {
            return "<RuleSetting " + valueName + " = " + value + ">";
        }

        public <E extends LayoutElement> RuleSettingWrapper<E> getWrapper(E displayableElement, Function<E, Object> valueGetter)
        {
            return new RuleSettingWrapper<>(displayableElement, valueGetter);
        }

        private RuleSetting<B> setValue(@NotNull B value)
        {
            this.value = value;
            return this;
        }

        public class RuleSettingWrapper<E extends LayoutElement>
        {
            private final @NotNull E displayableElement;
            private final @NotNull Function<E, Object> valueGetter;

            public RuleSettingWrapper(@NotNull E displayableElement, @NonNull Function<E, Object> valueGetter)
            {
                this.displayableElement = displayableElement;
                this.valueGetter = valueGetter;
            }

            public E getWidget()
            {
                return displayableElement;
            }

            public RuleSetting<B> getSetting()
            {
                return RuleSetting.this;
            }

            public RuleSetting<B> setValueForParent()
            {
                return RuleSetting.this.setValue(valueType.cast(valueGetter.apply(displayableElement)));
            }
        }
    }
}
