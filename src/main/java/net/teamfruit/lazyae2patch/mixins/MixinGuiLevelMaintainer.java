package net.teamfruit.lazyae2patch.mixins;

import appeng.container.slot.SlotFake;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import io.github.phantamanta44.libnine.client.gui.L9GuiContainer;
import io.github.phantamanta44.libnine.gui.L9Container;
import io.github.phantamanta44.threng.client.gui.GuiLevelMaintainer;
import io.github.phantamanta44.threng.inventory.ContainerLevelMaintainer;
import io.github.phantamanta44.threng.tile.TileLevelMaintainer;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.teamfruit.lazyae2patch.IMixinContainerTile;
import net.teamfruit.lazyae2patch.IMixinGuiLevelMaintainer;
import net.teamfruit.lazyae2patch.IMixinInventoryRequest;
import net.teamfruit.lazyae2patch.Reference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(GuiLevelMaintainer.class)
public abstract class MixinGuiLevelMaintainer extends L9GuiContainer implements IMixinGuiLevelMaintainer {

    @Shadow(remap = false)
    private ContainerLevelMaintainer cont;

    public MixinGuiLevelMaintainer(L9Container container, @Nullable ResourceLocation bg, int sizeX, int sizeY) {
        super(container, bg, sizeX, sizeY);
    }

    public ContainerLevelMaintainer getContainer() {
        return this.cont;
    }

    @Inject(method = "updateTextBoxes", at = @At("HEAD"), remap = false, cancellable = true)
    public void onUpdateTextBoxes(TileLevelMaintainer.InventoryRequest requests, CallbackInfo ci) {
        TileLevelMaintainer guiTile = ((IMixinContainerTile<TileLevelMaintainer>) (Object) this.cont).getTile();
        TileLevelMaintainer requestTile = ((IMixinInventoryRequest) (Object) requests).getOwner();
        if (!guiTile.equals(requestTile))
            ci.cancel();
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getTargets(Object obj) {
        if (!(obj instanceof ItemStack)) {
            return Collections.emptyList();
        }

        List<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();
        for (Slot slot : getContainer().inventorySlots) {
            if (slot instanceof SlotFake) {
                IGhostIngredientHandler.Target<Object> target = new IGhostIngredientHandler.Target<Object>() {

                    @Override
                    public Rectangle getArea() {
                        return new Rectangle(getGuiLeft() + slot.xPos, getGuiTop() + slot.yPos, 16, 16);
                    }

                    @Override
                    public void accept(Object item) {
                        ItemStack itemStack = (ItemStack) item;
                        try {
                            PacketInventoryAction packet = new PacketInventoryAction(InventoryAction.PLACE_JEI_GHOST_ITEM, (SlotFake) slot, AEItemStack.fromItemStack(itemStack));
                            NetworkHandler.instance().sendToServer(packet);
                        } catch (IOException e) {
                            Reference.logger.error(e);
                        }
                    }
                };
                targets.add(target);
            }
        }
        return targets;
    }

}
