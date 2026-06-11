package io.github.jasonsimpart.createdelightcore.mixin.create;

import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ProcessingRecipeSerializer.class, remap = false)
public class ProcessingRecipeSerializerMixin {

    @Redirect(
            method = "writeToBuffer(Lnet/minecraft/network/FriendlyByteBuf;Lcom/simibubi/create/content/processing/recipe/ProcessingRecipe;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;writeVarInt(I)Lnet/minecraft/network/FriendlyByteBuf;",
                    ordinal = 5,
                    remap = true
            )
    )
    private FriendlyByteBuf createdelightcore$writeStableHeatCondition(FriendlyByteBuf buffer, int ignoredOrdinal,
                                                                       FriendlyByteBuf originalBuffer,
                                                                       ProcessingRecipe<?> recipe) {
        return buffer.writeUtf(recipe.getRequiredHeat().serialize());
    }

    @Redirect(
            method = "readFromBuffer(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lcom/simibubi/create/content/processing/recipe/ProcessingRecipe;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;readVarInt()I",
                    ordinal = 5,
                    remap = true
            )
    )
    private int createdelightcore$readStableHeatCondition(FriendlyByteBuf buffer) {
        return HeatCondition.deserialize(buffer.readUtf()).ordinal();
    }
}
