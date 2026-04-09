package org.adde0109.pcf.v26_1.forge;

import static org.adde0109.pcf.v17_1.forge.PCFBootstrap.IGNORE_SERVER_VERSION;

import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.AConstraints;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;

import net.minecraftforge.fml.IExtensionPoint;

import org.adde0109.pcf.PCFInitializer;

@AConstraints({
    @AConstraint(platform = Platform.FORGE, version = @Versions(min = MinecraftVersion.V18_2)),
    @AConstraint( // TODO: Filter based on Forge version
            platform = {Platform.ARCLIGHT, Platform.MAGMA, Platform.MOHIST},
            version = @Versions(value = {MinecraftVersion.V18_2, MinecraftVersion.V19_4}),
            invert = true),
    @AConstraint(
            platform = Platform.KETTING,
            version = @Versions(MinecraftVersion.V20_4),
            invert = true),
    @AConstraint(
            platform = {Platform.MAGMA, Platform.MOHIST},
            version = @Versions(MinecraftVersion.V20_1),
            invert = true),
    @AConstraint(
            platform = Platform.GOLDENFORGE,
            version = @Versions(MinecraftVersion.V19_2),
            invert = true),
    @AConstraint(
            version =
                    @Versions({
                        MinecraftVersion.V19,
                        MinecraftVersion.V19_1,
                        MinecraftVersion.V19_3,
                        MinecraftVersion.V20_2
                    }),
            invert = true)
})
public final class DisplayTestInit implements PCFInitializer {
    public DisplayTestInit() {
        // IGNORE_SERVER_ONLY = () -> NetworkConstants.IGNORESERVERONLY;
        IGNORE_SERVER_VERSION = IExtensionPoint.DisplayTest.IGNORE_SERVER_VERSION;
    }

    @Override
    public void onInit() {}
}
