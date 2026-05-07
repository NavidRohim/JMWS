package me.brynview.navidrohim.jmws.client.syncing.rules.registry;

import com.google.gson.*;
import me.brynview.navidrohim.jmws.Constants;
import me.brynview.navidrohim.jmws.common.JMWSCommon;
import me.brynview.navidrohim.jmws.common.syncing.rules.CommonRule;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

public abstract class ClientShareRule implements CommonRule
{
    protected final List<RuleSetting<?>> settings;

    public ClientShareRule()
    {
        this.settings = this.getSettingsForRule();
    }

    protected abstract Component getFailureMessage();

    public abstract Component getDescription();

    public abstract Component getDisplayName();

    protected abstract List<RuleSetting<?>> getSettingsForRule();

    @NotNull
    public List<RuleSetting<?>.RuleSettingWrapper<?>> getDisplayableElements()
    {
        return List.of();
    }

    public static class RuleSerialiser implements JsonSerializer<ClientShareRule>
    {

        private static final Gson SERIALISER = new GsonBuilder().registerTypeHierarchyAdapter(ClientShareRule.class, new RuleSerialiser()).create();


        @Override
        public JsonElement serialize(ClientShareRule src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonArray jsonObject = new JsonArray();

            for (RuleSetting<?>.RuleSettingWrapper<?> displayableElement : src.getDisplayableElements())
            {
                JsonElement rulesJsonString = JMWSCommon.gson.toJsonTree(displayableElement.getSetting());
                jsonObject.add(rulesJsonString);
            }

            Constants.getLogger().info("Serialised rule: {}", jsonObject);
            return jsonObject;
        }

        public static JsonElement serialise(ClientShareRule rule)
        {
            return SERIALISER.toJsonTree(rule);
        }
    }

    public static class RuleSetting<B>
    {
        private final transient @NotNull ClientShareRule parentRule;
        private final transient @NotNull InputTypes<B> valueTypeWrapper;
        public transient B valueObj;

        public final @NotNull String valueType;
        public final @NotNull String valueName;
        public String value;

        public RuleSetting(@NotNull ClientShareRule parentRule, @NotNull String valueName, @NonNull InputTypes<B> valueType)
        {
            this.parentRule = parentRule;
            this.valueName = valueName;
            this.valueTypeWrapper = valueType;
            this.valueType = valueType.toString();
        }

        public @Nullable B getValueObj()
        {
            return valueObj;
        }

        public @NonNull ClientShareRule getParentRule()
        {
            return parentRule;
        }

        public String toString()
        {
            return "<RuleSetting %s=%s, hash=%s>".formatted(this.valueName, this.value, this.hashCode());
        }

        public <E extends LayoutElement> RuleSettingWrapper<E> getWrapper(E displayableElement, Function<E, Object> valueGetter)
        {
            return new RuleSettingWrapper<>(displayableElement, valueGetter);
        }

        private RuleSetting<B> setValueObj(@NotNull Object valueObj)
        {
            this.valueObj = (B) valueObj;
            this.value = valueObj.toString();
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
                return RuleSetting.this.setValueObj(valueTypeWrapper.cast(valueGetter.apply(displayableElement)));
            }
        }
    }
}
