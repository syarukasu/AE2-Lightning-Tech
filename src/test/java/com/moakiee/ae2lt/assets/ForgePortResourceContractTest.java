package com.moakiee.ae2lt.assets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

final class ForgePortResourceContractTest {

    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path JAVA = Path.of("src/main/java/com/moakiee/ae2lt/mixin");

    @Test
    void allJsonResourcesParse() throws Exception {
        try (Stream<Path> paths = Files.walk(RESOURCES)) {
            for (Path path : paths.filter(Files::isRegularFile)
                    .filter(candidate -> candidate.toString().endsWith(".json"))
                    .toList()) {
                try (var reader = Files.newBufferedReader(path)) {
                    JsonParser.parseReader(reader);
                }
            }
        }
    }

    @Test
    void forgeResourcesDoNotContainNeoForgeRecipeIds() throws Exception {
        try (Stream<Path> paths = Files.walk(RESOURCES.resolve("data"))) {
            for (Path path : paths.filter(Files::isRegularFile)
                    .filter(candidate -> candidate.toString().endsWith(".json"))
                    .toList()) {
                assertFalse(
                        Files.readString(path).contains("neoforge:"),
                        path + " still contains a NeoForge-only identifier");
            }
        }
    }

    @Test
    void neoEcoOverloadRecipesUseTheDustTagsPublishedOnForge120() throws Exception {
        Path recipes = RESOURCES.resolve("data/ae2lt/recipes/overload_processing");
        Map<String, List<String>> expectedTags = Map.of(
                "neoeco_crystal_ingot.json", List.of("forge:dusts/energized_crystal"),
                "neoeco_energized_crystal.json", List.of("forge:dusts/energized_crystal"),
                "neoeco_energized_fluix_crystal.json", List.of("forge:dusts/energized_crystal"),
                "neoeco_energized_superconductive_ingot.json",
                        List.of("forge:dusts/energized_fluix_crystal", "forge:dusts/aluminum"));

        for (var entry : expectedTags.entrySet()) {
            Path recipePath = recipes.resolve(entry.getKey());
            var recipe = JsonParser.parseString(Files.readString(recipePath)).getAsJsonObject();
            var tags = StreamSupport.stream(recipe.getAsJsonArray("inputs").spliterator(), false)
                    .map(element -> element.getAsJsonObject().getAsJsonObject("ingredient"))
                    .filter(ingredient -> ingredient.has("tag"))
                    .map(ingredient -> ingredient.get("tag").getAsString())
                    .toList();

            assertTrue(tags.containsAll(entry.getValue()),
                    recipePath + " does not use Neo ECO's Forge 1.20 dust tags");
            assertFalse(tags.stream().anyMatch(tag ->
                            tag.startsWith("c:dusts/energized_") || tag.equals("c:dusts/aluminum")),
                    recipePath + " still uses the NeoForge common-tag namespace for a Neo ECO dust");
        }
    }

    @Test
    void dataPackUsesMinecraft120PluralDirectories() throws Exception {
        List<String> invalidDirectories = List.of("recipe", "loot_table", "advancement", "structure");
        try (Stream<Path> namespaces = Files.list(RESOURCES.resolve("data"))) {
            for (Path namespace : namespaces.filter(Files::isDirectory).toList()) {
                for (String directory : invalidDirectories) {
                    Path candidate = namespace.resolve(directory);
                    if (!Files.isDirectory(candidate)) {
                        continue;
                    }
                    try (Stream<Path> files = Files.walk(candidate)) {
                        assertFalse(
                                files.anyMatch(Files::isRegularFile),
                                candidate + " contains 1.21-style singular data-pack paths");
                    }
                }
            }
        }
    }

    @Test
    void everyConfiguredMixinHasSource() throws Exception {
        assertMixinSourcesExist(RESOURCES.resolve("ae2lt.mixins.json"), JAVA);
        assertMixinSourcesExist(
                RESOURCES.resolve("ae2lt.ae2wtlib.mixins.json"),
                JAVA.resolve("ae2wtlib"));
    }

    @Test
    void everyMixinSourceIsConfigured() throws Exception {
        assertMixinSourcesConfigured(
                RESOURCES.resolve("ae2lt.mixins.json"),
                JAVA,
                JAVA.resolve("ae2wtlib"));
        assertMixinSourcesConfigured(
                RESOURCES.resolve("ae2lt.ae2wtlib.mixins.json"),
                JAVA.resolve("ae2wtlib"),
                null);
    }

    @Test
    void everyMixinConfigLoadsTheGeneratedRefmap() throws Exception {
        for (String fileName : List.of("ae2lt.mixins.json", "ae2lt.ae2wtlib.mixins.json")) {
            var json = JsonParser.parseString(Files.readString(RESOURCES.resolve(fileName))).getAsJsonObject();
            assertTrue(json.has("refmap"), fileName + " does not declare a refmap");
            assertTrue(
                    json.get("refmap").getAsString().equals("ae2lt.refmap.json"),
                    fileName + " declares the wrong refmap");
        }
    }

    @Test
    void attributionIdentifiesBothUpstreamsAndTheUnofficialFork() throws Exception {
        String credits = Files.readString(Path.of("CREDITS.md"));
        assertTrue(credits.contains("MOAKIEE"), "original AE2LT creator is not credited");
        assertTrue(
                credits.contains("TeamAppliedEnergistics"),
                "Applied Energistics 2 team is not credited");
        assertTrue(credits.contains("unofficial"), "fork status is not disclosed");

        String modsToml = Files.readString(RESOURCES.resolve("META-INF/mods.toml"));
        assertTrue(modsToml.contains("credits = \"${mod_credits}\""));
    }

    private static void assertMixinSourcesExist(Path config, Path packageRoot) throws Exception {
        var json = JsonParser.parseString(Files.readString(config)).getAsJsonObject();
        for (String section : List.of("mixins", "client")) {
            if (!json.has(section)) {
                continue;
            }
            for (var element : json.getAsJsonArray(section)) {
                String className = element.getAsString();
                Path source = packageRoot.resolve(className.replace('.', '/') + ".java");
                assertTrue(Files.isRegularFile(source), className + " is configured but has no source file");
            }
        }
    }

    private static void assertMixinSourcesConfigured(Path config, Path packageRoot, Path excludedRoot)
            throws Exception {
        var json = JsonParser.parseString(Files.readString(config)).getAsJsonObject();
        var configured = new java.util.HashSet<String>();
        for (String section : List.of("mixins", "client")) {
            if (!json.has(section)) {
                continue;
            }
            for (var element : json.getAsJsonArray(section)) {
                configured.add(element.getAsString());
            }
        }

        try (Stream<Path> paths = Files.walk(packageRoot)) {
            for (Path source : paths.filter(Files::isRegularFile)
                    .filter(candidate -> candidate.toString().endsWith(".java"))
                    .filter(candidate -> excludedRoot == null || !candidate.startsWith(excludedRoot))
                    .filter(candidate -> {
                        try {
                            return Files.readString(candidate).matches("(?s).*@Mixin\\s*\\(.*");
                        } catch (Exception exception) {
                            throw new RuntimeException(exception);
                        }
                    })
                    .toList()) {
                String className = packageRoot.relativize(source).toString()
                        .replace('\\', '.')
                        .replace('/', '.')
                        .replaceFirst("\\.java$", "");
                assertTrue(configured.contains(className), className + " is a mixin source but is not configured");
            }
        }
    }
}
