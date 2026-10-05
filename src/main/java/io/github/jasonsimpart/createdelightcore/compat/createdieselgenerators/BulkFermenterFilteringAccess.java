package io.github.jasonsimpart.createdelightcore.compat.createdieselgenerators;

import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermentingRecipe;
import net.minecraft.core.Direction;

public interface BulkFermenterFilteringAccess {
    FilteringBehaviour createdelightcore$getFilter();

    boolean createdelightcore$isFilterSideDisplayed(Direction direction);

    BulkFermentingRecipe createdelightcore$getCurrentRecipe();

    int createdelightcore$getWidth();

    int createdelightcore$getHeight();
}
