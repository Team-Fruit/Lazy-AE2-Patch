package net.teamfruit.lazyae2patch.jei;

import io.github.phantamanta44.threng.client.gui.GuiLevelMaintainer;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.teamfruit.lazyae2patch.IMixinGuiLevelMaintainer;

import java.util.ArrayList;
import java.util.List;

public class LazyAE2PatchGuiHandler implements IGhostIngredientHandler<GuiLevelMaintainer> {

    @Override
    public <I> List<Target<I>> getTargets(GuiLevelMaintainer gui, I ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();
        List<Target<?>> guiTarget = ((IMixinGuiLevelMaintainer) (Object) gui).getTargets(ingredient);
        targets.addAll((List<Target<I>>) (Object) guiTarget);
        return targets;
    }

    @Override
    public void onComplete() {
    }
}
