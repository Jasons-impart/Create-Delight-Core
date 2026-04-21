package io.github.jasonsimpart.createdelightcore.network;

import io.github.jasonsimpart.createdelightcore.util.Triplet;
import net.minecraft.world.level.material.Fluid;

import java.util.HashMap;
import java.util.Map;

public class ClientFuelCache {
    public static Map<Fluid, Triplet<Integer, Boolean, Integer>> BURNER_MAP = new HashMap<>();
    public static Map<Fluid, Triplet<Integer, Boolean, Integer>> COOLER_MAP = new HashMap<>();
}
