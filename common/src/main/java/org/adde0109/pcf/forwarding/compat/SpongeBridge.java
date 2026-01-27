package org.adde0109.pcf.forwarding.compat;

/**
 * <a
 * href="https://github.com/SpongePowered/Sponge/blob/api-9/src/mixins/java/org/spongepowered/common/mixin/core/server/network/ServerLoginPacketListenerImplMixin.java">Used
 * for SpongeForge 1.16.5 and 1.18.2</a>
 */
public interface SpongeBridge {
    boolean bridge$fireAuthEvent();
}
