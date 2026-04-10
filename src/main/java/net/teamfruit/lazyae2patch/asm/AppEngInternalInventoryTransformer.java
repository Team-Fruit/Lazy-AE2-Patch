package net.teamfruit.lazyae2patch.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * ASM transformer that adds a safe {@code getStackInSlot} override to
 * {@code AppEngInternalInventory}.
 *
 * <p>Some coremods heavily modify AE2's {@code GuiInterfaceTerminal},
 * which can result in {@code ClientDCInternalInv} entries with 0-size inventories.
 * When the GUI tries to render slots, {@code ItemStackHandler.validateSlotIndex} throws
 * {@code "Slot 0 not in valid range - [0,0)"}.
 *
 * <p>By overriding {@code getStackInSlot} at the inventory level, this fix applies
 * regardless of how the GUI code is modified by other mods.
 */
public class AppEngInternalInventoryTransformer implements IClassTransformer {

    private static final String TARGET_CLASS = "appeng.tile.inventory.AppEngInternalInventory";
    private static final String TARGET_CLASS_INTERNAL = "appeng/tile/inventory/AppEngInternalInventory";
    private static final String SUPER_CLASS = "net/minecraftforge/items/ItemStackHandler";
    private static final String ITEMSTACK_CLASS = "net/minecraft/item/ItemStack";
    private static final String ITEMSTACK_DESC = "L" + ITEMSTACK_CLASS + ";";
    private static final String GET_STACK_NAME = "getStackInSlot";
    private static final String GET_STACK_DESC = "(I)" + ITEMSTACK_DESC;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET_CLASS.equals(transformedName)) {
            return basicClass;
        }

        try {
            ClassNode cn = new ClassNode();
            new ClassReader(basicClass).accept(cn, 0);

            // Skip if getStackInSlot is already defined (by another transformer or the class itself)
            boolean alreadyDefined = cn.methods.stream()
                    .anyMatch(m -> GET_STACK_NAME.equals(m.name) && GET_STACK_DESC.equals(m.desc));
            if (alreadyDefined) {
                return basicClass;
            }

            String emptyField = isDeobfuscated() ? "EMPTY" : "field_190927_a";
            cn.methods.add(buildSafeGetStackInSlot(emptyField));

            ClassWriter cw = new SafeClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        } catch (Exception e) {
            System.err.println("[LazyAE2Patch] Failed to transform " + TARGET_CLASS);
            e.printStackTrace();
            return basicClass;
        }
    }

    /**
     * Builds a {@code getStackInSlot(int)} method node that performs bounds checking
     * before delegating to {@code super.getStackInSlot(int)}.
     * Returns {@code ItemStack.EMPTY} for out-of-bounds slot indices.
     *
     * <pre>{@code
     * public ItemStack getStackInSlot(int slot) {
     *     if (slot < 0 || slot >= this.getSlots()) return ItemStack.EMPTY;
     *     return super.getStackInSlot(slot);
     * }
     * }</pre>
     */
    private MethodNode buildSafeGetStackInSlot(String emptyFieldName) {
        MethodNode mn = new MethodNode(ACC_PUBLIC, GET_STACK_NAME, GET_STACK_DESC, null, null);

        LabelNode returnEmpty = new LabelNode();
        LabelNode callSuper = new LabelNode();

        InsnList insns = mn.instructions;

        insns.add(new VarInsnNode(ILOAD, 1));
        insns.add(new JumpInsnNode(IFLT, returnEmpty));

        insns.add(new VarInsnNode(ILOAD, 1));
        insns.add(new VarInsnNode(ALOAD, 0));
        insns.add(new MethodInsnNode(INVOKEVIRTUAL, TARGET_CLASS_INTERNAL, "getSlots", "()I", false));
        insns.add(new JumpInsnNode(IF_ICMPLT, callSuper));

        insns.add(returnEmpty);
        insns.add(new FieldInsnNode(GETSTATIC, ITEMSTACK_CLASS, emptyFieldName, ITEMSTACK_DESC));
        insns.add(new InsnNode(ARETURN));

        insns.add(callSuper);
        insns.add(new VarInsnNode(ALOAD, 0));
        insns.add(new VarInsnNode(ILOAD, 1));
        insns.add(new MethodInsnNode(INVOKESPECIAL, SUPER_CLASS, GET_STACK_NAME, GET_STACK_DESC, false));
        insns.add(new InsnNode(ARETURN));

        mn.maxLocals = 2;
        mn.maxStack = 2;
        return mn;
    }

    /** Checks if we are in a deobfuscated (development) environment to resolve the correct field name for {@code ItemStack.EMPTY}. */
    private static boolean isDeobfuscated() {
        try {
            Object val = Launch.blackboard.get("fml.deobfuscatedEnvironment");
            return val instanceof Boolean && (Boolean) val;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ClassWriter that avoids loading classes for frame computation.
     * In modded environments, {@code getCommonSuperClass} can fail
     * because not all classes are available on the classpath.
     */
    private static class SafeClassWriter extends ClassWriter {
        SafeClassWriter(int flags) {
            super(flags);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            return "java/lang/Object";
        }
    }
}
