package org.adde0109.pcf;


import com.google.gson.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Mod(value = "pcf")//, dist = Dist.DEDICATED_SERVER)
public class Initializer {

  public static ModernForwarding modernForwardingInstance;
  public static final List<String> integratedArgumentTypes = new ArrayList<>();

  public static final Config config;

  public Initializer() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configSpec);

    NeoForge.EVENT_BUS.addListener(this::serverAboutToStart);
    try (Reader reader = new InputStreamReader(Objects.requireNonNull(this.getClass()
            .getResourceAsStream("/integrated_argument_types.json")))) {
      JsonObject result = new Gson().fromJson(reader, JsonObject.class);
      result.get("entries").getAsJsonArray().iterator().forEachRemaining((k) -> integratedArgumentTypes.add(k.getAsString()));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }



  public void serverAboutToStart(ServerAboutToStartEvent event) {
    String forwardingSecret = config.forwardingSecret.get();
    if(!(forwardingSecret.isBlank() || forwardingSecret.isEmpty())) {
      modernForwardingInstance = new ModernForwarding(forwardingSecret);
    }
  }


  static final ModConfigSpec configSpec;
  static {
    final Pair<Config, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Config::new);
    configSpec = specPair.getRight();
    config = specPair.getLeft();
  }

  public static class Config {
    public final ModConfigSpec.ConfigValue<? extends String> forwardingSecret;

    Config(ModConfigSpec.Builder builder)
    {
      builder.comment("Modern Forwarding Settings")
              .push("modernForwarding");

      forwardingSecret = builder
              .define("forwardingSecret", "");

      builder.pop();
    }

  }
}
