package de.themoep.tftoverlay.data;
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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class TftChampion implements Cacheable {
    private final String id;
    private final String name;
    private final URL iconUrl;
    private final List<TftSynergy> synergies;
    private final int cost;
    private final String health;
    private final String damage;
    private final String dps;
    private final int range;
    private final double speed;
    private final int armor;
    private final int magicResistance;
    private final Spell spell;
    private final boolean pbe;
    private final List<TftItem> recommendedItems = new ArrayList<>();

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("iconUrl", iconUrl.toString());
        map.put("synergies", synergies.stream().map(TftSynergy::getSerId).collect(Collectors.toList()));
        map.put("cost", cost);
        map.put("health", health);
        map.put("damage", damage);
        map.put("dps", dps);
        map.put("range", range);
        map.put("speed", speed);
        map.put("armor", armor);
        for (Map.Entry<String, Object> entry : spell.serialize().entrySet()) {
            map.put("spell-" + entry.getKey(), entry.getValue());
        }
        map.put("magicResistance", magicResistance);
        map.put("pbe", pbe);
        map.put("recommendedItems", recommendedItems.stream().map(TftItem::getId).collect(Collectors.toList()));
        return map;
    }

    public Color getColor() {
        switch (cost) {
            case 1:
                return new Color(128, 128, 128);
            case 2:
                return new Color(17, 178, 136);
            case 3:
                return new Color(32, 122, 199);
            case 4:
                return new Color(196, 64, 218);
        }
        if (cost > 4) {
            return new Color(255, 185, 59);
        }
        return new Color(0, 0, 0);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Spell {
        private final String id;
        private final String name;
        private final URL iconUrl;
        private final String description;
        private final String effect;
        private final String type;
        private final String mana;

        public Map<String, Object> serialize() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("iconUrl", iconUrl.toString());
            map.put("description", description);
            map.put("effect", effect);
            map.put("type", type);
            map.put("mana", mana);
            return map;
        }
    }
}