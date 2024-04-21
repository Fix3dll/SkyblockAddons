package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import lombok.Getter;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

import static codes.biscuit.skyblockaddons.utils.TweakerUtils.exit;
import static codes.biscuit.skyblockaddons.utils.TweakerUtils.showMessage;

public class SkyblockAddonsMixinPlugin implements IMixinConfigPlugin {

    @Getter
    private static boolean deobfuscated;

    @Override
    public void onLoad(String mixinPackage) {
        // Reference class for check for old installation of SkyblockAddons
        if (checkForClass("codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer")) {
            SkyblockAddons.getLogger().error("Launch failed because old installation of SkyblockAddons was found."
                    + " Please remove it and restart Minecraft!");
            showMessage("Launch failed because old version of SkyblockAddons was found."
                    + "\nPlease remove it and restart Minecraft!");
            exit();
        }

        deobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        MixinExtrasBootstrap.init();
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
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

    private boolean checkForClass(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}