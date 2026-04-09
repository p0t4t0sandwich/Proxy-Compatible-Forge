package org.adde0109.pcf.forwarding.compatibility.prelogin;

import com.mojang.authlib.GameProfile;

import org.adde0109.pcf.forwarding.modern.ServerLoginPacketListenerBridge;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public interface ArclightPreLogin {
    /**
     * <a
     * href="https://github.com/IzzelAliz/Arclight/blob/Trials/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginNetHandlerMixin.java">Used
     * for Arclight 1.14 - 1.20.1</a>
     */
    final class V14 {
        private static MethodHandle preLogin;

        public static void preLogin(final @NonNull ServerLoginPacketListenerBridge slpl)
                throws Exception {
            if (preLogin == null) {
                final MethodHandles.Lookup lookup =
                        MethodHandles.privateLookupIn(slpl.getClass(), MethodHandles.lookup());
                final MethodType methodType = MethodType.methodType(void.class);
                preLogin = lookup.findVirtual(slpl.getClass(), "arclight$preLogin", methodType);
            }

            try {
                preLogin.invoke(slpl);
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <a
     * href="https://github.com/IzzelAliz/Arclight/blob/Net/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginNetHandlerMixin.java">Used
     * for Arclight 1.20.2 - 1.20.3</a>
     */
    final class V20_2 {
        private static MethodHandle preLogin;

        public static void preLogin(
                final @NonNull ServerLoginPacketListenerBridge slpl,
                final @NonNull GameProfile profile)
                throws Exception {
            if (preLogin == null) {
                final MethodHandles.Lookup lookup =
                        MethodHandles.privateLookupIn(slpl.getClass(), MethodHandles.lookup());
                final MethodType methodType = MethodType.methodType(void.class, GameProfile.class);
                preLogin = lookup.findVirtual(slpl.getClass(), "arclight$preLogin", methodType);
            }

            try {
                preLogin.invoke(slpl, profile);
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <a
     * href="https://github.com/IzzelAliz/Arclight/blob/Whisper/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginNetHandlerMixin.java">Used
     * for Arclight 1.20.4+.</a>
     */
    final class V20_4 {
        private static MethodHandle preLogin;

        public static void preLogin(
                final @NonNull ServerLoginPacketListenerBridge slpl,
                final @NonNull GameProfile profile)
                throws Exception {
            if (preLogin == null) {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final MethodType methodType = MethodType.methodType(void.class, GameProfile.class);
                preLogin = lookup.findVirtual(slpl.getClass(), "bridge$preLogin", methodType);
            }

            try {
                preLogin.invoke(slpl, profile);
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
