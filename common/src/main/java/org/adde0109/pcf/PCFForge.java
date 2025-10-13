package org.adde0109.pcf;

import dev.neuralnexus.taterapi.meta.MetaAPI;
import dev.neuralnexus.taterapi.meta.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.platforms.TaterMetadata;

import net.minecraftforge.fml.common.Mod;

@Mod(PCF.MOD_ID)
public final class PCFForge {
    public PCFForge() {
        // Bootstrap TaterLibLite Metadata
        TaterMetadata.initForge();

        MetaAPI api = MetaAPI.instance();
        MinecraftVersion mcv = api.version();
        // spotless:off
        PCF.logger.info("Initializing Proxy Compatible Forge on "
                + "Minecraft " + mcv
                + " (" + api.platform() + " " + api.meta().apiVersion() + ")");
        // spotless:on

        PCF.instance().loadPlugins();
    }
}
