package me.navidrohim.jmws.client;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;


public class MixinLoadingPlugin implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.jmws.json");
    }
}
