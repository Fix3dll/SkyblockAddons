package codes.biscuit.skyblockaddons.mixins;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import lombok.Getter;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A mixin plugin to automatically discover all mixins in the current JAR.
 * <p>
 * This mixin plugin automatically scans your entire JAR (or class directory, in case of an in-IDE launch) for classes inside of your
 * mixin package and registers those. It does this recursively for sub packages of the mixin package as well. This means you will need
 * to only have mixin classes inside of your mixin package, which is good style anyway.
 *
 * @author Linnea Gräf
 * @see <a href="https://github.com/nea89o/Forge1.8.9Template/blob/master/src/main/java/com/example/init/AutoDiscoveryMixinPlugin.java">nea89o/Forge1.8.9Template</a>
 */
public class SkyblockAddonsMixinPlugin implements IMixinConfigPlugin {

    @Getter
    private static boolean deobfuscated;

    @Override
    public void onLoad(String mixinPackage) {
        deobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        MixinExtrasBootstrap.init();
    }

    /**
     * Resolves the base class root for a given class URL. This resolves either the JAR root, or the class file root.
     * In either case the return value of this + the class name will resolve back to the original class url, or to other
     * class urls for other classes.
     */
    public URL getBaseUrlForClassUrl(URL classUrl) {
        String string = classUrl.toString();
        if (classUrl.getProtocol().equals("jar")) {
            try {
                return new URL(string.substring(4).split("!")[0]);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        if (string.endsWith(".class")) {
            try {
                return new URL(string.replace("\\", "/")
                        .replace(getClass().getCanonicalName()
                                .replace(".", "/") + ".class", ""));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return classUrl;
    }

    /**
     * A list of all discovered mixins.
     */
    private List<String> mixins = null;

    String mixinBasePackage = "codes.biscuit.skyblockaddons.mixins.transformers.";
    String mixinBaseDir = mixinBasePackage.replace(".", "/");

    /**
     * Try to add mixin class ot the mixins based on the filepath inside of the class root.
     * Removes the {@code .class} file suffix, as well as the base mixin package.
     * <p><b>This method cannot be called after mixin initialization.</p>
     *
     * @param className the name or path of a class to be registered as a mixin.
     */
    public void tryAddMixinClass(String className) {
        String norm = (className.endsWith(".class") ? className.substring(0, className.length() - ".class".length()) : className)
                .replace("\\", "/")
                .replace("/", ".");
        if (norm.startsWith(mixinBasePackage) && !norm.endsWith(".")) {
            mixins.add(norm.substring(mixinBasePackage.length()));
        }
    }

    /**
     * Search through the JAR or class directory to find mixins contained in {@link #mixinBasePackage}
     */
    @Override
    public List<String> getMixins() {
        if (mixins != null) return mixins;
        System.out.println("Trying to discover mixins");
        mixins = new ArrayList<>();
        URL classUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
        System.out.println("Found classes at " + classUrl);
        Path file;
        try {
            file = Paths.get(getBaseUrlForClassUrl(classUrl).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Base directory found at " + file);
        if (Files.isDirectory(file)) {
            walkDir(file);
        } else {
            walkJar(file);
        }
        System.out.println("Found mixins: " + mixins);

        return mixins;
    }

    /**
     * Search through directory for mixin classes based on {@link #mixinBaseDir}.
     *
     * @param classRoot The root directory in which classes are stored for the default package.
     */
    private void walkDir(Path classRoot) {
        System.out.println("Trying to find mixins from directory");
        try (Stream<Path> classes = Files.walk(classRoot.resolve(mixinBaseDir))) {
            classes.map(it -> classRoot.relativize(it).toString())
                    .forEach(this::tryAddMixinClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read through a JAR file, trying to find all mixins inside.
     */
    private void walkJar(Path file) {
        System.out.println("Trying to find mixins from jar file");
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file))) {
            ZipEntry next;
            while ((next = zis.getNextEntry()) != null) {
                tryAddMixinClass(next.getName());
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }
}