package de.themoep.tftoverlay.data.Providers;
/*
 * TFT-Overlay - TFT-Overlay-Java
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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.themoep.tftoverlay.TftOverlay;
import de.themoep.tftoverlay.data.TftChampion;
import de.themoep.tftoverlay.data.TftClass;
import de.themoep.tftoverlay.data.TftItem;
import de.themoep.tftoverlay.data.TftOrigin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public abstract class DataProvider {
    protected final TftOverlay main;
    private final Map<String, TftItem> items = new LinkedHashMap<>();

    private final Table<TftItem, TftItem, TftItem> combinationTable = HashBasedTable.create();

    private final Map<String, TftChampion> champions = new LinkedHashMap<>();
    private final Map<String, TftClass> classes = new LinkedHashMap<>();
    private final Map<String, TftOrigin> origins = new LinkedHashMap<>();

    protected void add(TftItem item) {
        items.put(item.getId(), item);
    }

    protected void add(TftChampion champion) {
        champions.put(champion.getId(), champion);
    }

    protected void add(TftClass tftClass) {
        classes.put(tftClass.getId(), tftClass);
    }

    protected void add(TftOrigin origin) {
        origins.put(origin.getId(), origin);
    }

    public void setupCombinations() {
        for (TftItem item : items.values()) {
            if (!item.getIngredients().isEmpty()) {
                combinationTable.put(item.getIngredients().get(0), item.getIngredients().get(1), item);
                combinationTable.put(item.getIngredients().get(1), item.getIngredients().get(0), item);
            }
        }
    }

    public TftItem getCombination(TftItem item1, TftItem item2) {
        return combinationTable.get(item1, item2);
    }
}