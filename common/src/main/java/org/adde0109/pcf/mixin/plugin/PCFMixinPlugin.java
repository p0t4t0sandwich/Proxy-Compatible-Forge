package org.adde0109.pcf.mixin.plugin;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import com.bawnorton.mixinsquared.adjuster.MixinAnnotationAdjusterRegistrar;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import com.bawnorton.mixinsquared.api.MixinCanceller;
import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;

import dev.neuralnexus.taterapi.meta.MetaAPI;
import dev.neuralnexus.taterapi.meta.MinecraftVersions;
import dev.neuralnexus.taterapi.meta.Platforms;
import dev.neuralnexus.taterapi.muxins.Muxins;

import org.adde0109.pcf.PCF;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/** A mixin plugin for PCF. */
public class PCFMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        if (MetaAPI.instance().isPlatformPresent(Platforms.SPONGE)
                && MetaAPI.instance()
                        .meta(Platforms.FORGE)
                        .get()
                        .minecraftVersion()
                        .is(MinecraftVersions.V16_5)) {
            MixinSquaredBootstrap.init();
            ServiceLoader.load(MixinCanceller.class).forEach(MixinCancellerRegistrar::register);
            ServiceLoader.load(MixinAnnotationAdjuster.class)
                    .forEach(MixinAnnotationAdjusterRegistrar::register);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    // TODO: Conditionally apply mixins based on enable configs
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        PCF.forceLoadConfig();
        PCF.Debug debug = PCF.instance().debug();
        return Muxins.shouldApplyMixin(mixinClassName, debug.disabledMixins(), debug.enabled());
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        MixinSquaredBootstrap.reOrderExtensions();
        return null;
    }

    @Override
    public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo) {}

    @Override
    public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo) {}
}
