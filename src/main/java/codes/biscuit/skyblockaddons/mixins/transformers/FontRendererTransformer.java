package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.hooks.FontRendererHook;
import lombok.Getter;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @see <a href="https://github.com/hannibal002/SkyHanni/blob/16ef943b3c2ce8db2331332261143a12bdba61cf/src/main/java/at/hannibal2/skyhanni/mixins/transformers/MixinFontRenderer.java">SkyHanni SBA Chroma port</a>
 */
@Mixin(FontRenderer.class)
public abstract class FontRendererTransformer {

    @Shadow
    protected abstract void resetStyles();

    @Unique @Getter
    private static boolean sba$initialized = false;

    @Inject(method = "renderChar", at = @At("HEAD"))
    public void turnAllTextsChroma(char ch, boolean italic, CallbackInfoReturnable<Float> cir) {
        if (sba$initialized) {
            FontRendererHook.turnAllTextsChroma();
        }
    }

    /**
     * Modify color code constant to add Z color code
     */
    @ModifyConstant(method = "renderStringAtPos", constant = @Constant(stringValue = "0123456789abcdefklmnor"))
    public String insertZColorCode(String constant) {
        sba$initialized = true;
        FontRendererHook.onFontRendererInitalized();
        return "0123456789abcdefklmnorz";
    }

    /**
     * Inject call to {@link FontRendererHook#restoreChromaState()} after 1st and 3rd fontrenderer.italicStyle = ___ call
     */
    @Inject(method = "renderStringAtPos", at = {
            @At(value = "FIELD", opcode = 181, target = "Lnet/minecraft/client/gui/FontRenderer;italicStyle:Z", ordinal = 0, shift = At.Shift.AFTER),
            @At(value = "FIELD", opcode = 181, target = "Lnet/minecraft/client/gui/FontRenderer;italicStyle:Z", ordinal = 2, shift = At.Shift.AFTER)})
    public void insertRestoreChromaState(CallbackInfo ci) {
        if (sba$initialized) {
            FontRendererHook.restoreChromaState();
        }
    }

    /**
     * Inject call to {@link FontRendererHook#toggleChromaOn(int i1)} to check for Z color code index and if so,
     * reset styles and toggle chroma on
     */
    @Inject(method = "renderStringAtPos", at = @At(value = "INVOKE", target = "Ljava/lang/String;indexOf(I)I", ordinal = 0, shift = At.Shift.BY, by = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    public void toggleChromaCondition(String text, boolean shadow, CallbackInfo ci, int i, char c0, int i1) {
        if (sba$initialized && FontRendererHook.toggleChromaOn(i1)) {
            this.resetStyles();
        }
    }

    /**
     * Inject call to {@link FontRendererHook#endRenderString()} to turn off chroma rendering after entire
     * string has been rendered
     */
    @Inject(method = "renderStringAtPos", at = @At("RETURN"))
    public void insertEndOfString(String text, boolean shadow, CallbackInfo ci) {
        if (sba$initialized) {
            FontRendererHook.endRenderString();
        }
    }

    /**
     * Inject call to {@link FontRendererHook#beginRenderString(boolean)} as first call
     */
    @Inject(method = "renderStringAtPos", at = @At("HEAD"))
    public void beginRenderString(String text, boolean shadow, CallbackInfo ci) {
        if (sba$initialized) {
            FontRendererHook.beginRenderString(shadow);
        }
    }

}
