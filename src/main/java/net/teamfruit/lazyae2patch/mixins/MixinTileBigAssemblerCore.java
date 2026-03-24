package net.teamfruit.lazyae2patch.mixins;

import appeng.helpers.ICustomNameObject;
import io.github.phantamanta44.threng.tile.TileBigAssemblerCore;
import io.github.phantamanta44.threng.tile.base.TileAENetworked;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TileBigAssemblerCore.class)
public abstract class MixinTileBigAssemblerCore extends TileAENetworked implements ICustomNameObject {

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
        ((TileEntity) (Object) this).markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        if (lazyae2patch$customName != null && !lazyae2patch$customName.isEmpty()) {
            compound.setString("customName", lazyae2patch$customName);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("customName")) {
            lazyae2patch$customName = compound.getString("customName");
        } else {
            lazyae2patch$customName = null;
        }
    }
}
