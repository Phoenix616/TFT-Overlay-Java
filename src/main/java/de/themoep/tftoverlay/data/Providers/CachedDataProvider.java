package de.themoep.tftoverlay.data.Providers;

/*
 * TFT-Overlay - tftoverlay - $project.description
 * Copyright (c) 2019 Max Lee aka Phoenix616 (mail@moep.tv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.themoep.tftoverlay.TftOverlay;
import de.themoep.tftoverlay.data.TftChampion;
import de.themoep.tftoverlay.data.TftClass;
import de.themoep.tftoverlay.data.TftItem;
import de.themoep.tftoverlay.data.TftOrigin;
import de.themoep.tftoverlay.data.TftSynergy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class CachedDataProvider extends DataProvider {

    private final File cacheFolder;

    public CachedDataProvider(TftOverlay main, File cacheFolder) throws IOException {
        super(main);
        this.cacheFolder = cacheFolder;
        load();
    }

    private void load() throws IOException {
        Files.newDirectoryStream(new File(cacheFolder, "items").toPath()).forEach(path -> {
            if (!path.toFile().isFile()) {
                return;
            }
            Properties props = new Properties();
            try {
                props.load(new FileReader(path.toFile()));
                TftItem item = new TftItem(
                        props.getProperty("id"),
                        props.getProperty("name"),
                        new URL(props.getProperty("iconUrl")),
                        props.getProperty("description")
                );
                for (String s : props.getProperty("ingredients").split(",")) {
                    s = cleanUp(s);
                    TftItem ingredient = getItems().get(s);
                    if (ingredient != null) {
                        item.getIngredients().add(ingredient);
                        ingredient.getIngredient().add(item);
                    }
                }
                for (String s : props.getProperty("ingredient").split(",")) {
                    s = cleanUp(s);
                    TftItem ingredientTo = getItems().get(s);
                    if (ingredientTo != null) {
                        item.getIngredient().add(ingredientTo);
                        ingredientTo.getIngredients().add(item);
                    }
                }
                add(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Files.newDirectoryStream(new File(cacheFolder, "origins").toPath()).forEach(path -> {
            if (!path.toFile().isFile()) {
                return;
            }
            Properties props = new Properties();
            try {
                props.load(new FileReader(path.toFile()));
                add(new TftOrigin(
                        props.getProperty("id"),
                        props.getProperty("name"),
                        new URL(props.getProperty("iconUrl")),
                        props.getProperty("description") != null ? props.getProperty("description") : "",
                        props.getProperty("effects") != null ? props.getProperty("effects") : ""
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Files.newDirectoryStream(new File(cacheFolder, "classes").toPath()).forEach(path -> {
            if (!path.toFile().isFile()) {
                return;
            }
            Properties props = new Properties();
            try {
                props.load(new FileReader(path.toFile()));
                add(new TftClass(
                        props.getProperty("id"),
                        props.getProperty("name"),
                        new URL(props.getProperty("iconUrl")),
                        props.getProperty("description") != null ? props.getProperty("description") : "",
                        props.getProperty("effects") != null ? props.getProperty("effects") : ""
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Files.newDirectoryStream(new File(cacheFolder, "champions").toPath()).forEach(path -> {
            if (!path.toFile().isFile()) {
                return;
            }
            Properties props = new Properties();
            try {
                props.load(new FileReader(path.toFile()));
                TftChampion champion = new TftChampion(
                        props.getProperty("id"),
                        props.getProperty("name"),
                        new URL(props.getProperty("iconUrl")),
                        Arrays.stream(props.getProperty("synergies").split(",")).map(s -> {
                            String[] parts = cleanUp(s).split(":");
                            if (parts.length > 1) {
                                switch (parts[0]) {
                                    case "o":
                                        return getOrigins().get(parts[1]);
                                    case "c":
                                        return getClasses().get(parts[1]);
                                }
                            }
                            main.getLogger().warning("Invalid synergy " + s + " found for champion " + props.getProperty("id"));
                            return null;
                        }).filter(Objects::nonNull).collect(Collectors.toList()),
                        getInt(props, "cost"),
                        props.getProperty("health"),
                        props.getProperty("damage"),
                        props.getProperty("dps"),
                        getInt(props, "range"),
                        getDouble(props, "speed"),
                        getInt(props, "armor"),
                        getInt(props, "magicResistance"),
                        new TftChampion.Spell(
                                props.getProperty("spell-id"),
                                props.getProperty("spell-name"),
                                new URL(props.getProperty("spell-iconUrl")),
                                props.getProperty("spell-description"),
                                props.getProperty("spell-effect"),
                                props.getProperty("spell-typ"),
                                props.getProperty("spell-mana")
                        ),
                        props.getProperty("pbe").equals("true")
                );
                for (String s : props.getProperty("recommendedItems").split(",")) {
                    TftItem item = getItems().get(cleanUp(s));
                    if (item != null) {
                        champion.getRecommendedItems().add(item);
                        item.getChampions().add(champion);
                    } else {
                        main.getLogger().warning("No item with ID " + s + " found for champion " + champion.getId());
                    }
                }
                for (TftSynergy synergy : champion.getSynergies()) {
                    synergy.getChampions().add(champion);
                }
                add(champion);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String cleanUp(String s) {
        if (s.startsWith("[")) {
            s = s.substring(1);
        }
        if (s.endsWith("]")) {
            s = s.substring(0, s.length() - 1);
        }
        return s.trim();
    }

    private int getInt(Properties properties, String key) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private double getDouble(Properties properties, String key) {
        try {
            return Double.parseDouble(properties.getProperty(key));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
