package codes.biscuit.skyblockaddons.shader;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum VertexFormatElement {

    POSITION(3, ElementType.FLOAT),
    TEX(2, ElementType.FLOAT),
    COLOR(4, ElementType.UNSIGNED_BYTE)
    ;

    private final int count;
    private final ElementType elementType;

    public int getTotalSize() {
        return count * elementType.getSize();
    }
}
