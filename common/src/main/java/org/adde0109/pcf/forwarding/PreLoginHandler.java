package org.adde0109.pcf.forwarding;

import com.mojang.authlib.GameProfile;

import dev.neuralnexus.taterapi.event.Cancellable;
import dev.neuralnexus.taterapi.mc.server.players.NameAndId;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
@FunctionalInterface
public interface PreLoginHandler {
    PreLoginHandler DEFAULT_HANDLER =
            (slpl, profile, _) -> {
                final NameAndId nameAndId = new NameAndId(profile);
                slpl.bridge$logger_info(
                        "UUID of player {} is {}", nameAndId.name(), nameAndId.id());
                slpl.bridge$startClientVerification(profile);
            };
    @ApiStatus.Internal List<PreLoginHandler> HANDLERS = new ArrayList<>(List.of(DEFAULT_HANDLER));

    /**
     * Process the forwarded profile
     *
     * @param slpl the ServerLoginPacketListener
     * @param profile the forwarded GameProfile
     * @param c the cancellable wrapper
     * @throws Exception if an error occurs
     */
    void process(
            final @NonNull ServerLoginPacketListenerBridge slpl,
            final @NonNull GameProfile profile,
            final @NonNull Cancellable c)
            throws Exception;
}
