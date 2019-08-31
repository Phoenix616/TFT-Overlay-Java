package de.themoep.tftoverlay.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

@Getter
@RequiredArgsConstructor
public class TftItem implements Cacheable {
    private final String id;
    private final String name;
    private final URL iconUrl;
    private final String description;
    private final Set<TftItem> ingredient = new LinkedHashSet<>();
    private final List<TftItem> ingredients = new ArrayList<>();
    private final List<TftChampion> champions = new ArrayList<>();

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("iconUrl", iconUrl.toString());
        map.put("description", description);
        map.put("ingredient", ingredient.stream().map(TftItem::getId).collect(Collectors.toList()));
        map.put("ingredients", ingredients.stream().map(TftItem::getId).collect(Collectors.toList()));
        map.put("champions", champions.stream().map(TftChampion::getId).collect(Collectors.toList()));
        return map;
    }

    public TftItem getOtherIngredient(TftItem item) {
        for (TftItem tftItem : ingredients) {
            if (tftItem != item) {
                return tftItem;
            }
        }
        return item;
    }
}