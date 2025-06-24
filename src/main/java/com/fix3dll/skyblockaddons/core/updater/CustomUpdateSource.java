package com.fix3dll.skyblockaddons.core.updater;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import lombok.EqualsAndHashCode;
import lombok.Value;
import moe.nea.libautoupdate.JsonUpdateSource;
import moe.nea.libautoupdate.UpdateData;
import net.minecraft.util.StringUtil;

import java.util.concurrent.CompletableFuture;

@Value
@EqualsAndHashCode(callSuper = false)
public class CustomUpdateSource extends JsonUpdateSource {

    @Override
    public CompletableFuture<UpdateData> checkUpdate(String updateStream) {
        if (StringUtil.isNullOrEmpty(updateStream)) {
            throw new IllegalArgumentException("'updateStream' cannot be null or empty!");
        }

        return CompletableFuture.supplyAsync(
                () -> SkyblockAddons.getInstance().getOnlineData().getUpdateData(updateStream)
        );
    }
}