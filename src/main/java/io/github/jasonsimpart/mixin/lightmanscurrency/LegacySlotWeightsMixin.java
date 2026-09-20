package io.github.jasonsimpart.mixin.lightmanscurrency;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Opt-in conversion for the pack's legacy relative-weight slot machine. */
@Mixin(value = SlotMachineTraderData.class, remap = false)
public abstract class LegacySlotWeightsMixin {
    @Inject(method = "loadAdditionalFromJson", at = @At("TAIL"))
    private void convertWeights(JsonObject json, HolderLookup.Provider lookup, CallbackInfo ci) {
        if (!json.has("createdelightcore:relative_weights")
                || !json.get("createdelightcore:relative_weights").getAsBoolean()) return;
        var source = json.getAsJsonArray("Entries");
        var entries = ((SlotMachineTraderData) (Object) this).getAllEntries();
        if (entries.size() != source.size()) throw new IllegalArgumentException("Incomplete legacy slot entries");
        double total = 0;
        for (var value : source) {
            double weight = value.getAsJsonObject().get("Weight").getAsDouble();
            if (!Double.isFinite(weight) || weight <= 0) throw new IllegalArgumentException("Invalid slot weight");
            total += weight;
        }
        for (int i = 0; i < entries.size(); i++) {
            double odds = source.get(i).getAsJsonObject().get("Weight").getAsDouble() / total * 100;
            if (odds < 0.01 || odds > 99.9) throw new IllegalArgumentException("Slot odds outside native range");
            entries.get(i).setOdds(odds);
        }
    }
}
