package net.teamfruit.lazyae2patch;

import net.minecraft.tileentity.TileEntity;

public interface IMixinContainerLevelMaintainer<T extends TileEntity> {

    T getTile();
}
