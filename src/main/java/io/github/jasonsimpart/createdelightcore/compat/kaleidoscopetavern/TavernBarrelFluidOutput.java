package io.github.jasonsimpart.createdelightcore.compat.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import de.cadentem.quality_food.core.Quality;
import io.github.createdelight.qualityfoodfluids.api.QualityFoodFluidsApi;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.compat.alcohol.ItemFluidContainers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

/** The resolved recipe snapshot and the only extractable stock of a fluid-mode batch. */
public final class TavernBarrelFluidOutput {
    public static final int CAPACITY = 4000;

    private enum Source { FIXED, CGT }

    private final ResourceLocation recipeId;
    private final ItemStack template;
    private final int assembledCount;
    private final int servingAmount;
    private final Item container;
    private final Fluid fluid;
    private final Source source;
    private final int brewLevel;
    private final FluidTank tank = new FluidTank(CAPACITY);
    private boolean delivering;

    private TavernBarrelFluidOutput(ResourceLocation recipeId, ItemStack assembled, int servingAmount,
                                   Item container, Fluid fluid, Source source, int brewLevel) {
        this.recipeId = recipeId;
        this.template = assembled.copyWithCount(1);
        this.assembledCount = assembled.getCount();
        this.servingAmount = servingAmount;
        this.container = container;
        this.fluid = fluid;
        this.source = source;
        this.brewLevel = brewLevel;
    }

    @Nullable
    public static TavernBarrelFluidOutput select(ResourceLocation recipeId, ItemStack assembled,
                                                 @Nullable Ingredient carrier, int brewLevel) {
        if (assembled.isEmpty()) return null;
        try {
            var nativeServing = TavernCgtDrinks.serving(assembled.getItem());
            var fixed = ItemFluidContainers.find(assembled);
            if (nativeServing == null && fixed == null) {
                CreateDelightCore.LOGGER.debug("No fluid conversion for Tavern barrel recipe {} producing {}", recipeId, itemId(assembled));
                return null;
            }

            boolean nativeDrink = nativeServing != null;
            int unit = nativeDrink ? nativeServing.amount() : fixed.amount();
            Item container = nativeDrink ? nativeServing.container() : fixed.emptyContainer();
            if (container == null || container == Items.AIR || carrier == null || !carrier.test(new ItemStack(container))) {
                return reject(recipeId, assembled, "fluid container is incompatible with the recipe carrier");
            }
            if (unit <= 0 || (long) assembled.getCount() * unit > CAPACITY) {
                return reject(recipeId, assembled, assembled.getCount() + " servings of " + unit + " mB exceed barrel capacity");
            }

            if (!nativeDrink) return new TavernBarrelFluidOutput(recipeId, assembled, unit, container,
                    fixed.fluid(), Source.FIXED, 0);
            FluidStack sample = nativeFluid(assembled, unit, brewLevel);
            if (sample.isEmpty()) return reject(recipeId, assembled, "CGT did not produce a fluid");
            return new TavernBarrelFluidOutput(recipeId, assembled, unit, container, sample.getFluid(), Source.CGT, brewLevel);
        } catch (ReflectiveOperationException e) {
            return reject(recipeId, assembled, "CGT conversion failed: " + e.getCause());
        }
    }

    private static FluidStack nativeFluid(ItemStack template, int amount, int brewLevel) throws ReflectiveOperationException {
        var unqualified = QualityFoodFluidsApi.clearItemQuality(template.copyWithCount(1));
        return TavernCgtDrinks.fromItem(unqualified, amount, brewLevel);
    }

    @Nullable
    private static TavernBarrelFluidOutput reject(ResourceLocation recipeId, ItemStack item, String reason) {
        CreateDelightCore.LOGGER.warn("Using item output for Tavern barrel recipe {} producing {}: {}", recipeId, itemId(item), reason);
        return null;
    }

    private static ResourceLocation itemId(ItemStack item) {
        return ForgeRegistries.ITEMS.getKey(item.getItem());
    }

    public boolean completeBrewing(Quality quality) {
        int amount = assembledCount * servingAmount;
        FluidStack result;
        try {
            result = source == Source.CGT ? nativeFluid(template, amount, brewLevel) : new FluidStack(fluid, amount);
        } catch (ReflectiveOperationException e) {
            reject(recipeId, template, "CGT conversion failed at brew completion: " + e.getCause());
            return false;
        }
        if (result.getFluid() != fluid || result.getAmount() != amount) {
            reject(recipeId, template, "resolved fluid or amount changed at brew completion");
            return false;
        }
        tank.setFluid(QualityFoodFluidsApi.withQuality(result, quality));
        return true;
    }

    public void updateQuality(Quality quality) {
        tank.setFluid(QualityFoodFluidsApi.withQuality(tank.getFluid(), quality));
    }

    public int amount() { return tank.getFluidAmount(); }
    public int servings() { return amount() / servingAmount; }
    public ItemStack assembledOutput() { return template.copyWithCount(assembledCount); }
    public Ingredient carrier() { return Ingredient.of(container); }
    public FluidStack visibleFluid() { return tank.getFluid().copy(); }

    public ItemStack previewBottle() {
        if (delivering || servings() == 0) return ItemStack.EMPTY;
        ItemStack filled = TavernQuality.qualify(template, QualityFoodFluidsApi.getQuality(tank.getFluid()));
        if (source == Source.CGT && filled.getItem() instanceof BottleBlockItem) {
            BottleBlockItem.setBrewLevel(filled, brewLevel);
        }
        return filled;
    }

    /** A rejected delivery leaves the fluid intact. Ignore reentrant drains during delivery. */
    public ItemStack transferServing(Predicate<ItemStack> deliver) {
        ItemStack filled = previewBottle();
        if (filled.isEmpty()) return ItemStack.EMPTY;
        delivering = true;
        try {
            if (!deliver.test(filled)) return ItemStack.EMPTY;
            tank.drain(servingAmount, FluidAction.EXECUTE);
            return filled;
        } finally {
            delivering = false;
        }
    }

    public FluidStack drain(FluidStack request, FluidAction action) {
        FluidStack held = tank.getFluid();
        if (request.isEmpty() || held.getFluid() != request.getFluid() || !Objects.equals(held.getTag(), request.getTag())) {
            return FluidStack.EMPTY;
        }
        return drain(request.getAmount(), action);
    }

    public FluidStack drain(int amount, FluidAction action) {
        return delivering || amount <= 0 ? FluidStack.EMPTY : tank.drain(amount, action);
    }

    CompoundTag save() {
        var tag = new CompoundTag();
        tag.putString("source", source.name());
        tag.putString("recipe", recipeId.toString());
        tag.put("template", template.save(new CompoundTag()));
        tag.putInt("assembled_count", assembledCount);
        tag.putInt("serving_amount", servingAmount);
        tag.putString("container", ForgeRegistries.ITEMS.getKey(container).toString());
        tag.putString("fluid", ForgeRegistries.FLUIDS.getKey(fluid).toString());
        tag.putInt("brew_level", brewLevel);
        if (amount() > 0) tag.put("tank", tank.writeToNBT(new CompoundTag()));
        return tag;
    }

    @Nullable
    static TavernBarrelFluidOutput load(CompoundTag tag, boolean brewing) {
        var recipeId = ResourceLocation.tryParse(tag.getString("recipe"));
        ItemStack assembled = ItemStack.of(tag.getCompound("template")).copyWithCount(tag.getInt("assembled_count"));
        int unit = tag.getInt("serving_amount");
        Item container = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(tag.getString("container")));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.tryParse(tag.getString("fluid")));
        Source source = switch (tag.getString("source")) {
            case "FIXED" -> Source.FIXED;
            case "CGT" -> Source.CGT;
            default -> null;
        };
        if (recipeId == null || source == null || assembled.isEmpty() || unit <= 0
                || (long) assembled.getCount() * unit > CAPACITY || container == null || container == Items.AIR
                || fluid == null || fluid == Fluids.EMPTY) return null;
        var output = new TavernBarrelFluidOutput(recipeId, assembled, unit, container, fluid, source, tag.getInt("brew_level"));
        if (!brewing) {
            FluidStack held = FluidStack.loadFluidStackFromNBT(tag.getCompound("tank"));
            if (held.isEmpty() || held.getFluid() != fluid || held.getAmount() > assembled.getCount() * unit) return null;
            output.tank.setFluid(held);
        }
        return output;
    }
}
