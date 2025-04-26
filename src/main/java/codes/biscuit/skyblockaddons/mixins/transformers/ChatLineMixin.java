package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.mixins.extensions.ChatLineExtension;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatLine.class)
public class ChatLineMixin implements ChatLineExtension {

    @Unique
    private IChatComponent sba$parentComponent = null;

    @Override
    public IChatComponent sba$getParentComponent() {
        return sba$parentComponent;
    }

    @Override
    public ChatLine sba$withParentComponent(@NotNull IChatComponent chatComponent) {
        this.sba$parentComponent = chatComponent;
        return (ChatLine) (Object) this;
    }

}