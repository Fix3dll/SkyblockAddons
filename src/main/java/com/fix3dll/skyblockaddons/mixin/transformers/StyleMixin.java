package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.mixin.extensions.StyleExtension;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URI;
import java.util.function.UnaryOperator;

import static com.fix3dll.skyblockaddons.core.feature.Feature.SHOW_CLICKABLE_MESSAGES_CONTENT;

@Mixin(Style.class)
public class StyleMixin implements StyleExtension {

    @Shadow @Final @Nullable ClickEvent clickEvent;
    @Mutable @Shadow @Final @Nullable HoverEvent hoverEvent;

    @Unique private boolean sba$chromaDisabled = false;

    @Unique private static final UnaryOperator<Style> sba$color = style -> SHOW_CLICKABLE_MESSAGES_CONTENT.isChroma()
            ? style.withColor(DrawUtils.CHROMA_TEXT_COLOR)
            : style.withColor(SHOW_CLICKABLE_MESSAGES_CONTENT.getColor());
    @Unique private HoverEvent sba$cachedHoverEvent = null;
    @Unique private long sba$lastUpdateTime = 0L;

    @Inject(method = "getHoverEvent", at = @At("RETURN"), cancellable = true)
    public void sba$getHoverEvent(CallbackInfoReturnable<HoverEvent> cir) {
        if (this.clickEvent == null || SHOW_CLICKABLE_MESSAGES_CONTENT.isDisabled()) return;

        long currentTime = System.currentTimeMillis();
        if (sba$cachedHoverEvent != null && currentTime - sba$lastUpdateTime < 500L) {
            cir.setReturnValue(sba$cachedHoverEvent);
            return;
        }

        Component display = switch (this.clickEvent) {
            case ClickEvent.OpenUrl(URI uri) -> Component
                    .literal(Translations.getMessage("tooltip.clickableAddress")).withStyle(sba$color)
                    .append(Component.literal(uri.toString()).withColor(ColorCode.WHITE.getColor()));
            case ClickEvent.RunCommand(String command) -> Component
                    .literal(Translations.getMessage("tooltip.clickableCommand")).withStyle(sba$color)
                    .append(Component.literal(command).withColor(ColorCode.WHITE.getColor()));
            case ClickEvent.OpenFile(String path) -> Component
                    .literal(Translations.getMessage("tooltip.clickableAddress")).withStyle(sba$color)
                    .append(Component.literal(path).withColor(ColorCode.WHITE.getColor()));
            case null, default -> null;
        };

        if (display != null) {
            if (this.hoverEvent == null) {
                cir.setReturnValue(sba$cachedHoverEvent = new HoverEvent.ShowText(display));
            } else if (this.hoverEvent instanceof HoverEvent.ShowText(Component value)) {
                cir.setReturnValue(sba$cachedHoverEvent = new HoverEvent.ShowText(value.copy().append("\n").append(display)));
            }
        }

        sba$lastUpdateTime = currentTime;
    }

    /**
     * Preserves the custom chroma flag during style inheritance and merging.
     */
    @Inject(method = "applyTo", at = @At("RETURN"))
    private void sba$preserveChromaFlagOnMerge(Style parent, CallbackInfoReturnable<Style> cir) {
        if (this.sba$chromaDisabled) {
            Style mergedStyle = cir.getReturnValue();

            // Ensure we are not mutating the global EMPTY style
            if (mergedStyle != null && mergedStyle != Style.EMPTY) {
                ((StyleExtension) (Object) mergedStyle).sba$setChromaDisabled(true);
            }
        }
    }

    @Override
    public boolean sba$isChromaDisabled() {
        return sba$chromaDisabled;
    }

    @Override
    public void sba$setChromaDisabled(boolean value) {
        this.sba$chromaDisabled = value;
    }

}