package io.github.jasonsimpart.mixin.resourcepack;

import io.github.jasonsimpart.Config;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(PathPackResources.class)
public abstract class PathPackResourcesMixin {
    private static final String CREATE_CAFE_NAMESPACE = "createcafe";
    private static final String CREATE_CAFE_BAD_RESOURCE_PATH = "textures/item/demon's_dream_fruit_milk_tea.png";
    private static final String BIOMESPY_NAMESPACE = "biomespy";
    private static final String BIOMESPY_BAD_RESOURCE_PATH = "tags/worldgen/structure/uninit_safe.json\u200E";
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final String NETHEREXP_NAMESPACE = "netherexp";
    private static final String CREATE_DRAGONS_PLUS_NAMESPACE = "create_dragons_plus";
    private static final ThreadLocal<PackIdentity> CDC_CURRENT_PACK = new ThreadLocal<>();

    @Shadow
    @Final
    private Path root;

    @Inject(method = "listResources", at = @At("HEAD"))
    private void cdc$capturePackIdentity(
            net.minecraft.server.packs.PackType packType,
            String namespace,
            String path,
            PackResources.ResourceOutput resourceOutput,
            CallbackInfo ci
    ) {
        CDC_CURRENT_PACK.set(new PackIdentity(((PackResources) (Object) this).location().id(), this.root.toString()));
    }

    @Inject(method = "listResources", at = @At("RETURN"))
    private void cdc$clearPackIdentity(
            net.minecraft.server.packs.PackType packType,
            String namespace,
            String path,
            PackResources.ResourceOutput resourceOutput,
            CallbackInfo ci
    ) {
        CDC_CURRENT_PACK.remove();
    }

    @Redirect(
            method = "listPath",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V")
    )
    private static void cdc$filterKnownInvalidPaths(
            Stream<Path> stream,
            Consumer<? super Path> action,
            String namespace,
            Path namespacePath,
            List<String> decomposedPath,
            PackResources.ResourceOutput output
    ) {
        if (!Config.ENABLE_INVALID_RESOURCE_PATH_FILTERS.get() || !cdc$shouldCheckNamespace(namespace)) {
            stream.forEach(action);
            return;
        }

        stream.filter(path -> !cdc$isKnownFilteredPath(namespace, namespacePath, path)).forEach(action);
    }

    private static boolean cdc$shouldCheckNamespace(String namespace) {
        return CREATE_CAFE_NAMESPACE.equals(namespace)
                || BIOMESPY_NAMESPACE.equals(namespace)
                || MINECRAFT_NAMESPACE.equals(namespace)
                || NETHEREXP_NAMESPACE.equals(namespace)
                || CREATE_DRAGONS_PLUS_NAMESPACE.equals(namespace);
    }

    private static boolean cdc$isKnownFilteredPath(String namespace, Path namespacePath, Path path) {
        String resourcePath = namespacePath.relativize(path).toString().replace('\\', '/');
        return CREATE_CAFE_NAMESPACE.equals(namespace) && CREATE_CAFE_BAD_RESOURCE_PATH.equals(resourcePath)
                || BIOMESPY_NAMESPACE.equals(namespace) && BIOMESPY_BAD_RESOURCE_PATH.equals(resourcePath)
                || cdc$isNetherExpFilteredPath(namespace, namespacePath, resourcePath)
                || cdc$isCreateDragonsPlusFilteredPath(namespace, namespacePath, resourcePath);
    }

    private static boolean cdc$isNetherExpFilteredPath(String namespace, Path namespacePath, String resourcePath) {
        if (!cdc$isFromCurrentPackOrPath(namespacePath, "netherexp")
                && !cdc$isFromCurrentPackOrPath(namespacePath, "jne_retextures")
                && !cdc$isFromCurrentPackOrPath(namespacePath, "jadens-nether-expansion")
                && !cdc$isFromCurrentPackOrPath(namespacePath, "nether-expansion")) {
            return false;
        }

        if (MINECRAFT_NAMESPACE.equals(namespace)) {
            return switch (resourcePath) {
                case "tags/worldgen/biome/spawns_warm_variant_frogs.json",
                     "tags/block/slabs.json",
                     "blockstates/nether_wart_block.json",
                     "blockstates/warped_wart_block.json" -> true;
                default -> false;
            };
        }

        return NETHEREXP_NAMESPACE.equals(namespace)
                && switch (resourcePath) {
                    case "blockstates/brazier_chest.json", "blockstates/treacherous_candle.json" -> true;
                    default -> false;
                };
    }

    private static boolean cdc$isCreateDragonsPlusFilteredPath(String namespace, Path namespacePath, String resourcePath) {
        return CREATE_DRAGONS_PLUS_NAMESPACE.equals(namespace)
                && (cdc$isFromCurrentPackOrPath(namespacePath, "createdragonsplus")
                || cdc$isFromCurrentPackOrPath(namespacePath, "create_dragons_plus"))
                && switch (resourcePath) {
                    case "data_maps/block/air_current_block_interaction/blasting.json",
                         "data_maps/block/air_current_block_interaction/freezing.json",
                         "data_maps/block/air_current_block_interaction/smoking.json",
                         "data_maps/block/air_current_block_interaction/splashing.json",
                         "data_maps/block/fragile_fluid_tank/lava.json",
                         "data_maps/block/fragile_fluid_tank/water.json" -> true;
                    default -> false;
                };
    }

    private static boolean cdc$isFromCurrentPackOrPath(Path namespacePath, String packMarker) {
        String normalizedMarker = packMarker.toLowerCase(Locale.ROOT);
        PackIdentity packIdentity = CDC_CURRENT_PACK.get();
        return packIdentity != null && packIdentity.contains(normalizedMarker)
                || namespacePath.toString().toLowerCase(Locale.ROOT).contains(normalizedMarker);
    }

    private record PackIdentity(String id, String root) {
        private boolean contains(String marker) {
            return id.toLowerCase(Locale.ROOT).contains(marker)
                    || root.toLowerCase(Locale.ROOT).contains(marker);
        }
    }
}
