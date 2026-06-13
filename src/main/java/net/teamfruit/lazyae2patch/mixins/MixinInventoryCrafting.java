package net.teamfruit.lazyae2patch.mixins;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AE2-UEL hardcodes processing-pattern InventoryCrafting to 4x4 (16 slots),
 * but Mass Assembler patterns can exceed that. Expand the backing list on demand.
 */
@Mixin(InventoryCrafting.class)
public abstract class MixinInventoryCrafting {

    @Shadow
    @Final
    @Mutable
    private NonNullList<ItemStack> stackList;

    @Inject(method = "setInventorySlotContents", at = @At("HEAD"))
    private void lazyae2patch$expandForLargePatterns(int index, ItemStack stack, CallbackInfo ci) {
        if (index >= 0 && index >= this.stackList.size()) {
            NonNullList<ItemStack> expanded = NonNullList.withSize(index + 1, ItemStack.EMPTY);
            for (int i = 0; i < this.stackList.size(); i++) {
                expanded.set(i, this.stackList.get(i));
            }
            this.stackList = expanded;
        }
    }
}
