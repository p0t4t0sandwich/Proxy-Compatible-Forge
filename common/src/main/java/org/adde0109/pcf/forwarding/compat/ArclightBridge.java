package org.adde0109.pcf.forwarding.compat;

import com.mojang.authlib.GameProfile;

public interface ArclightBridge {
    /**
     * <a
     * href="https://github.com/IzzelAliz/Arclight/blob/Trials/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginNetHandlerMixin.java">Used
     * in Arclight 1.14 - 1.20.1</a>
     */
    interface V1 {
        void arclight$preLogin() throws Exception;
    }

    /**
     * <a
     * href="https://github.com/IzzelAliz/Arclight/blob/Net/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginNetHandlerMixin.java">Used
     * in Arclight 1.20.2 - 1.20.3</a>
     */
    interface V2 {
        void arclight$preLogin(GameProfile profile) throws Exception;
    }

    /**
     * <a
     * href="https://github.com/IzzelAliz/Arclight/blob/Whisper/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginNetHandlerMixin.java">Used
     * in Arclight 1.20.4+.</a> <br>
     * {@link SpigotLoginHandler.V2#callPlayerPreLoginEvents(GameProfile)} would be used, but it is
     * private in <a
     * href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginPacketListenerImplMixin.java">Arclight
     * 1.20.5+.</a>
     */
    interface V3 {
        void bridge$preLogin(GameProfile profile) throws Exception;
    }
}
