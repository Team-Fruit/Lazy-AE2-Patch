package net.teamfruit.lazyae2patch;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class PatternSlotFilter implements IAEItemFilter {

    public static final PatternSlotFilter INSTANCE = new PatternSlotFilter();

    @Override
    public boolean allowExtract(IItemHandler inv, int slot, int amount) {
        return true;
    }

    @Override
    public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ICraftingPatternItem;
    }
}
