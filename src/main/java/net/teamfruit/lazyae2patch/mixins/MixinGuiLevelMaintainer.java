package net.teamfruit.lazyae2patch.mixins;

import io.github.phantamanta44.libnine.client.gui.L9GuiContainer;
import io.github.phantamanta44.libnine.gui.L9Container;
import io.github.phantamanta44.threng.client.gui.GuiLevelMaintainer;
import io.github.phantamanta44.threng.constant.ResConst;
import io.github.phantamanta44.threng.inventory.ContainerLevelMaintainer;
import io.github.phantamanta44.threng.tile.TileLevelMaintainer;
import net.minecraft.util.ResourceLocation;
import net.teamfruit.lazyae2patch.IMixinContainerLevelMaintainer;
import net.teamfruit.lazyae2patch.IMixinInventoryRequest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(GuiLevelMaintainer.class)
public abstract class MixinGuiLevelMaintainer extends L9GuiContainer {

    @Shadow(remap = false)
    private ContainerLevelMaintainer cont;

    public MixinGuiLevelMaintainer(L9Container container, @Nullable ResourceLocation bg, int sizeX, int sizeY) {
        super(container, bg, sizeX, sizeY);
    }

    @Inject(method = "updateTextBoxes", at = @At("HEAD"), remap = false, cancellable = true)
    public void onUpdateTextBoxes(TileLevelMaintainer.InventoryRequest requests, CallbackInfo ci) {
        TileLevelMaintainer guiTile = ((IMixinContainerLevelMaintainer<TileLevelMaintainer>) (Object) this.cont).getTile();
        TileLevelMaintainer requestTile = ((IMixinInventoryRequest) (Object) requests).getOwner();
        if (!guiTile.equals(requestTile))
            ci.cancel();
    }

}
