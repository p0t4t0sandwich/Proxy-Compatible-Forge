package org.adde0109.pcf.forwarding;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import dev.neuralnexus.taterapi.meta.Constraint;
import dev.neuralnexus.taterapi.meta.MetaAPI;
import dev.neuralnexus.taterapi.meta.MinecraftVersions;

import net.minecraft.server.MinecraftServer;

import org.adde0109.pcf.PCF;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class ReflectionUtils {
    private ReflectionUtils() {}

    private static final MethodHandle profilePropertiesHandle;
    private static final MethodHandle profilePropertyNameHandle;
    private static final MethodHandle profilePropertyValueHandle;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        // com.mojang:authlib:5.0.0 or newer
        final String profilePropertyNameString;
        final String profilePropertyValueString;
        if (Constraint.noLessThan(MinecraftVersions.V20_2).result()) {
            profilePropertyNameString = "name";
            profilePropertyValueString = "value";
        } else {
            profilePropertyNameString = "getName";
            profilePropertyValueString = "getValue";
        }
        try {
            profilePropertyNameHandle =
                    lookup.findVirtual(
                            Property.class,
                            profilePropertyNameString,
                            MethodType.methodType(String.class));
            profilePropertyValueHandle =
                    lookup.findVirtual(
                            Property.class,
                            profilePropertyValueString,
                            MethodType.methodType(String.class));
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to initialize GameProfile method handles", e);
        }

        // com.mojang:authlib:7.0.0 or newer
        if (Constraint.noLessThan(MinecraftVersions.V21_9).result()) {
            profilePropertiesHandle = null;
        } else {
            try {
                //noinspection JavaLangInvokeHandleSignature
                profilePropertiesHandle =
                        lookup.findVirtual(
                                GameProfile.class,
                                "getProperties",
                                MethodType.methodType(PropertyMap.class));
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                throw new IllegalStateException(
                        "Failed to initialize GameProfile method handles", e);
            }
        }
    }

    /**
     * Gets the name of a Property
     *
     * @param property the property
     * @return the name
     */
    public static @NonNull String getName(final @NonNull Property property) {
        try {
            return (String) profilePropertyNameHandle.invokeExact(property);
        } catch (final Throwable e) {
            throw new IllegalStateException("Failed to get name from Property", e);
        }
    }

    /**
     * Gets the value of a Property
     *
     * @param property the property
     * @return the value
     */
    public static @NonNull String getValue(final @NonNull Property property) {
        try {
            return (String) profilePropertyValueHandle.invokeExact(property);
        } catch (final Throwable e) {
            throw new IllegalStateException("Failed to get value from Property", e);
        }
    }

    /**
     * Gets the properties from the given GameProfile - 1.21.8 and older
     *
     * @param profile the profile
     * @return the properties
     */
    public static @NonNull PropertyMap getProperties(final @NonNull GameProfile profile) {
        try {
            return (PropertyMap) profilePropertiesHandle.invokeExact(profile);
        } catch (final Throwable e) {
            throw new IllegalStateException("Failed to get properties from GameProfile", e);
        }
    }

    private static final MethodHandle ENFORCE_SECURE_PROFILE;

    static {
        MethodHandle enforceSecureProfileHandle = null;
        if (Constraint.range(MinecraftVersions.V19, MinecraftVersions.V19_2).result()) {
            try {
                Class<MinecraftServer> minecraftServerClass = MinecraftServer.class;
                //noinspection JavaLangInvokeHandleSignature
                enforceSecureProfileHandle =
                        MethodHandles.lookup()
                                .findVirtual(
                                        minecraftServerClass,
                                        "m_214005_", // enforceSecureProfile
                                        MethodType.methodType(boolean.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                PCF.logger.error(
                        "Failed to get MethodHandle for MinecraftServer.enforceSecureProfile", e);
            }
        }
        ENFORCE_SECURE_PROFILE = enforceSecureProfileHandle;
    }

    public static boolean enforceSecureProfile() {
        if (ENFORCE_SECURE_PROFILE == null) {
            return false;
        }
        try {
            return (boolean)
                    ENFORCE_SECURE_PROFILE.invokeExact(
                            (MinecraftServer) MetaAPI.instance().server());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
