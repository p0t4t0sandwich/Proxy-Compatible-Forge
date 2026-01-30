package org.adde0109.pcf.forwarding.compat;

import com.mojang.authlib.GameProfile;

import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public interface MohistPreLogin {
    /**
     * <a
     * href="https://github.com/Teneted/Tenet/blob/1.20.1/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Used
     * in Mohist 1.20.1</a>
     */
    class V20_1 {
        private static MethodHandle loginHandler;
        private static MethodHandle fireEvents;

        public static void fireEvents(final @NonNull ServerLoginPacketListenerBridge slpl)
                throws Exception {
            if (loginHandler == null || fireEvents == null) {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();

                final Class<?> clazz = Class.forName("com.mohistmc.bukkit.LoginHandler");
                final MethodType cType = MethodType.methodType(void.class);
                loginHandler = lookup.findConstructor(clazz, cType);

                final MethodType methodType = MethodType.methodType(void.class, slpl.getClass());
                fireEvents = lookup.findVirtual(clazz, "fireEvents", methodType);
            }

            try {
                fireEvents.invoke(loginHandler.invoke(), slpl);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <a
     * href="https://github.com/MohistMC/Youer/blob/1.21.1/patches/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Used
     * in Youer 1.21.1</a>
     */
    class Youer {
        private static MethodHandle loginHandler;
        private static MethodHandle fireEvents;

        public static void fireEvents(
                final @NonNull ServerLoginPacketListenerBridge slpl,
                final @NonNull GameProfile profile)
                throws Exception {
            if (loginHandler == null || fireEvents == null) {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();

                final Class<?> clazz = Class.forName("com.mohistmc.youer.bukkit.LoginHandler");
                final MethodType cType = MethodType.methodType(void.class);
                loginHandler = lookup.findConstructor(clazz, cType);

                final MethodType methodType =
                        MethodType.methodType(void.class, slpl.getClass(), GameProfile.class);
                fireEvents = lookup.findVirtual(clazz, "fireEvents", methodType);
            }

            try {
                fireEvents.invoke(loginHandler.invoke(), slpl, profile);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
