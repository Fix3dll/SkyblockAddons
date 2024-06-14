package codes.biscuit.skyblockaddons.shader;

import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

import java.util.Objects;
import java.util.function.Supplier;

public class Uniform<T> {

    private final UniformType<T> uniformType;
    private final Supplier<T> uniformValuesSupplier;
    private final String name;

    private int uniformID;
    private T previousUniformValue;

    public Uniform(Shader shader, UniformType<T> uniformType, String name, Supplier<T> uniformValuesSupplier) {
        this.uniformType = uniformType;
        this.uniformValuesSupplier = uniformValuesSupplier;
        this.name = name;

        init(shader, name);
    }

    private void init(Shader shader, String name) {
        uniformID = OpenGlHelper.glGetUniformLocation(shader.getProgram(), name);
    }

    public void update() {
        T newUniformValue = uniformValuesSupplier.get();
        if (!Objects.deepEquals(previousUniformValue, newUniformValue)) {
            if (uniformType == UniformType.FLOAT) {
                glUniform1f(uniformID, (Float) newUniformValue);

            } else if (uniformType == UniformType.VEC3) {
                float[] values = (float[]) newUniformValue;
                glUniform3f(uniformID, values[0], values[1], values[2]);
            }

            previousUniformValue = newUniformValue;
        }
    }

    private static final boolean USING_ARB_SHADERS;

    static {
        USING_ARB_SHADERS = OpenGlHelper.shadersSupported && !GLContext.getCapabilities().OpenGL21;
    }

    private static void glUniform1f(int location, float v0) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glUniform1fARB(location, v0);
        } else {
            GL20.glUniform1f(location, v0);
        }
    }

    private static void glUniform3f(int location, float v0, float v1, float v2) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glUniform3fARB(location, v0, v1, v2);
        } else {
            GL20.glUniform3f(location, v0, v1, v2);
        }
    }
}
