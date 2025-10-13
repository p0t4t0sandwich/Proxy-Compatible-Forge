package org.adde0109.pcf;

import dev.neuralnexus.taterapi.loader.EntrypointLoader;
import dev.neuralnexus.taterapi.logger.Logger;
import dev.neuralnexus.taterapi.meta.Constraint;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PCF {
    private PCF() {}

    public static final String MOD_ID = "pcf";

    private static final PCF INSTANCE = new PCF();
    public static final Logger logger = Logger.create(MOD_ID);

    public static PCF instance() {
        return INSTANCE;
    }

    private final EntrypointLoader<PCFInitializer> loader =
            new EntrypointLoader<>(PCFInitializer.class, Logger.create(MOD_ID));

    void loadPlugins() {
        Constraint.Evaluator.DEBUG = true;
        this.loader.load();
        this.loader.onInit();
    }

    private String forwardingSecret;
    private final List<String> MODDED_ARGUMENT_TYPES = new ArrayList<>();

    public String forwardingSecret() {
        return this.forwardingSecret;
    }

    @ApiStatus.Internal
    public void setForwardingSecret(String secret) {
        this.forwardingSecret = secret;
    }

    public List<String> moddedArgumentTypes() {
        return this.MODDED_ARGUMENT_TYPES;
    }

    @ApiStatus.Internal
    public void addModdedArgumentTypes(Collection<String> types) {
        this.MODDED_ARGUMENT_TYPES.addAll(types);
    }
}
