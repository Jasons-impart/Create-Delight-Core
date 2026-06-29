package io.github.jasonsimpart.createdelightcore.compat.createdieselgenerators;

import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermentingRecipe;

public interface BulkFermenterFilteringAccess {
    FilteringBehaviour createdelightcore$getFilter();

    BulkFermentingRecipe createdelightcore$getCurrentRecipe();

    int createdelightcore$getWidth();

    int createdelightcore$getHeight();
}
