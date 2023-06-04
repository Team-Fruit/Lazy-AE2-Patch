package net.teamfruit.lazyae2patch.mixins;

import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.threng.tile.TileLevelMaintainer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.teamfruit.lazyae2patch.IMixinInventoryRequest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileLevelMaintainer.InventoryRequest.class)
public abstract class MixinInventoryRequest implements ISerializable, IItemHandlerModifiable, IMixinInventoryRequest {

    @Shadow(remap = false)
    private TileLevelMaintainer owner;

    public TileLevelMaintainer getOwner() {
        return this.owner;
    }
}
