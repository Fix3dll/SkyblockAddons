package com.fix3dll.skyblockaddons.mixin;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.TweakerUtils;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class SkyblockAddonsMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        // Reference class for check for old installation of SkyblockAddons
        if (checkForClass("codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer")) {
            SkyblockAddons.getLogger().error("Launch failed because old installation of SkyblockAddons was found."
                    + " Please remove it and restart Minecraft!");
            TweakerUtils.showMessage("Launch failed because old version of SkyblockAddons was found."
                    + "\nPlease remove it and restart Minecraft!");
            TweakerUtils.exit();
        }
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
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