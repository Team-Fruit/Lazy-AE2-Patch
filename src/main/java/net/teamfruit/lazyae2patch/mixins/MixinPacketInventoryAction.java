package net.teamfruit.lazyae2patch.mixins;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.SlotFake;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import io.github.phantamanta44.threng.inventory.ContainerLevelMaintainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PacketInventoryAction.class)
public abstract class MixinPacketInventoryAction extends AppEngPacket {

    @Shadow(remap = false)
    private InventoryAction action;

    @Shadow(remap = false)
    private int slot;

    @Shadow(remap = false)
    private IAEItemStack slotItem;

    @Inject(method = "serverPacketData", at = @At("TAIL"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onServerPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player, CallbackInfo ci, EntityPlayerMP sender) {
        if (this.action == InventoryAction.PLACE_JEI_GHOST_ITEM && sender.openContainer instanceof ContainerLevelMaintainer) {
            Slot slot = sender.openContainer.inventorySlots.get(this.slot);
            if (slot instanceof SlotFake) {
                if (this.slotItem != null) {
                    slot.putStack(this.slotItem.createItemStack());
                } else {
                    slot.putStack(ItemStack.EMPTY);
                }
            }
        }
    }
}
