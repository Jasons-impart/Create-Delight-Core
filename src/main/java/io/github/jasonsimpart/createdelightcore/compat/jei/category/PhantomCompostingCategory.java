package io.github.jasonsimpart.createdelightcore.compat.jei.category;

import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDTags;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PhantomCompostingCategory implements IRecipeCategory<PhantomCompostingCategory.PhantomCompostingRecipe> {
    public static final ResourceLocation UID = CreateDelightCore.id("phantom_composting");
    public static final RecipeType<PhantomCompostingRecipe> RECIPE_TYPE = new RecipeType<>(UID,
            PhantomCompostingRecipe.class);

    private static final ResourceLocation TEXTURE =
            CreateDelightCore.id("textures/gui/jei/phantom_composting.png");

    private final IDrawable background;
    private final IDrawable slotIcon;
    private final IDrawable icon;
    private final ItemStack phantomCompost;
    private final ItemStack lunaSoil;

    public PhantomCompostingCategory(IGuiHelper guiHelper) {
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 118, 80);
        slotIcon = guiHelper.createDrawable(TEXTURE, 119, 0, 22, 22);

        phantomCompost = new ItemStack(CDBlocks.PHANTOM_COMPOST.get());
        lunaSoil = new ItemStack(CDBlocks.LUNA_SOIL.get());
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, lunaSoil);
    }

    @Override
    public RecipeType<PhantomCompostingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + CreateDelightCore.MODID + ".phantomComposting");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PhantomCompostingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 26)
                .addItemStack(phantomCompost);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 93, 26)
                .addItemStack(lunaSoil);

        List<ItemStack> accelerators = getAccelerators();
        if (!accelerators.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 64, 54)
                    .addItemStacks(accelerators);
        }
    }

    @Override
    public void draw(PhantomCompostingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        slotIcon.draw(graphics, 63, 53);
    }

    @Override
    public List<Component> getTooltipStrings(PhantomCompostingRecipe recipe, IRecipeSlotsView recipeSlotsView,
                                             double mouseX, double mouseY) {
        if (inIconOn(49, 9, mouseX, mouseY)) {
            return List.of(translate(".dimension"));
        }
        if (inIconAt(40, 38, mouseX, mouseY)) {
            return List.of(translate(".light"));
        }
        if (inIconAt(53, 38, mouseX, mouseY)) {
            return List.of(translate(".fluid"));
        }
        if (inIconAt(67, 38, mouseX, mouseY)) {
            return List.of(translate(".accelerators"));
        }
        return Collections.emptyList();
    }

    private static List<ItemStack> getAccelerators() {
        return ForgeRegistries.BLOCKS.tags()
                .getTag(CDTags.AllBlockTags.PHANTOM_COMPOST_ACTIVATORS.tag)
                .stream()
                .map(ItemStack::new)
                .filter(stack -> !stack.isEmpty())
                .collect(Collectors.toList());
    }

    private static boolean inIconAt(int x, int y, double mouseX, double mouseY) {
        return x <= mouseX && mouseX < x + 11 && y <= mouseY && mouseY < y + 11;
    }

    private static boolean inIconOn(int x, int y, double mouseX, double mouseY) {
        return x <= mouseX && mouseX < x + 16 && y <= mouseY && mouseY < y + 19;
    }

    private static Component translate(String suffix) {
        return Component.translatable("jei." + CreateDelightCore.MODID + ".phantomComposting" + suffix);
    }

    public record PhantomCompostingRecipe() {
    }
}
