package codes.biscuit.skyblockaddons.asm.transformer;

import codes.biscuit.skyblockaddons.asm.SkyblockAddonsASMTransformer;
import codes.biscuit.skyblockaddons.asm.utils.ITransformer;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;

import java.util.Iterator;

public class RenderGlobalTransformer implements ITransformer {

    private final String renderEntities;
    private final String renderEntitiesDesc;
    private final String isRenderEntityOutlines;
    private final String isRenderEntityOutlinesDesc;
    private final String iCamera;

    public RenderGlobalTransformer() {
        String entity;
        if (SkyblockAddonsASMTransformer.isDeobfuscated()) {
            renderEntities = "renderEntities";
            isRenderEntityOutlines = "isRenderEntityOutlines";
            iCamera = "net/minecraft/client/renderer/culling/ICamera";
            entity = "net/minecraft/entity/Entity";
        } else {
            renderEntities = "a";
            isRenderEntityOutlines = "d";
            iCamera = "bia";
            entity = "pk";
        }
        renderEntitiesDesc = "(L" + entity + ";L" + iCamera + ";F)V";
        isRenderEntityOutlinesDesc = "()Z";
    }

    private LabelNode existingLabel = null;
    private final LabelNode newLabel = new LabelNode();

    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.renderer.RenderGlobal"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (renderEntities.equals(methodNode.name) && renderEntitiesDesc.equals(methodNode.desc)) {

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode ain = iterator.next();

                    if (ain instanceof MethodInsnNode && ain.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode min = (MethodInsnNode) ain;

                        if (isRenderEntityOutlines.equals(min.name) && isRenderEntityOutlinesDesc.equals(min.desc)) {
                            if (ain.getNext() instanceof JumpInsnNode && ain.getNext().getOpcode() == Opcodes.IFEQ) {
                                JumpInsnNode jin = (JumpInsnNode) ain.getNext();
                                existingLabel = jin.label;
                                methodNode.instructions.insertBefore(
                                        ain.getPrevious(),
                                        shouldRenderEntityOutlinesExtraCondition(newLabel)
                                );
                            }
                        }
                    }

                    if (ain instanceof LabelNode && ain == existingLabel) {
                        methodNode.instructions.insertBefore(ain, newLabel);
                    }
                }
            }
        }
    }

    private InsnList shouldRenderEntityOutlinesExtraCondition(LabelNode labelNode) {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // camera
        list.add(new VarInsnNode(Opcodes.FLOAD, 3)); // partialTicks
        list.add(new VarInsnNode(Opcodes.DLOAD, 5)); // x
        list.add(new VarInsnNode(Opcodes.DLOAD, 7)); // y
        list.add(new VarInsnNode(Opcodes.DLOAD, 9)); // z
        list.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "codes/biscuit/skyblockaddons/mixins/hooks/RenderGlobalHook",
                        "blockRenderingSkyblockItemOutlines",
                        "(L" + iCamera  + ";FDDD)Z",
                        false));
        list.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));

        return list;
    }
}
