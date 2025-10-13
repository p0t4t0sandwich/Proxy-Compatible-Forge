package org.adde0109.pcf;

import dev.neuralnexus.taterapi.meta.MetaAPI;
import dev.neuralnexus.taterapi.meta.platforms.TaterMetadata;

import net.neoforged.fml.common.Mod;

@Mod(PCF.MOD_ID)
public final class PCFNeo {
    public PCFNeo() {
        // Bootstrap TaterLibLite Metadata
        TaterMetadata.initNeoForge();

        MetaAPI api = MetaAPI.instance();
        // spotless:off
        PCF.logger.info("Initializing Proxy Compatible Forge on "
                + "Minecraft " + api.version()
                + " (" + api.platform() + " " + api.meta().apiVersion() + ")");
        // spotless:on

        PCF.instance().loadPlugins();
    }
}
