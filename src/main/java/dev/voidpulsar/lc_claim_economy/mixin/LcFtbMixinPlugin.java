package dev.voidpulsar.lc_claim_economy.mixin;

import dev.voidpulsar.lc_claim_economy.compat.ModCompat;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Every mixin declared in {@code lc_claim_economy.mixins.json} (both the
 * "mixins" and "client" lists) targets an FTB Chunks or FTB Teams class.
 * When only Open Parties and Claims is installed (no FTB), none of those
 * target classes exist, and applying the config normally would throw at
 * startup. This plugin makes the whole config a no-op in that case instead
 * of crashing.
 * <p>
 * All our mixins use string-based {@code @Mixin(targets = "...")} targeting
 * rather than {@code @Mixin(SomeClass.class)}, so returning false here
 * happens before Mixin ever needs to resolve the actual target class.
 */
public class LcFtbMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        if (!ModCompat.isFtbAvailable()) {
            org.slf4j.LoggerFactory.getLogger("lc_claim_economy")
                    .info("FTB Chunks/Teams not detected - skipping FTB Chunks/Teams integration mixins.");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return ModCompat.isFtbAvailable();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
