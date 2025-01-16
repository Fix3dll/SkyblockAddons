package codes.biscuit.skyblockaddons.shader.chroma;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.shader.Shader;
import codes.biscuit.skyblockaddons.shader.UniformType;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;

public abstract class ChromaShader extends Shader {

    public ChromaShader(String shaderName) throws Exception {
        super(shaderName, shaderName);
    }

    @Override
    protected void registerUniforms() {
        // Chroma size is made proportionate to the size of the screen (ex. in a 1920px width screen, 100 = 1920)
        registerUniform(UniformType.FLOAT, "chromaSize", () -> Feature.CHROMA_SIZE.numberValue().floatValue() * (Minecraft.getMinecraft().displayWidth / 100F));
        registerUniform(UniformType.FLOAT, "timeOffset", () -> {
            float ticks = (float) main.getScheduler().getTotalTicks() + Utils.getPartialTicks();
            float chromaSpeed = Feature.CHROMA_SPEED.numberValue().floatValue() / 360F;
            return ticks * chromaSpeed;
        });
        registerUniform(UniformType.FLOAT, "saturation", () -> Feature.CHROMA_SATURATION.numberValue().floatValue());
    }
}