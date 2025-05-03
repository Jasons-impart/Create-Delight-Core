package io.github.jasonsimpart.createdelightcore.registry;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import de.cadentem.quality_food.core.Quality;
import io.github.jasonsimpart.createdelightcore.content.filter.ItemQualityAttribute;

public class CDCItemAttributes {
    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        ItemAttribute.register(new ItemQualityAttribute(Quality.NONE));
        registered = true;
    }
}
