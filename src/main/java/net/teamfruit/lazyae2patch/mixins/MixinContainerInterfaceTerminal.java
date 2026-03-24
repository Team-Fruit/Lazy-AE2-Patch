package net.teamfruit.lazyae2patch.mixins;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.helpers.InventoryAction;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.WrapperRangeItemHandler;
import appeng.util.inv.filter.IAEItemFilter;
import io.github.phantamanta44.threng.tile.TileBigAssemblerCore;
import io.github.phantamanta44.threng.tile.TileBigAssemblerPatternStore;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.teamfruit.lazyae2patch.MassAssemblerTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static appeng.helpers.ItemStackHelper.stackWriteToNBT;

@Mixin(ContainerInterfaceTerminal.class)
public abstract class MixinContainerInterfaceTerminal extends AEBaseContainer {

    @Shadow(remap = false)
    private IGrid grid;

    @Shadow(remap = false)
    private NBTTagCompound data;

    private MixinContainerInterfaceTerminal() {
        super(null, null, null);
    }

    @Unique
    private final Map<TileBigAssemblerPatternStore, MassAssemblerTracker> lazyae2patch$maTrackers = new HashMap<>();

    @Unique
    private final Map<Long, MassAssemblerTracker> lazyae2patch$maById = new HashMap<>();

    /**
     * Redirect Map.size() on diList to force a full regen when Mass Assembler state changes.
     * This is the only Map.size() call in detectAndSendChanges, on the diList field.
     */
    @Redirect(method = "detectAndSendChanges",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;size()I", remap = false))
    private int lazyae2patch$onDiListSize(Map<?, ?> map) {
        if (lazyae2patch$maNeedsUpdate()) {
            return -1;
        }
        return map.size();
    }

    /**
     * After regenList rebuilds all interface entries, also add Mass Assembler pattern store entries.
     */
    @Inject(method = "regenList", at = @At("TAIL"), remap = false)
    private void lazyae2patch$onRegenList(NBTTagCompound data, CallbackInfo ci) {
        lazyae2patch$maTrackers.clear();
        lazyae2patch$maById.clear();

        if (this.grid == null) return;

        final IActionHost host = this.getActionHost();
        if (host == null) return;
        final IGridNode agn = host.getActionableNode();
        if (agn == null || !agn.isActive()) return;

        for (final IGridNode gn : this.grid.getMachines(TileBigAssemblerCore.class)) {
            if (!gn.isActive()) continue;
            final TileBigAssemblerCore core = (TileBigAssemblerCore) gn.getMachine();
            if (!core.isActive()) continue;

            final List<TileBigAssemblerPatternStore> stores = core.getPatternStores();
            for (int i = 0; i < stores.size(); i++) {
                final TileBigAssemblerPatternStore store = stores.get(i);
                final MassAssemblerTracker tracker = new MassAssemblerTracker(store, core, i);
                lazyae2patch$maTrackers.put(store, tracker);
                lazyae2patch$maById.put(tracker.id, tracker);
                lazyae2patch$writeTrackerData(data, tracker, 0, tracker.server.getSlots());
            }
        }
    }

    /**
     * Handle slot interactions for Mass Assembler entries in the Interface Terminal.
     */
    @Inject(method = "doAction", at = @At("HEAD"), cancellable = true, remap = false)
    private void lazyae2patch$onDoAction(EntityPlayerMP player, InventoryAction action, int slot, long id, CallbackInfo ci) {
        final MassAssemblerTracker tracker = lazyae2patch$maById.get(id);
        if (tracker == null) return;

        lazyae2patch$handleMaAction(player, action, slot, tracker);
        this.detectAndSendChanges();
        ci.cancel();
    }

    @Unique
    private void lazyae2patch$handleMaAction(EntityPlayerMP player, InventoryAction action, int slot, MassAssemblerTracker tracker) {
        final ItemStack is = tracker.server.getStackInSlot(slot);
        final boolean hasItemInHand = !player.inventory.getItemStack().isEmpty();

        final InventoryAdaptor playerHand = new AdaptorItemHandler(new WrapperCursorItemHandler(player.inventory));

        final IItemHandler theSlot = new WrapperFilteredItemHandler(
                new WrapperRangeItemHandler(tracker.server, slot, slot + 1),
                new LazyAE2PatternSlotFilter());
        final InventoryAdaptor interfaceSlot = new AdaptorItemHandler(theSlot);

        final IItemHandler interfaceHandler = tracker.server;

        switch (action) {
            case PICKUP_OR_SET_DOWN:
                if (hasItemInHand) {
                    boolean canInsert = true;
                    for (int s = 0; s < interfaceHandler.getSlots(); s++) {
                        if (Platform.itemComparisons().isSameItem(interfaceHandler.getStackInSlot(s), player.inventory.getItemStack())) {
                            canInsert = false;
                            break;
                        }
                    }
                    if (canInsert) {
                        ItemStack inSlot = theSlot.getStackInSlot(0);
                        if (inSlot.isEmpty()) {
                            player.inventory.setItemStack(interfaceSlot.addItems(player.inventory.getItemStack()));
                        } else {
                            inSlot = inSlot.copy();
                            final ItemStack inHand = player.inventory.getItemStack().copy();
                            ItemHandlerUtil.setStackInSlot(theSlot, 0, ItemStack.EMPTY);
                            player.inventory.setItemStack(ItemStack.EMPTY);
                            player.inventory.setItemStack(interfaceSlot.addItems(inHand.copy()));
                            if (player.inventory.getItemStack().isEmpty()) {
                                player.inventory.setItemStack(inSlot);
                            } else {
                                player.inventory.setItemStack(inHand);
                                ItemHandlerUtil.setStackInSlot(theSlot, 0, inSlot);
                            }
                        }
                    }
                } else {
                    ItemHandlerUtil.setStackInSlot(theSlot, 0, playerHand.addItems(theSlot.getStackInSlot(0)));
                }
                break;

            case SPLIT_OR_PLACE_SINGLE:
                if (hasItemInHand) {
                    boolean canInsert = true;
                    for (int s = 0; s < interfaceHandler.getSlots(); s++) {
                        if (Platform.itemComparisons().isSameItem(interfaceHandler.getStackInSlot(s), player.inventory.getItemStack())) {
                            canInsert = false;
                            break;
                        }
                    }
                    if (canInsert) {
                        ItemStack extra = playerHand.removeItems(1, ItemStack.EMPTY, null);
                        if (!extra.isEmpty() && !interfaceSlot.containsItems()) {
                            extra = interfaceSlot.addItems(extra);
                        }
                        if (!extra.isEmpty()) {
                            playerHand.addItems(extra);
                        }
                    }
                } else if (!is.isEmpty()) {
                    ItemStack extra = interfaceSlot.removeItems((is.getCount() + 1) / 2, ItemStack.EMPTY, null);
                    if (!extra.isEmpty()) {
                        extra = playerHand.addItems(extra);
                    }
                    if (!extra.isEmpty()) {
                        interfaceSlot.addItems(extra);
                    }
                }
                break;

            case SHIFT_CLICK: {
                final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(player);
                ItemHandlerUtil.setStackInSlot(theSlot, 0, playerInv.addItems(theSlot.getStackInSlot(0)));
                break;
            }

            case MOVE_REGION: {
                final InventoryAdaptor playerInvAd = InventoryAdaptor.getAdaptor(player);
                for (int x = 0; x < tracker.server.getSlots(); x++) {
                    ItemHandlerUtil.setStackInSlot(tracker.server, x, playerInvAd.addItems(tracker.server.getStackInSlot(x)));
                }
                break;
            }

            case CREATIVE_DUPLICATE:
                if (player.capabilities.isCreativeMode && !hasItemInHand) {
                    player.inventory.setItemStack(is.isEmpty() ? ItemStack.EMPTY : is.copy());
                }
                break;

            case PLACE_SINGLE: {
                final net.minecraft.inventory.Slot playerSlot;
                try {
                    playerSlot = this.inventorySlots.get(slot);
                } catch (IndexOutOfBoundsException ignored) {
                    return;
                }
                if (!(playerSlot instanceof appeng.container.slot.AppEngSlot)) return;
                if (!((appeng.container.slot.AppEngSlot) playerSlot).isPlayerSide() || !playerSlot.getHasStack())
                    return;
                ItemStack itemStack = playerSlot.getStack();
                if (!itemStack.isEmpty()) {
                    IItemHandler handler = new WrapperFilteredItemHandler(
                            new WrapperRangeItemHandler(tracker.server, 0, tracker.server.getSlots()),
                            new LazyAE2PatternSlotFilter());
                    playerSlot.putStack(ItemHandlerHelper.insertItem(handler, itemStack, false));
                }
                break;
            }

            default:
                break;
        }

        this.updateHeld(player);
    }

    @Unique
    private boolean lazyae2patch$maNeedsUpdate() {
        if (this.grid == null) return false;

        final IActionHost host = this.getActionHost();
        if (host == null) return false;
        final IGridNode agn = host.getActionableNode();
        if (agn == null || !agn.isActive()) return false;

        int total = 0;
        for (final IGridNode gn : this.grid.getMachines(TileBigAssemblerCore.class)) {
            if (!gn.isActive()) continue;
            final TileBigAssemblerCore core = (TileBigAssemblerCore) gn.getMachine();
            if (!core.isActive()) continue;
            for (final TileBigAssemblerPatternStore store : core.getPatternStores()) {
                if (!lazyae2patch$maTrackers.containsKey(store)) return true;
                total++;
            }
        }

        if (total != lazyae2patch$maTrackers.size()) return true;

        for (final MassAssemblerTracker tracker : lazyae2patch$maTrackers.values()) {
            for (int x = 0; x < tracker.server.getSlots(); x++) {
                if (tracker.isDifferent(x)) return true;
            }
        }

        return false;
    }

    @Unique
    private void lazyae2patch$writeTrackerData(NBTTagCompound data, MassAssemblerTracker tracker, int offset, int length) {
        final String name = "=" + Long.toString(tracker.id, Character.MAX_RADIX);
        final NBTTagCompound tag = data.getCompoundTag(name);

        if (tag.isEmpty()) {
            tag.setLong("sortBy", tracker.sortBy);
            tag.setString("un", tracker.unlocalizedName);
            tag.setTag("pos", NBTUtil.createPosTag(tracker.pos));
            tag.setInteger("dim", tracker.dim);
            tag.setInteger("numUpgrades", tracker.numUpgrades);
        }

        for (int x = 0; x < length; x++) {
            final NBTTagCompound itemNBT = new NBTTagCompound();
            final ItemStack is = tracker.server.getStackInSlot(x + offset);
            tracker.client.setStackInSlot(x + offset, is.isEmpty() ? ItemStack.EMPTY : is.copy());
            if (!is.isEmpty()) {
                stackWriteToNBT(is, itemNBT);
            }
            tag.setTag(Integer.toString(x + offset), itemNBT);
        }

        data.setTag(name, tag);
    }

    @Unique
    private static class LazyAE2PatternSlotFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof ICraftingPatternItem;
        }
    }
}
