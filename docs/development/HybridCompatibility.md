# Hybrid Compatibility

## Arclight

Versions: 1.14 - 1.20.1 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl` <br>
Method: `arclight$preLogin()` <br>
Source: <https://github.com/IzzelAliz/Arclight/blob/Trials/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginNetHandlerMixin.java>

Version: 1.20.2 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl` <br>
Method: `arclight$preLogin(GameProfile)` <br>
Source: <https://github.com/IzzelAliz/Arclight/blob/Net/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginNetHandlerMixin.java>

Versions: 1.20.3 - 1.20.4 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl` <br>
Method: `bridge$preLogin(GameProfile);` <br>
Source: <https://github.com/IzzelAliz/Arclight/blob/Whisper/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginNetHandlerMixin.java>

Versions: 1.20.5 and above <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl` <br>
Method: `bridge$preLogin(GameProfile)` <br>
Source: <https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mixin/core/network/ServerLoginPacketListenerImplMixin.java>
Notes: Alternatively call `private void callPlayerPreLoginEvents(GameProfile)` and use PCF logic

## CatServer

Versions: 1.12.2, 1.16.5, 1.18.2 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl$LoginHandler` <br>
Method: `fireEvents()` <br>
Sources:
- 1.12.2: <https://github.com/Luohuayu/CatServer/blob/1.12.2/patches/net/minecraft/server/network/NetHandlerLoginServer.java.patch>
- 1.16.5: <https://github.com/Luohuayu/CatServer/blob/1.16.5/patches/minecraft/net/minecraft/network/login/ServerLoginNetHandler.java.patch>
- 1.18.2: <https://github.com/Luohuayu/CatServer/blob/1.18.2/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

## Ketting

Version: 1.20.1 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl$LoginHandler` <br>
Method: `fireEvents()` <br>
Source: <https://github.com/kettingpowered/Ketting-1-20-x/blob/1.20.1/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

Versions: 1.20.2 - 1.20.4 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl$LoginHandler` <br>
Method: `fireEvents(GameProfile)` <br>
Source: <https://github.com/kettingpowered/Ketting-1-20-x/blob/1.20.4/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

## Magma

Versions: 1.12.2, 1.16.5, 1.18.2, 1.19.3, 1.20.1 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl$LoginHandler` <br>
Method: `fireEvents()` <br>
Sources:
- 1.12.2: <https://github.com/magmamaintained/Magma-1.12.2/blob/master/patches/minecraft/net/minecraft/server/network/NetHandlerLoginServer.java.patch>
- 1.16.5: No (direct) URL available
- 1.18.2 - 1.20.1: <https://github.com/magmamaintained/Magma-1.20.1/blob/master/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

### Magma Neo

Version: 1.21.1 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl` <br>
Method: `callPlayerPreLoginEvents(GameProfile)` <br>
Source: <https://github.com/magmafoundation/Magma-Neo/blob/1.21.x/patches/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

## Mohist

Version: 1.7.10 <br>
Class: `net.minecraft.network.NetHandlerLoginServer` <br>
Method: `this.field_147327_f.getConfigurationManager().attemptLogin(this, this.field_147337_i, this.hostname)` <br>
Source: <https://github.com/Teneted/Tenet/blob/1.7.10/patches/net/minecraft/server/network/NetHandlerLoginServer.java.patch> <br>
Note: Implement later after researching Cauldron

Versions: 1.12.2, 1.16.5, 1.18.2, 1.19.2, 1.19.4 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl$LoginHandler` <br>
Method: `fireEvents()` <br>
Sources:
- 1.12.2: <https://github.com/Teneted/Tenet/blob/1.12.2/patches/minecraft/net/minecraft/server/network/NetHandlerLoginServer.java.patch>
- 1.16.5: <https://github.com/Teneted/Tenet/blob/1.16.5/patches/minecraft/net/minecraft/network/login/ServerLoginNetHandler.java.patch>
- 1.18.2 - 1.19.4: <https://github.com/Teneted/Tenet/blob/1.19.4/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

Version: 1.20.1 <br>
Class: `com.mohistmc.bukkit.LoginHandler` <br>
Method: `fireEvents(ServerLoginPacketListenerImpl)` <br>
Source: <https://github.com/Teneted/Tenet/blob/1.20.1/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

Versions: 1.20.2 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl$LoginHandler` <br>
Method: `fireEvents(GameProfile)` <br>
Source: <https://github.com/Teneted/Tenet/blob/1.20.2/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

Versions: 1.21.1, 1.21.4 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl` <br>
Method: `callPlayerPreLoginEvents(GameProfile)` <br>
Source: <https://github.com/Teneted/Tenet/blob/1.21.4/patches/minecraft/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

### Youer

Note: No longer owned by Mohist, copyright was sold

Versions: 1.21.1 <br>
Class: `com.mohistmc.youer.bukkit.LoginHandler` <br>
Method: `fireEvents(ServerLoginPacketListenerImpl, GameProfile)` <br>
Source: <https://github.com/MohistMC/Youer/blob/1.21.1/patches/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

Versions: 1.21.11 - 26.1 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl` <br>
Method: `callPlayerPreLoginEvents(GameProfile)` <br>
Source: <https://github.com/MohistMC/Youer/blob/26.1/nms-patches/net/minecraft/server/network/ServerLoginPacketListenerImpl.patch>

### NeoTenet

Note: Mohist's new NeoForge implementation

Versions: 1.21.1, 1.21.10 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl` <br>
Method: `callPlayerPreLoginEvents(GameProfile)` <br>
Source: <https://github.com/Teneted/NeoTenet/blob/1.21.10/paper-patches/sources/net/minecraft/server/network/ServerLoginPacketListenerImpl.java.patch>

## Sponge

Versions: 1.16.5, 1.18.2 <br>
Class: `net.minecraft.server.network.ServerLoginPacketListenerImpl` <br>
Method:
```java
if (this.bridge$fireAuthEvent()) {
    ci.cancel();
}
```
Sources:
- API 8: <https://github.com/SpongePowered/Sponge/blob/api-8/src/mixins/java/org/spongepowered/common/mixin/core/server/network/ServerLoginPacketListenerImplMixin.java>
- API 9: <https://github.com/SpongePowered/Sponge/blob/api-9/src/mixins/java/org/spongepowered/common/mixin/core/server/network/ServerLoginPacketListenerImplMixin.java>
