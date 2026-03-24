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
        // Highlight button points to the controller
        this.pos = core.getPos();
        this.dim = core.getWorld().provider.getDimension();
        BlockPos corePos = core.getPos();
        long coreSort = ((long) corePos.getZ() << 24) ^ ((long) corePos.getX() << 8) ^ corePos.getY();
        this.sortBy = (coreSort << 8) | storeIndex;
        // Use custom name if set via Cutting Knife, otherwise use block translation key
        if (core instanceof ICustomNameObject && ((ICustomNameObject) core).hasCustomInventoryName()) {
            this.unlocalizedName = ((ICustomNameObject) core).getCustomInventoryName();
        } else {
            IBlockState storeState = store.getWorld().getBlockState(store.getPos());
            Block storeBlock = storeState.getBlock();
            ItemStack storeStack = new ItemStack(storeBlock, 1, storeBlock.getMetaFromState(storeState));
            this.unlocalizedName = storeStack.getItem().getTranslationKey(storeStack);
        }
        // 36 slots = 4 rows of 9, so numUpgrades = 3 (base 1 row + 3 extra rows)
        this.numUpgrades = 3;
    }

    public boolean isDifferent(int slot) {
        ItemStack serverStack = server.getStackInSlot(slot);
        ItemStack clientStack = client.getStackInSlot(slot);
        if (serverStack.isEmpty() && clientStack.isEmpty()) {
            return false;
        }
        if (serverStack.isEmpty() || clientStack.isEmpty()) {
            return true;
        }
        return !ItemStack.areItemStacksEqual(serverStack, clientStack);
    }
}
