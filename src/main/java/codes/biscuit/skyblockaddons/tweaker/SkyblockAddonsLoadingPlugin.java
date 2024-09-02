package codes.biscuit.skyblockaddons.tweaker;

import codes.biscuit.skyblockaddons.asm.SkyblockAddonsASMTransformer;
import net.hypixel.modapi.tweaker.HypixelModAPITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class SkyblockAddonsLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        //noinspection unchecked
        List<String> tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");
        tweakClasses.add(HypixelModAPITweaker.class.getName());

        return new String[]{SkyblockAddonsASMTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
