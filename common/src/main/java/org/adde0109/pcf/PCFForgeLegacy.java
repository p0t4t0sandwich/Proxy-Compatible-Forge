package org.adde0109.pcf;

import cpw.mods.fml.common.Mod;

@Mod(modid = PCF.MOD_ID, name = PCF.MOD_NAME, acceptableRemoteVersions = "*", useMetadata = true)
public final class PCFForgeLegacy {
    public PCFForgeLegacy() {
        PCF.instance().onInit();
    }
}
