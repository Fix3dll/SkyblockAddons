package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

@Pseudo
@Mixin(targets = "com.fix3dll.skyblockaddons.libautoupdate.UpdateUtils", remap = false)
public class libautoupdateMixin {

    @Unique
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    /**
     * @author Fix3dll (SBA)
     * @reason path error
     */
    @Overwrite
    public static File getJarFileContainingClass(Class<?> clazz) {
        LOGGER.warn("Please share this on issue #116: {}", clazz.getProtectionDomain().getCodeSource().getLocation());
        val location = clazz.getProtectionDomain().getCodeSource().getLocation();
        if (location == null)
            return null;
        var path = location.toString();
        path = path.split("!", 2)[0];
        if (path.startsWith("jar:")) {
            path = path.substring(4);
        }
        try {
            URI uri = new URI(path);
            return new File(uri.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

}