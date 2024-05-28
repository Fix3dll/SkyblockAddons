package codes.biscuit.skyblockaddons.shader;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.opengl.GL11;

@AllArgsConstructor @Getter
public enum ElementType {

    FLOAT(4, GL11.GL_FLOAT, false),
    UNSIGNED_BYTE(1, GL11.GL_UNSIGNED_BYTE, true),
    ;

    private final int size;
    private final int glType;
    private final boolean normalize;
}
