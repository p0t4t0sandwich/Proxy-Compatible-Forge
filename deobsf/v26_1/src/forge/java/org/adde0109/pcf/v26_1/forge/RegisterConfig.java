package org.adde0109.pcf.v26_1.forge;

import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.PCFInitializer;

@AConstraint(platform = Platform.FORGE, version = @Versions(min = MinecraftVersion.V17))
public final class RegisterConfig implements PCFInitializer {
    @SuppressWarnings("removal")
    @Override
    public void onInit() {
        ModLoadingContext.get()
                .registerConfig(ModConfig.Type.COMMON, Config.spec, PCF.CONFIG_FILE_NAME);
        Config.reload();
    }
}
