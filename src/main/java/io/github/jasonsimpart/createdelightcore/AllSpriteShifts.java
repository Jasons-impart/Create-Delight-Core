package io.github.jasonsimpart.createdelightcore;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;

public class AllSpriteShifts {
    public static final CTSpriteShiftEntry STEEL_CASING = getCT(AllCTTypes.OMNIDIRECTIONAL, "steel_casing");
    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(type, CreateDelightCore.id("block/" + blockTextureName),
                CreateDelightCore.id("block/" + connectedTextureName + "_connected"));
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
        return getCT(type, blockTextureName, blockTextureName);
    }

}
