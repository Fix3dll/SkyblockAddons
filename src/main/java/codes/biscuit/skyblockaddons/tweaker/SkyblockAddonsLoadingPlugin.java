package codes.biscuit.skyblockaddons.tweaker;

import codes.biscuit.skyblockaddons.asm.SkyblockAddonsASMTransformer;
import lombok.Getter;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class SkyblockAddonsLoadingPlugin implements IFMLLoadingPlugin {

    @Getter
    private static final boolean deobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    @Override
    public String[] getASMTransformerClass() {
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
