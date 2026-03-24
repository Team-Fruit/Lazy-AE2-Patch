package net.teamfruit.lazyae2patch.mixins;

import appeng.helpers.ICustomNameObject;
import io.github.phantamanta44.threng.tile.TileBigAssemblerCore;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileBigAssemblerCore.class)
public abstract class MixinTileBigAssemblerCore implements ICustomNameObject {

    @Unique
    private String lazyae2patch$customName;

    @Override
    public String getCustomInventoryName() {
        return lazyae2patch$customName != null ? lazyae2patch$customName : "";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return lazyae2patch$customName != null && !lazyae2patch$customName.isEmpty();
    }

    @Override
    public void setCustomName(String name) {
        lazyae2patch$customName = name;
    }

    // Use both MCP and SRG names to work in dev and production environments
    @Inject(method = {"writeToNBT", "func_189515_b"}, at = @At("TAIL"), remap = false)
    private void lazyae2patch$onWriteNBT(NBTTagCompound data, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (lazyae2patch$customName != null && !lazyae2patch$customName.isEmpty()) {
            data.setString("customName", lazyae2patch$customName);
        }
    }

    @Inject(method = {"readFromNBT", "func_145839_a"}, at = @At("TAIL"), remap = false)
    private void lazyae2patch$onReadNBT(NBTTagCompound data, CallbackInfo ci) {
        if (data.hasKey("customName")) {
            lazyae2patch$customName = data.getString("customName");
        } else {
            lazyae2patch$customName = null;
        }
    }
}
