package me.brynview.navidrohim.jmws.client.syncing.api;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncInformation;
import me.brynview.navidrohim.jmws.client.syncing.ClientSyncRegistry;
import me.brynview.navidrohim.jmws.client.syncing.objects.Context;
import me.brynview.navidrohim.jmws.client.syncing.rules.ClientShareRegistry;
import me.brynview.navidrohim.jmws.client.syncing.rules.ClientShareRule;
import me.brynview.navidrohim.jmws.common.api.PossessesIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface ClientObjectWrapper<T> extends PossessesIdentifier {

    String getSerialization();
    String getIdentifier();

    /*
    Note; in ARGB format
     */
    int getColour();

    UUID getOwner();
    T getNativeObject();

    boolean getGlobal();
    void setGlobal(boolean global);

    void addSharedTo(UUID sharedTo);
    void removeSharedTo(UUID sharedTo);
    void sendShareRequest(UUID sharedTo);

    void clearSharedTo();
    Set<UUID> getSharedTo();
    ClientBaseObjectWrapper.ClientShareRuleset getShareRules();
    boolean isSharing();

    void createRemotely(boolean silent);
    void removeRemotely(boolean silent);
    void updateRemotely();

    void createLocally();
    void removeLocally();

    ClientSyncInformation getInfo();
    void setInfo(ClientSyncInformation info);
    void update();

    boolean isLegacy();
    boolean isUsable();
    boolean isNative();
    boolean isInbuilt();

    void setNativeObject(@NotNull T nativeObject);
    void setContext(Context context);
    Context getContext();
    ClientSyncRegistry getType();

    final class ClientShareRuleset
    {
        private final String raw;
        private final ImmutableSet<ClientShareRule> rules;

        public ClientShareRuleset(@Nullable String jsonFormattedRuleset) {

            if (jsonFormattedRuleset == null)
            {
                this.raw = "";
                this.rules = ImmutableSet.of();
            } else {
                this.raw = jsonFormattedRuleset;
                this.rules = ClientShareRegistry.getRulesFromJsonString(jsonFormattedRuleset);
            }
        }

        public String getRaw()
        {
            return raw;
        }

        public String serialise()
        {
            JsonArray jsonRules = new JsonArray();
            for (ClientShareRule rule : rules) {
                jsonRules.add(rule.getRegistryKey());
            }
            return jsonRules.toString();
        }

        public ImmutableSet<ClientShareRule> getRuleset()
        {
            return rules;
        }
    }
}
