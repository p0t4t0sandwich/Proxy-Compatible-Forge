package org.adde0109.pcf.v1_14_4.forge;

import net.minecraftforge.common.ForgeConfigSpec;

import org.adde0109.pcf.PCF;
import org.adde0109.pcf.common.ModernForwarding;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Config {
    public static final Config config;
    public static final ForgeConfigSpec spec;

    static {
        final Pair<Config, ForgeConfigSpec> specPair =
                new ForgeConfigSpec.Builder().configure(Config::new);
        spec = specPair.getRight();
        config = specPair.getLeft();
    }

    public final ForgeConfigSpec.ConfigValue<? extends String> forwardingSecret;
    public final ForgeConfigSpec.ConfigValue<? extends List<String>> moddedArgumentTypes;

    Config(ForgeConfigSpec.Builder builder) {
        builder.comment("Modern Forwarding Settings").push("modernForwarding");
        forwardingSecret = builder.define("forwardingSecret", "");

        builder.comment(
                        "List of argument types that are not vanilla but are integrated into the server (found in the Vanilla registry)")
                .push("integratedArgumentTypes");
        moddedArgumentTypes =
                builder.define("moddedArgumentTypes", List.of("livingthings:sampler_types"));

        builder.pop();
    }

    public static void setupForwarding() {
        String forwardingSecret = Config.config.forwardingSecret.get();
        if (!(forwardingSecret.isBlank() || forwardingSecret.isEmpty())) {
            PCF.modernForwarding = new ModernForwarding(forwardingSecret);
        }
    }

    public static void setupModdedArgumentTypes() {
        List<String> moddedArgumentTypes = Config.config.moddedArgumentTypes.get();
        PCF.moddedArgumentTypes.addAll(moddedArgumentTypes);
    }
}
