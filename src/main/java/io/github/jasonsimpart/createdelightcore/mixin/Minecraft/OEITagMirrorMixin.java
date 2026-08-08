package io.github.jasonsimpart.createdelightcore.mixin.Minecraft;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public abstract class OEITagMirrorMixin {
    private static final String OEI_MOD_ID = "oneenoughitem";
    private static final String ITEMS_TAG_DIR = "tags/items";
    private static final String REPLACEMENT_LOADER = "com.mafuyu404.oneenoughitem.util.MixinUtils$ReplacementLoader";
    private static final String OEI_CONFIG = "com.mafuyu404.oneenoughitem.init.config.OEIConfig";

    @Shadow
    @Final
    private String directory;

    @Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;", at = @At("RETURN"))
    private void createdelightcore$mirrorOeiItemTags(ResourceManager resourceManager,
                                                      CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {
        if (!ITEMS_TAG_DIR.equals(this.directory) || !isOeiDeeperReplaceEnabled()) {
            return;
        }

        Map<String, String> mappings = loadOeiMappings(resourceManager);
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags = cir.getReturnValue();
        if (mappings.isEmpty() || tags == null || tags.isEmpty()) {
            return;
        }

        int mirrored = 0;
        for (Map.Entry<ResourceLocation, List<TagLoader.EntryWithSource>> tag : tags.entrySet()) {
            ResourceLocation tagId = tag.getKey();
            List<TagLoader.EntryWithSource> entries = tag.getValue();
            if (entries == null) {
                continue;
            }

            List<TagLoader.EntryWithSource> additions = new ArrayList<>();
            if (tryMirrorItem(mappings.get("#" + tagId), tagId, "#" + tagId, additions)) {
                mirrored++;
            }

            for (TagLoader.EntryWithSource entryWithSource : entries) {
                TagEntry entry = entryWithSource.entry();
                if (!entryWithSource.remove() && !entry.isTag()
                        && tryMirrorItem(mappings.get(entry.getId().toString()), tagId, entry.getId().toString(), additions)) {
                    mirrored++;
                }
            }

            entries.addAll(additions);
        }

        if (mirrored > 0) {
            CreateDelightCore.LOGGER.info("OEI item tag mirror: added {} item entries", mirrored);
        }
    }

    private static boolean isOeiDeeperReplaceEnabled() {
        if (!ModList.get().isLoaded(OEI_MOD_ID)) {
            return false;
        }

        try {
            Class<?> configClass = Class.forName(OEI_CONFIG);
            Object config = configClass.getMethod("get").invoke(null);
            return config != null && (boolean) config.getClass().getMethod("deeperReplace").invoke(config);
        } catch (ReflectiveOperationException | LinkageError e) {
            CreateDelightCore.LOGGER.warn("OEI item tag mirror: cannot read Deeper_Replace", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> loadOeiMappings(ResourceManager resourceManager) {
        try {
            Class<?> loaderClass = Class.forName(REPLACEMENT_LOADER);
            Method loadCurrentSnapshot = loaderClass.getMethod("loadCurrentSnapshot", ResourceManager.class);
            Object snapshot = loadCurrentSnapshot.invoke(null, resourceManager);
            Object mappings = snapshot.getClass().getMethod("dataMap").invoke(snapshot);
            return mappings instanceof Map<?, ?> map ? (Map<String, String>) map : Collections.emptyMap();
        } catch (ReflectiveOperationException | LinkageError e) {
            CreateDelightCore.LOGGER.warn("OEI item tag mirror: cannot load replacement mappings", e);
            return Collections.emptyMap();
        }
    }

    private static boolean tryMirrorItem(String targetItemId, ResourceLocation tagId, String source,
                                         List<TagLoader.EntryWithSource> additions) {
        if (targetItemId == null || targetItemId.isBlank() || "minecraft:air".equals(targetItemId)) {
            return false;
        }

        try {
            ResourceLocation targetId = new ResourceLocation(targetItemId);
            if (BuiltInRegistries.ITEM.getOptional(targetId).isEmpty()) {
                return false;
            }

            additions.add(new TagLoader.EntryWithSource(
                    TagEntry.element(targetId),
                    "createdelightcore:oei_tag_mirror"
            ));
            CreateDelightCore.LOGGER.debug("OEI item tag mirror: add '{}' to {} (source='{}')", targetId, tagId, source);
            return true;
        } catch (Exception e) {
            CreateDelightCore.LOGGER.warn("OEI item tag mirror: invalid target item '{}' from {}", targetItemId, source);
            return false;
        }
    }
}
