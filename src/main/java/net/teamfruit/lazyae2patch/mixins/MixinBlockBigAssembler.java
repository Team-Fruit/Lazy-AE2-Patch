package net.teamfruit.lazyae2patch.mixins;

import appeng.api.util.AEPartLocation;
import appeng.core.sync.GuiBridge;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.util.Platform;
import io.github.phantamanta44.libnine.component.multiblock.MultiBlockCore;
import io.github.phantamanta44.threng.block.BlockBigAssembler;
import io.github.phantamanta44.threng.tile.TileBigAssemblerCore;
import io.github.phantamanta44.threng.tile.base.IBigAssemblerUnit;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBigAssembler.class)
public abstract class MixinBlockBigAssembler {

    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
    private void lazyae2patch$onCuttingKnife(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                              EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ,
                                              CallbackInfoReturnable<Boolean> cir) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() instanceof ToolQuartzCuttingKnife) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IBigAssemblerUnit) {
                MultiBlockCore<IBigAssemblerUnit> core = ((IBigAssemblerUnit) te).getMultiBlockConnection().getCore();
                if (core != null && core.getUnit().isActive()) {
                    TileBigAssemblerCore coreTE = (TileBigAssemblerCore) core.getUnit();
                    if (!world.isRemote) {
                        Platform.openGUI(player, coreTE, AEPartLocation.fromFacing(facing), GuiBridge.GUI_RENAMER);
                    }
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
