package io.github.jasonsimpart.createdelightcore.compat.ftbranks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

final class SponsorTitleStore {
    private static final Gson GSON = new Gson();
    private static final URI REMOTE_URI = URI.create(
            "https://raw.githubusercontent.com/Jasons-impart/Create-Delight-Remake/main/docs/sponsors.json");
    private static final String DEFAULT_RESOURCE = "/createdelightcore/sponsors.json";
    private static final String LEGACY_FILE = "donate_list.json";
    private static final int MAX_FILE_BYTES = 1024 * 1024;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final MinecraftServer server;
    private final Path cachePath;
    private final AtomicReference<Map<String, String>> titles;
    private final ExecutorService refreshExecutor;
    private volatile boolean closed;

    SponsorTitleStore(MinecraftServer server) {
        this.server = server;
        this.cachePath = server.getServerDirectory().toPath()
                .resolve(".createdelightcore")
                .resolve("sponsors-cache.json");
        this.titles = new AtomicReference<>(loadInitialTitles());
        this.refreshExecutor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "createdelight-sponsor-sync");
            thread.setDaemon(true);
            return thread;
        });
    }

    Optional<String> findTitle(String playerName) {
        return Optional.ofNullable(titles.get().get(playerName));
    }

    void refreshAsync() {
        refreshExecutor.execute(this::refreshRemote);
    }

    void close() {
        closed = true;
        refreshExecutor.shutdownNow();
    }

    private Map<String, String> loadInitialTitles() {
        Optional<Map<String, String>> cached = readFile(cachePath);
        if (cached.isPresent()) {
            return cached.get();
        }

        Optional<Map<String, String>> legacy = readFile(server.getServerDirectory().toPath().resolve(LEGACY_FILE));
        if (legacy.isPresent()) {
            return legacy.get();
        }

        try (var stream = SponsorTitleStore.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (stream == null) {
                throw new IOException("Missing built-in sponsor list: " + DEFAULT_RESOURCE);
            }
            try (Reader reader = new java.io.InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return parse(reader);
            }
        } catch (IOException | JsonParseException exception) {
            CreateDelightCore.LOGGER.error("Failed to load built-in sponsor list", exception);
            return Map.of();
        }
    }

    private void refreshRemote() {
        if (closed) {
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(REMOTE_URI)
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header("User-Agent", "CreateDelightCore sponsor sync")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IOException("HTTP status " + response.statusCode());
            }
            byte[] body = response.body();
            if (body.length > MAX_FILE_BYTES) {
                throw new IOException("Sponsor list is larger than " + MAX_FILE_BYTES + " bytes");
            }

            Map<String, String> remoteTitles = parse(new String(body, StandardCharsets.UTF_8));
            if (closed) {
                return;
            }
            writeCache(remoteTitles);
            titles.set(remoteTitles);
            try {
                server.execute(() -> FTBRanksCompat.applyTitlesToOnlinePlayers(server, this));
            } catch (RuntimeException exception) {
                CreateDelightCore.LOGGER.debug("Sponsor title refresh completed after server shutdown");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (IOException | JsonParseException exception) {
            CreateDelightCore.LOGGER.debug("Sponsor list refresh failed: {}", exception.getMessage());
        }
    }

    private void writeCache(Map<String, String> values) throws IOException {
        Path parent = cachePath.getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "sponsors-", ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(values), StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temporary, cachePath, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, cachePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Optional<Map<String, String>> readFile(Path path) {
        try {
            if (!Files.isRegularFile(path) || Files.size(path) > MAX_FILE_BYTES) {
                return Optional.empty();
            }
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                return Optional.of(parse(reader));
            }
        } catch (IOException | JsonParseException exception) {
            CreateDelightCore.LOGGER.debug("Ignoring invalid sponsor cache: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    private static Map<String, String> parse(String json) {
        return parse(GSON.fromJson(json, JsonObject.class));
    }

    private static Map<String, String> parse(Reader reader) throws IOException {
        return parse(GSON.fromJson(reader, JsonObject.class));
    }

    private static Map<String, String> parse(JsonObject object) {
        if (object == null) {
            throw new JsonParseException("Sponsor list must be a JSON object");
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();
            if (entry.getKey().isBlank() || value == null || !value.isJsonPrimitive()
                    || !value.getAsJsonPrimitive().isString() || value.getAsString().isBlank()) {
                throw new JsonParseException("Sponsor list entries must map names to non-empty strings");
            }
            result.put(entry.getKey(), value.getAsString());
        }
        return Collections.unmodifiableMap(result);
    }
}
