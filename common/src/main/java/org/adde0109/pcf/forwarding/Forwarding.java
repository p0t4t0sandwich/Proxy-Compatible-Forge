package org.adde0109.pcf.forwarding;

import static dev.neuralnexus.taterapi.network.chat.Component.translatable;

import dev.neuralnexus.taterapi.network.chat.ThrowingComponent;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.forwarding.legacy.LegacyForwarding;
import org.adde0109.pcf.forwarding.modern.ModernForwarding;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class Forwarding {
    private static final Object FAILED_TO_VERIFY =
            translatable("multiplayer.disconnect.unverified_username");

    /**
     * Abstract implementation of the hello packet handler
     *
     * @param slpl The ServerLoginPacketListenerImpl
     * @param ci The callback info
     */
    public static void handleHello(
            final @NonNull ServerLoginPacketListenerBridge slpl, final @NonNull CallbackInfo ci) {
        try {
            switch (PCF.instance().forwarding().mode()) {
                case LEGACY, BUNGEEGUARD -> LegacyForwarding.handleHello(slpl, ci);
                case MODERN -> ModernForwarding.handleHello(slpl, ci);
            }
        } catch (final ThrowingComponent e) {
            slpl.bridge$connection().bridge$disconnect(e.getComponent());
        } catch (final Exception e) {
            // final NameAndId nameAndId = new NameAndId(profile);
            // PCF.logger.warn("Exception while forwarding user " + nameAndId.name());
            // throw new ThrowingComponent(FAILED_TO_VERIFY, e);
            e.printStackTrace();
            slpl.bridge$connection().bridge$disconnect(FAILED_TO_VERIFY);
        } finally {
            ci.cancel();
        }
    }
}
