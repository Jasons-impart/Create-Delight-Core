package io.github.jasonsimpart.createdelightcore.util;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FancyMenuAssetExporter {
    private static final String RESOURCE_ROOT = "/assets/createdelightcore/textures/gui/fancymenu/";
    private static final List<Asset> ASSETS = createAssets();

    private FancyMenuAssetExporter() {
    }

    public static void export() {
        Path gameDirectory = FMLPaths.GAMEDIR.get();
        for (Asset asset : ASSETS) {
            Path target = gameDirectory.resolve(asset.targetPath());
            try (InputStream input = CreateDelightCore.class.getResourceAsStream(RESOURCE_ROOT + asset.resourcePath())) {
                if (input == null) {
                    CreateDelightCore.LOGGER.warn("Missing bundled FancyMenu asset: {}", asset.resourcePath());
                    continue;
                }
                Files.createDirectories(target.getParent());
                Files.copy(input, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                CreateDelightCore.LOGGER.warn("Failed to export FancyMenu asset to {}", target, exception);
            }
        }
    }

    private static List<Asset> createAssets() {
        List<Asset> assets = new ArrayList<>();
        assets.add(new Asset("minecraft_title.png", "config/fancymenu/assets/minecraft_title.png"));
        for (int index = 1; index <= 15; index++) {
            String filename = "image_" + index + ".png";
            assets.add(new Asset(
                    "slideshow/" + filename,
                    "config/fancymenu/slideshows/create_delight/images/" + filename
            ));
        }
        return List.copyOf(assets);
    }

    private record Asset(String resourcePath, String targetPath) {
    }
}
