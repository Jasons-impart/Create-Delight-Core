package io.github.jasonsimpart.createdelightcore.registry;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;

public class CDCSpriteShifts {
    public static final CTSpriteShiftEntry STEEL_CASING = getCT(AllCTTypes.OMNIDIRECTIONAL, "steel_casing");
    public static final CTSpriteShiftEntry FORGE_STEEL_CASING = getCT(AllCTTypes.OMNIDIRECTIONAL, "forge_steel_casing");
    public static final CTSpriteShiftEntry STEEL_GLASS_CASING = getCT(AllCTTypes.OMNIDIRECTIONAL, "steel_glass_casing");
    public static final CTSpriteShiftEntry STEEL_CLEAR_GLASS_CASING = getCT(AllCTTypes.OMNIDIRECTIONAL, "steel_clear_glass_casing");
    public static final CTSpriteShiftEntry COPPER_COIL = getCT(AllCTTypes.OMNIDIRECTIONAL, "copper_coil");

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(type, CreateDelightCore.id("block/" + blockTextureName),
                CreateDelightCore.id("block/" + connectedTextureName + "_connected"));
    }
    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
        return getCT(type, blockTextureName, blockTextureName);
    }

}
