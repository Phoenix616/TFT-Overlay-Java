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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class TftSynergy implements Cacheable {
    private final String type;
    private final String id;
    private final String name;
    private final URL iconUrl;
    private final String description;
    private final String effects;
    private final Collection<TftChampion> champions = new ArrayList<>();

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", type);
        map.put("id", id);
        map.put("name", name);
        map.put("iconUrl", iconUrl.toString());
        map.put("description", description);
        map.put("effects", effects);
        map.put("champions", champions.stream().map(TftChampion::getId).collect(Collectors.toList()));
        return map;
    }

    public String getSerId() {
        return getType().charAt(0) + ":" + getId();
    }
}