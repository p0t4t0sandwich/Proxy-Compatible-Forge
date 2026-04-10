package org.adde0109.pcf.v16_5.forge;

import dev.neuralnexus.taterapi.meta.Mappings;
import dev.neuralnexus.taterapi.meta.anno.AConstraint;
import dev.neuralnexus.taterapi.meta.anno.Versions;
import dev.neuralnexus.taterapi.meta.enums.MinecraftVersion;
import dev.neuralnexus.taterapi.meta.enums.Platform;
import dev.neuralnexus.taterapi.network.NetworkRegistry;

import org.adde0109.pcf.PCFInitializer;
import org.adde0109.pcf.v16_5.forge.forwarding.network.CCustomQueryPacketAdapter;
import org.adde0109.pcf.v16_5.forge.forwarding.network.SCustomQueryAnswerPacketAdapter;

@AConstraint(
        mappings = Mappings.LEGACY_SEARGE,
        platform = Platform.FORGE,
        version = @Versions(min = MinecraftVersion.V14, max = MinecraftVersion.V16_5))
public final class AdapterRegistryInit implements PCFInitializer {
    public AdapterRegistryInit() {
        NetworkRegistry.registerAdapter(
                CCustomQueryPacketAdapter.INSTANCE, SCustomQueryAnswerPacketAdapter.INSTANCE);
    }

    @Override
    public void onInit() {}
}
