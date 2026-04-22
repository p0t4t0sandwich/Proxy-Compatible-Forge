package org.adde0109.pcf.forwarding.legacy;

import com.mojang.authlib.properties.Property;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

public interface ConnectionBridgeLegacy {
    UUID bridge$spoofedUUID();

    void bridge$spoofedUUID(final @NonNull UUID uuid);

    Property[] bridge$spoofedProfile();

    void bridge$spoofedProfile(final @NonNull Property[] properties);
}
