package io.github.jasonsimpart.content.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ShallowCaveDensityFunction(DensityFunction input, DensityFunction surfaceDensity, int depth) implements DensityFunction {
    public static final MapCodec<ShallowCaveDensityFunction> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(ShallowCaveDensityFunction::input),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("surface_density").forGetter(ShallowCaveDensityFunction::surfaceDensity),
            com.mojang.serialization.Codec.INT.fieldOf("depth").forGetter(ShallowCaveDensityFunction::depth)
    ).apply(instance, ShallowCaveDensityFunction::new));
    public static final KeyDispatchDataCodec<ShallowCaveDensityFunction> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        int depthLimit = depth;
        if (depthLimit <= 0) {
            return input.compute(context);
        }

        double surfaceDensity = this.surfaceDensity.compute(context);
        if (surfaceDensity <= 0) {
            return input.compute(context);
        }

        double surfaceFactor = Mth.clamp(surfaceDensity / 1.5625D, 0.0D, 1.0D);
        double heightFactor = Mth.clamp((context.blockY() - 60.0D + depthLimit) / depthLimit, 0.0D, 1.0D);
        return input.compute(context) + surfaceFactor * heightFactor;
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ShallowCaveDensityFunction(input.mapAll(visitor), surfaceDensity.mapAll(visitor), depth));
    }

    @Override
    public double minValue() {
        return input.minValue();
    }

    @Override
    public double maxValue() {
        return input.maxValue() + 1.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
