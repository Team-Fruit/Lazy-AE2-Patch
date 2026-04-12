package net.teamfruit.lazyae2patch.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * FML coremod plugin for LazyAE2Patch.
 * Registers ASM transformers that patch AE2 classes at load time.
 * SortingIndex 1001 ensures this runs after AE2's own coremod (AE2ELCore).
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
public class LazyAE2PatchPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                "net.teamfruit.lazyae2patch.asm.AppEngInternalInventoryTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
