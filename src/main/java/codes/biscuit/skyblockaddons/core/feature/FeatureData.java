package codes.biscuit.skyblockaddons.core.feature;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumUtils.AnchorPoint;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import codes.biscuit.skyblockaddons.utils.objects.RegistrableEnum;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NonNull;
import org.apache.logging.log4j.Logger;

import java.util.TreeMap;

@Data
public class FeatureData<T> {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    // TODO next step of migration. move FeatureGuiData to FeatureData
    private final FeatureGuiData guiData;

    @SerializedName("value")
    private T value;
    @SerializedName("anchorPoint")
    private AnchorPoint anchorPoint;
    @SerializedName("coords")
    private Pair<Float, Float> coords;
    @SerializedName("barSizes")
    @NonNull private Pair<Float, Float> barSizes = new Pair<>(1.0F, 1.0F);
    @SerializedName("guiScale")
    private float guiScale = 1.0F;
    @SerializedName("color")
    private int color = ColorCode.RED.getColor();
    @SerializedName("chroma")
    private boolean chroma = false;
    @SerializedName("settings")
    private TreeMap<FeatureSetting, Object> settings = null;

    public void overwriteData(FeatureData<?> guiData) {
        //noinspection unchecked
        this.value = (T) guiData.getValue();
        this.anchorPoint = guiData.getAnchorPoint();
        this.coords = guiData.getCoords();
        this.barSizes = guiData.getBarSizes();
        this.guiScale = guiData.getGuiScale();
        this.color = guiData.getColor();
        this.chroma = guiData.isChroma();
        this.settings = guiData.getSettings();
    }

    public void setValue(Object value) {
        //noinspection unchecked
        this.value = (T) value;
    }

    public static boolean isValidValue(Object value) {
        return value instanceof Boolean ||
                value instanceof Number ||
                value instanceof RegistrableEnum ||
                value instanceof String;
    }

    public void setCoords(float x, float y) {
        if (coords != null) {
            this.coords.setLeft(x);
            this.coords.setRight(y);
        } else {
            this.coords = new Pair<>(x, y);
        }
    }

    public float getSizesX() {
        return Math.min(Math.max(this.barSizes.getLeft(), .25F), 1);
    }

    public float getSizesY() {
        return Math.min(Math.max(this.barSizes.getRight(), .25F), 1);
    }

}