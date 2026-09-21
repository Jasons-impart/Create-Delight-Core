package io.github.jasonsimpart.content.event;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import io.github.jasonsimpart.CreateDelightCore;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.util.Optional;
import io.github.jasonsimpart.util.ModIds;

/** Optional 0488 donor titles. Old quest-book grants are intentionally not migrated. */
public final class DonorLogin {
    private static final String APPLIED = "createdelightcore:donor_title_applied";
    private DonorLogin() {}

    public static void apply(ServerPlayer player) {
        if (!ModList.get().isLoaded(ModIds.FTB_RANKS) || player.getPersistentData().getBoolean(APPLIED)) return;
        var file = FMLPaths.GAMEDIR.get().resolve("donate_list.json");
        if (!Files.isRegularFile(file)) return;
        try (var reader = Files.newBufferedReader(file)) {
            var titles = JsonParser.parseReader(reader).getAsJsonObject();
            String name = player.getGameProfile().getName();
            if (!titles.has(name) || !titles.get(name).isJsonPrimitive()) return;
            String title = titles.get(name).getAsString();
            // Use the optional public API: title text is never executed as a command.
            Class<?> api = Class.forName("dev.ftb.mods.ftbranks.api.FTBRanksAPI");
            Class<?> managerType = Class.forName("dev.ftb.mods.ftbranks.api.RankManager");
            Class<?> rankType = Class.forName("dev.ftb.mods.ftbranks.api.Rank");
            Class<?> valueType = Class.forName("dev.ftb.mods.ftbranks.api.PermissionValue");
            Object manager = api.getMethod("manager").invoke(null);
            String id = "createdelightcore_donor_" + player.getUUID().toString().replace("-", "");
            Optional<?> existing = (Optional<?>) managerType.getMethod("getRank", String.class).invoke(manager, id);
            // Never enroll a player in an existing rank: even a reserved ID may be administrator-owned.
            if (existing.isPresent()) return;
            Object value = valueType.getMethod("parse", String.class).invoke(null, "[" + title + "]{name}");
            var setPermission = rankType.getMethod("setPermission", String.class, valueType);
            var add = rankType.getMethod("add", GameProfile.class);
            var delete = managerType.getMethod("deleteRank", String.class);
            // The deprecated (id, name, power) overload ignores id in FTB Ranks 2101.1.3.
            Object rank = managerType
                    .getMethod("createRank", String.class, int.class, boolean.class).invoke(manager, id, 1, false);
            try {
                setPermission.invoke(rank, "ftbranks.name_format", value);
                add.invoke(rank, player.getGameProfile());
                player.getPersistentData().putBoolean(APPLIED, true);
            } catch (Exception exception) {
                try {
                    Optional<?> current = (Optional<?>) managerType.getMethod("getRank", String.class).invoke(manager, id);
                    if (current.orElse(null) == rank) delete.invoke(manager, id);
                } catch (Exception rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            }
        } catch (Exception exception) {
            CreateDelightCore.LOGGER.warn("Unable to apply donor title from donate_list.json", exception);
        }
    }
}
