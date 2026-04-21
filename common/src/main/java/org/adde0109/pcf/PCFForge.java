package org.adde0109.pcf;

import net.minecraftforge.fml.common.Mod;

@Mod(
        value = PCF.MOD_ID,
        modid = PCF.MOD_ID,
        name = PCF.MOD_NAME,
        version = PCF.VERSION,
        useMetadata = true,
        serverSideOnly = true,
        acceptableRemoteVersions = "*")
public final class PCFForge {
    public PCFForge() {
        PCF.instance().onInit();
    }
}
