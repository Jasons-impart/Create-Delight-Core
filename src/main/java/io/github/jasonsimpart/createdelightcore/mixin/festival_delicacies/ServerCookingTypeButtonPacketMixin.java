package io.github.jasonsimpart.createdelightcore.mixin.festival_delicacies;

import cn.foggyhillside.festival_delicacies.network.packet.ServerCookingTypeButtonPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ServerCookingTypeButtonPacket.class, remap = false)
public class ServerCookingTypeButtonPacketMixin {
    @ModifyConstant(method = "lambda$handle$0", constant = @Constant(intValue = 7))
    private int createdelightcore$removeDumplingCookingModes(int maxCookingType) {
        return 4;
    }
}
