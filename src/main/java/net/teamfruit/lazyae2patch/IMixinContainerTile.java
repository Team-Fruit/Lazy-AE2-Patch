package net.teamfruit.lazyae2patch;

import net.minecraft.tileentity.TileEntity;

public interface IMixinContainerTile<T extends TileEntity> {

    T getTile();
}
