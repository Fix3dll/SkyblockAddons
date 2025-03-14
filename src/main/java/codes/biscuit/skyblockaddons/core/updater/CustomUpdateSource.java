package codes.biscuit.skyblockaddons.core.updater;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.EqualsAndHashCode;
import lombok.Value;
import moe.nea.libautoupdate.JsonUpdateSource;
import moe.nea.libautoupdate.UpdateData;
import net.minecraft.util.StringUtils;

import java.util.concurrent.CompletableFuture;

@Value
@EqualsAndHashCode(callSuper = false)
public class CustomUpdateSource extends JsonUpdateSource {

    @Override
    public CompletableFuture<UpdateData> checkUpdate(String updateStream) {
        if (StringUtils.isNullOrEmpty(updateStream)) {
            throw new IllegalArgumentException("'updateStream' cannot be null or empty!");
        }

        return CompletableFuture.supplyAsync(
                () -> SkyblockAddons.getInstance().getOnlineData().getUpdateData(updateStream)
        );
    }
}