package io.github.jasonsimpart.registry;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.resources.ResourceLocation;

public final class ModSpriteShifts {
    public static final CTSpriteShiftEntry STEEL_CASING = casing("steel_casing");
    public static final CTSpriteShiftEntry FORGE_STEEL_CASING = casing("forge_steel_casing");
    public static final CTSpriteShiftEntry STEEL_GLASS_CASING = casing("steel_glass_casing");
    public static final CTSpriteShiftEntry STEEL_CLEAR_GLASS_CASING = casing("steel_clear_glass_casing");

    private ModSpriteShifts() {
    }

    private static CTSpriteShiftEntry casing(String textureName) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL, textureName, textureName + "_connected");
    }

    private static CTSpriteShiftEntry getCT(CTType type, String textureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(type, blockTexture(textureName), blockTexture(connectedTextureName));
    }

    private static ResourceLocation blockTexture(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateDelightCore.MODID, "block/" + path);
    }
}
