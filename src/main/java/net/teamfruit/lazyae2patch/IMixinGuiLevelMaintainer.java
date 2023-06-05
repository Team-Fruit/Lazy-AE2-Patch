package net.teamfruit.lazyae2patch;

import mezz.jei.api.gui.IGhostIngredientHandler;

import java.util.List;

public interface IMixinGuiLevelMaintainer {

    List<IGhostIngredientHandler.Target<?>> getTargets(Object ingredient);
}
