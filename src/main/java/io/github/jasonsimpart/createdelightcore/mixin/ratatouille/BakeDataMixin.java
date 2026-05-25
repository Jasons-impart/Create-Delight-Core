package io.github.jasonsimpart.createdelightcore.mixin.ratatouille;

import com.llamalad7.mixinextras.sugar.Local;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.forsteri.ratatouille.content.oven.BakeData;
import org.forsteri.ratatouille.content.oven.OvenBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(BakeData.class)
public class BakeDataMixin {
    @Unique
    private static final ResourceLocation create_Delight_Core$rawCustomPizza =
            ResourceLocation.fromNamespaceAndPath("bakeries", "raw_custom_pizza");
    @Unique
    private static final ResourceLocation create_Delight_Core$customPizza =
            ResourceLocation.fromNamespaceAndPath("bakeries", "custom_pizza");

    @ModifyArg(method = "processFood",index = 1, at = @At(value = "INVOKE", target = "Lorg/forsteri/ratatouille/content/oven/OvenBlockEntity$Inventory;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"), remap = false)
    public ItemStack applyQuality(ItemStack par2, @Local OvenBlockEntity.Inventory inventory) {
        ItemStack input = inventory.getStackInSlot(0);
        if (create_Delight_Core$isItem(input, create_Delight_Core$rawCustomPizza)
                && create_Delight_Core$isItem(par2, create_Delight_Core$customPizza)
                && input.hasTag()) {
            par2.setTag(input.getTag().copy());
        }
        QualityUtils.applyQuality(par2, List.of(input), null);
        return par2;
    }

    @Unique
    private static boolean create_Delight_Core$isItem(ItemStack stack, ResourceLocation id) {
        return id.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }
}
