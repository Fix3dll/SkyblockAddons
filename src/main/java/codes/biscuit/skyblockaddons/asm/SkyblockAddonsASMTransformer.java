package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.transformer.RenderGlobalTransformer;
import codes.biscuit.skyblockaddons.asm.utils.ITransformer;
import com.google.common.collect.ArrayListMultimap;
import lombok.Getter;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.transformers.MixinClassWriter;

import java.util.Collection;

public class SkyblockAddonsASMTransformer implements IClassTransformer {

    private static final Logger logger = SkyblockAddons.getLogger();
    @Getter
    private static final boolean deobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    private final ArrayListMultimap<String, ITransformer> transformerMap = ArrayListMultimap.create();

    public SkyblockAddonsASMTransformer() {
        registerTransformer(new RenderGlobalTransformer());
    }

    private void registerTransformer(ITransformer transformer) {
        for (String className : transformer.getClassName()) {
            transformerMap.put(className, transformer);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        Collection<ITransformer> transformers = transformerMap.get(transformedName);
        if (transformers.isEmpty()) {
            return bytes;
        }

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.EXPAND_FRAMES);

        transformers.forEach(transformer -> {
            logger.info("Applying transformer {} on {}", transformer.getClass().getName(), transformedName);
            transformer.transform(node, transformedName);
        });

        MixinClassWriter writer = new MixinClassWriter(ClassWriter.COMPUTE_MAXS);

        try {
            node.accept(writer);
        } catch (Throwable ex) {
            logger.error("An exception occurred while transforming {}", transformedName);
            ex.printStackTrace();
            return bytes;
        }

        return writer.toByteArray();
    }
}
