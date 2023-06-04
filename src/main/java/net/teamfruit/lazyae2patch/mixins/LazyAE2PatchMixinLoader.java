package net.teamfruit.lazyae2patch.mixins;

import com.google.common.collect.Lists;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class LazyAE2PatchMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Lists.newArrayList("mixins.lazyae2patch.json");
    }
}
