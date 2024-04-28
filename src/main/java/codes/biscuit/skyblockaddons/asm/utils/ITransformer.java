package codes.biscuit.skyblockaddons.asm.utils;

import org.spongepowered.asm.lib.tree.ClassNode;

public interface ITransformer {

    String[] getClassName();

    void transform(ClassNode classNode, String name);

}
