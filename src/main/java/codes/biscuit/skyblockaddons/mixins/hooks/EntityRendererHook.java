package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;

public class EntityRendererHook {

    public static void onGetNightVisionBrightness(ReturnValue<Float> returnValue) {
        if (Feature.AVOID_BLINKING_NIGHT_VISION.isEnabled()) {
            returnValue.cancel(1.0F);
        }
    }
}
