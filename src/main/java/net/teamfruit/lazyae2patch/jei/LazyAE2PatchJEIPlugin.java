package net.teamfruit.lazyae2patch.jei;

import io.github.phantamanta44.threng.client.gui.GuiLevelMaintainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class LazyAE2PatchJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.addGhostIngredientHandler(GuiLevelMaintainer.class, new LazyAE2PatchGuiHandler());
    }
}
