package net.teamfruit.lazyae2patch.mixins;

import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.storage.IStackWatcherHost;
import io.github.phantamanta44.libnine.util.data.ByteUtils;
import io.github.phantamanta44.threng.tile.TileLevelMaintainer;
import io.github.phantamanta44.threng.tile.base.TileNetworkDevice;
import net.teamfruit.lazyae2patch.Reference;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TileLevelMaintainer.class)
public abstract class MixinTileLevelMaintainer extends TileNetworkDevice implements IStackWatcherHost, ICraftingRequester {

    @Override
    public void onTileSyncPacket(ByteUtils.Reader data) {
        super.onTileSyncPacket(data);
    }
}
