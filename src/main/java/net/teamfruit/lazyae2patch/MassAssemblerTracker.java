package net.teamfruit.lazyae2patch;

import appeng.helpers.ICustomNameObject;
import appeng.tile.inventory.AppEngInternalInventory;
import io.github.phantamanta44.threng.tile.TileBigAssemblerCore;
import io.github.phantamanta44.threng.tile.TileBigAssemblerPatternStore;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public class MassAssemblerTracker {

    private static long autoBase = Long.MAX_VALUE;

    public final long id;
    public final long sortBy;
    public final String unlocalizedName;
    public final IItemHandler server;
    public final AppEngInternalInventory client;
    public final BlockPos pos;
    public final int dim;
    public final int numUpgrades;

    public MassAssemblerTracker(TileBigAssemblerPatternStore store, TileBigAssemblerCore core, int storeIndex) {
        this.id = autoBase--;
        this.server = store.getPatternInventory();
        this.client = new AppEngInternalInventory(null, this.server.getSlots());
        this.pos = core.getPos();
        this.dim = core.getWorld().provider.getDimension();
        long coreSort = ((long) pos.getZ() << 24) ^ ((long) pos.getX() << 8) ^ pos.getY();
        this.sortBy = (coreSort << 8) | storeIndex;
        this.unlocalizedName = getDisplayName(store, core);
        this.numUpgrades = Math.max(0, (this.server.getSlots() / 9) - 1);
    }

    public static String getDisplayName(TileBigAssemblerPatternStore store, TileBigAssemblerCore core) {
        if (core instanceof ICustomNameObject && ((ICustomNameObject) core).hasCustomInventoryName()) {
            return ((ICustomNameObject) core).getCustomInventoryName();
        }

        IBlockState storeState = store.getWorld().getBlockState(store.getPos());
        Block storeBlock = storeState.getBlock();
        ItemStack storeStack = new ItemStack(storeBlock, 1, storeBlock.getMetaFromState(storeState));
        return storeStack.getItem().getTranslationKey(storeStack);
    }
}
