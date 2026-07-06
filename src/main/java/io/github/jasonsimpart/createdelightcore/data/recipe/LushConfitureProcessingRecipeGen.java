package io.github.jasonsimpart.createdelightcore.data.recipe;

import com.gumillea.cosmopolitan.core.reg.CosmoItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.api.data.recipe.DatagenMod;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public final class LushConfitureProcessingRecipeGen {
    private static final int SERVING_AMOUNT = 125;

    private static final DatagenMod FRUITS_DELIGHT = () -> "fruitsdelight";

    public static final class Mixing extends CDProcessingRecipeGen {
        GeneratedRecipe
                LUSH_CONFITURE_JELLY = create("flowing_lush_confiture_jelly", b -> b
                        .require(FRUITS_DELIGHT, "lemon_slice")
                        .require(Items.SUGAR)
                        .require(CosmoItems.ARBUTUS_BERRIES.get().asItem())
                        .require(Items.GLOW_BERRIES.asItem())
                        .require(Fluids.WATER, 100)
                        .output(CDFluids.LUSH_CONFITURE_JELLY.get(), SERVING_AMOUNT)
                        .requiresHeat(HeatCondition.HEATED)
                        .whenModLoaded("create")),

                LUSH_CONFITURE_JELLO = create("flowing_lush_confiture_jello", b -> b
                        .require(Items.SLIME_BALL)
                        .require(CDFluids.LUSH_CONFITURE_JELLY.get(), SERVING_AMOUNT)
                        .output(CDFluids.LUSH_CONFITURE_JELLO.get(), SERVING_AMOUNT)
                        .requiresHeat(HeatCondition.HEATED)
                        .whenModLoaded("create"));

        public Mixing(PackOutput output) {
            super(output);
        }

        @Override
        protected AllRecipeTypes getRecipeType() {
            return AllRecipeTypes.MIXING;
        }
    }

    public static final class Filling extends CDProcessingRecipeGen {
        GeneratedRecipe
                LUSH_CONFITURE_JELLY = create("lush_confiture_jelly", b -> b
                        .require(Items.GLASS_BOTTLE)
                        .require(CDFluids.LUSH_CONFITURE_JELLY.get(), SERVING_AMOUNT)
                        .output(CDBlocks.LUSH_CONFITURE.asItem())
                        .whenModLoaded("create")),

                LUSH_CONFITURE_JELLO = create("lush_confiture_jello", b -> b
                        .require(Items.BOWL)
                        .require(CDFluids.LUSH_CONFITURE_JELLO.get(), SERVING_AMOUNT)
                        .output(CDItems.LUSH_CONFITURE_JELLO.get())
                        .whenModLoaded("create"));

        public Filling(PackOutput output) {
            super(output);
        }

        @Override
        protected AllRecipeTypes getRecipeType() {
            return AllRecipeTypes.FILLING;
        }
    }

    public static final class Emptying extends CDProcessingRecipeGen {
        GeneratedRecipe
                LUSH_CONFITURE_JELLY = create("lush_confiture_jelly_emptying", b -> b
                        .require(CDBlocks.LUSH_CONFITURE.asItem())
                        .output(Items.GLASS_BOTTLE)
                        .output(CDFluids.LUSH_CONFITURE_JELLY.get(), SERVING_AMOUNT)
                        .whenModLoaded("create")),

                LUSH_CONFITURE_JELLO = create("lush_confiture_jello_emptying", b -> b
                        .require(CDItems.LUSH_CONFITURE_JELLO.get())
                        .output(Items.BOWL)
                        .output(CDFluids.LUSH_CONFITURE_JELLO.get(), SERVING_AMOUNT)
                        .whenModLoaded("create"));

        public Emptying(PackOutput output) {
            super(output);
        }

        @Override
        protected AllRecipeTypes getRecipeType() {
            return AllRecipeTypes.EMPTYING;
        }
    }

    private LushConfitureProcessingRecipeGen() {
    }
}
