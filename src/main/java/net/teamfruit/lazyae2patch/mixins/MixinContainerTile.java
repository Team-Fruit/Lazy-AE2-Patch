package net.teamfruit.lazyae2patch.mixins;

import io.github.phantamanta44.libnine.gui.L9Container;
import io.github.phantamanta44.threng.inventory.base.ContainerTile;
import net.minecraft.tileentity.TileEntity;
import net.teamfruit.lazyae2patch.IMixinContainerTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerTile.class)
public abstract class MixinContainerTile<T extends TileEntity> extends L9Container implements IMixinContainerTile<T> {

    @Shadow(remap = false)
    protected T tile;

    @Override
    public T getTile() {
        return this.tile;
    }
}
