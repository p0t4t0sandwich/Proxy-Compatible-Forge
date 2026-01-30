package org.adde0109.pcf.forwarding.compat;

import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * <a
 * href="https://github.com/SpongePowered/Sponge/blob/api-9/src/mixins/java/org/spongepowered/common/mixin/core/server/network/ServerLoginPacketListenerImplMixin.java">Used
 * for SpongeForge 1.16.5 and 1.18.2</a>
 */
public interface SpongePreLogin {
    class API8 {
        private static MethodHandle fireAuthEvent;

        public static boolean fireAuthEvent(final @NotNull ServerLoginPacketListenerBridge slpl)
                throws Exception {
            if (fireAuthEvent == null) {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final MethodType rType = MethodType.methodType(boolean.class);
                fireAuthEvent = lookup.findVirtual(slpl.getClass(), "bridge$fireAuthEvent", rType);
            }

            try {
                return (boolean) fireAuthEvent.invoke(slpl);
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
