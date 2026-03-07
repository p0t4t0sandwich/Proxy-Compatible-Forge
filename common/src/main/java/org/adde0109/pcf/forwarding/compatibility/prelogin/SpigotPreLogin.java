package org.adde0109.pcf.forwarding.compatibility.prelogin;

import com.mojang.authlib.GameProfile;

import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public interface SpigotPreLogin {
    /**
     * Used for: <br>
     * <a
     * href="https://github.com/kettingpowered/Ketting-1-20-x/blob/1.20.1/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Ketting
     * 1.20.1</a> <br>
     * <a
     * href="https://github.com/magmamaintained/Magma-1.20.1/blob/master/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Magma
     * 1.12.2, 1.16.5, 1.18.2, 1.19.3, 1.20.1</a> <br>
     * <a
     * href="https://github.com/Teneted/Tenet/blob/1.19.4/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Mohist
     * 1.12.2, 1.16.5, 1.18.2, 1.19.2, 1.19.4</a>
     */
    final class Legacy {
        private static MethodHandle loginHandler;
        private static MethodHandle fireEvents;

        public static void fireEvents(final @NonNull ServerLoginPacketListenerBridge slpl)
                throws Exception {
            if (loginHandler == null || fireEvents == null) {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();

                final Class<?> clazz = Class.forName(slpl.getClass().getName() + "$LoginHandler");
                final MethodType cType = MethodType.methodType(void.class, slpl.getClass());
                loginHandler = lookup.findConstructor(clazz, cType);

                MethodType methodType = MethodType.methodType(void.class);
                fireEvents = lookup.findVirtual(clazz, "fireEvents", methodType);
            }

            try {
                fireEvents.invoke(loginHandler.invoke(slpl));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Used for: <br>
     * <a
     * href="https://github.com/kettingpowered/Ketting-1-20-x/blob/1.20.4/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Ketting
     * 1.20.2 - 1.20.4</a> <br>
     * <a
     * href="https://github.com/Teneted/Tenet/blob/1.20.2/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Mohist
     * 1.20.2</a>
     */
    final class V20_2 {
        private static MethodHandle loginHandler;
        private static MethodHandle fireEvents;

        public static void fireEvents(
                final @NonNull ServerLoginPacketListenerBridge slpl,
                final @NonNull GameProfile profile)
                throws Exception {
            if (loginHandler == null || fireEvents == null) {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();

                final Class<?> clazz = Class.forName(slpl.getClass().getName() + "$LoginHandler");
                final MethodType cType = MethodType.methodType(void.class, slpl.getClass());
                loginHandler = lookup.findConstructor(clazz, cType);

                final MethodType methodType = MethodType.methodType(void.class, GameProfile.class);
                fireEvents = lookup.findVirtual(clazz, "fireEvents", methodType);
            }
            try {
                fireEvents.invoke(loginHandler.invoke(slpl), profile);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Used for: <br>
     * <a
     * href="https://github.com/magmafoundation/Magma-Neo/blob/1.21.x/patches/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Magma
     * Neo 1.21.1</a> <br>
     * <a
     * href="https://github.com/Teneted/Tenet/blob/1.21.4/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">Mohist
     * 1.21.1, 1.21.4</a> <br>
     * <a
     * href="https://github.com/MohistMC/Youer/blob/26.1/nms-patches/net/minecraft/server/network/ServerLoginPacketListenerImpl.patch">Youer
     * 1.21.11 - 26.1</a> <br>
     * <a
     * href="https://github.com/Teneted/NeoTenet/blob/1.21.10/paper-patches/sources/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch">NeoTenet
     * 1.21.1, 1.21.10</a>
     */
    final class V20_5 {
        private static MethodHandle callPlayerPreLoginEvents;

        public static void callPlayerPreLoginEvents(
                final @NonNull ServerLoginPacketListenerBridge slpl,
                final @NonNull GameProfile profile)
                throws Exception {
            if (callPlayerPreLoginEvents == null) {
                final MethodHandles.Lookup lookup =
                        MethodHandles.privateLookupIn(slpl.getClass(), MethodHandles.lookup());
                final MethodType methodType = MethodType.methodType(void.class, GameProfile.class);
                callPlayerPreLoginEvents =
                        lookup.findVirtual(slpl.getClass(), "callPlayerPreLoginEvents", methodType);
            }

            try {
                callPlayerPreLoginEvents.invoke(slpl, profile);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
