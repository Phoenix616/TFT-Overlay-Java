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
 * This program is distributed : the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.themoep.tftoverlay.TftOverlay;
import de.themoep.tftoverlay.Utils;
import de.themoep.tftoverlay.data.TftChampion;
import de.themoep.tftoverlay.data.TftClass;
import de.themoep.tftoverlay.data.TftItem;
import de.themoep.tftoverlay.data.TftOrigin;
import de.themoep.tftoverlay.data.TftSynergy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoLChessGgProvider extends DataProvider {

    public LoLChessGgProvider(TftOverlay main) {
        super(main);
        // Query data from lolchess.gg
        queryData();
    }

    private void queryData() {
        Map<String, String> headers = Utils.map("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; " + main.getName() + " " + main.getVersion() + ")");

        try {
            main.getLoading().addLine("Loading items...");

            // items: https://lolchess.gg/items -> Item List class=guide-items-table
            Document document = Jsoup.connect("https://lolchess.gg/items").headers(headers).get();
            for (Element element : document.select(".guide-items-table tbody tr")) {
                Element image = element.select("td img").get(0);
                String name = image.attr("alt");
                TftItem item = new TftItem(
                        name.toLowerCase(),
                        name,
                        getSource(image),
                        element.select(".desc").get(0).text().trim()
                );

                for (Element ingredientImage : element.select(".combination img")) {
                    TftItem ingredientItem = getItems().get(ingredientImage.attr("alt").toLowerCase());
                    if (ingredientItem != null) {
                        item.getIngredients().add(ingredientItem);
                        ingredientItem.getIngredient().add(item);
                    }
                }
                getItems().put(item.getId(), item);

                main.getLogger().info("Loaded Item " + item.getName() + "!");
            }

            main.getLoading().addLine("Loading synergies...");

            // synergies: https://lolchess.gg/synergies
            document = Jsoup.connect("https://lolchess.gg/synergies").headers(headers).get();
            for (Element element : document.select(".guide-synergy-table .guide-synergy-table__container")) {
                // "Origins" or "Classes"
                String type = element.select(".guide-synergy-table__header").get(0).text().trim();
                for (Element entryElement : element.select(".guide-synergy-table__synergy")) {
                    String id = entryElement.className().substring("guide-synergy-table__synergy guide-synergy-table__synergy--".length());
                    Element img = entryElement.select(".tft-hexagon img").get(0);
                    Elements descElements = entryElement.select(".guide-synergy-table__synergy__desc");
                    String description = descElements.isEmpty() ? "" : descElements.get(0).text().trim();
                    String effects = trim(entryElement.select(".guide-synergy-table__synergy__stats").get(0).text());
                    switch (type) {
                        case "Origins":
                            getOrigins().put(id, new TftOrigin(id, img.attr("alt"), getSource(img), description, effects));
                            break;
                        case "Classes":
                            getClasses().put(id, new TftClass(id, img.attr("alt"), getSource(img), description, effects));
                            break;
                        default:
                            System.out.println("Unknown synergy type " + type);
                            break;
                    }
                }
            }

            main.getLoading().addLine("Loading champions...");

            // champions https://lolchess.gg/champions/anivia -> class=guide-champion-table
            String link = "https://lolchess.gg/champions/aatrox";
            document = Jsoup.connect(link).headers(headers).get();
            for (Element element : document.select(".guide-champion-list .guide-champion-list__content a.guide-champion-list__item[href]")) {
                Document champDoc;
                String champLink = element.attr("href");
                if (!champLink.equals(link)) {
                    try {
                        Thread.sleep((long) (628 + Math.random() * 1000));
                    } catch (InterruptedException ignored) {}
                    champDoc = Jsoup.connect(champLink).headers(headers).get();
                } else {
                    champDoc = document;
                }

                Element iconElement = element.select(".guide-champion-item img").get(0);

                Elements statsElements = champDoc.select(".guide-champion-detail__base-stats .guide-champion-detail__base-stat");
                Element spellElement = champDoc.select(".guide-champion-detail__skill").get(0);
                Element spellImg = spellElement.select("img.guide-champion-detail__skill__icon").get(0);

                List<TftSynergy> synergies = new ArrayList<>();

                Elements detailStatsElements = champDoc.select(".guide-champion-detail__stats .guide-champion-detail__stats__row");
                for (Element originImage : detailStatsElements.get(1).select(".guide-champion-detail__stats__value img[alt]")) {
                    synergies.add(getOrigins().get(originImage.attr("alt").toLowerCase()));
                }
                for (Element classImage : detailStatsElements.get(2).select(".guide-champion-detail__stats__value img[alt]")) {
                    synergies.add(getClasses().get(classImage.attr("alt").toLowerCase()));
                }

                String id = champLink.substring("https://lolchess.gg/champions/".length());

                double speed = 0.0;
                int armor = 0;
                int magicResistance = 20;
                int range = 1;
                int cost = 0;
                try {
                    speed = Double.parseDouble(trim(statsElements.get(4).select(".guide-champion-detail__base-stat__value").get(0).text()));
                    armor = Integer.parseInt(trim(statsElements.get(5).select(".guide-champion-detail__base-stat__value").get(0).text()));
                    magicResistance = Integer.parseInt(trim(statsElements.get(6).select(".guide-champion-detail__base-stat__value").get(0).text()));
                    int rangeSubLength = "https://cdn.lolchess.gg/images/icon/ico-attack-distance-0".length();
                    URL imgSource = getSource(statsElements.get(3).select(".guide-champion-detail__base-stat__value img").get(0));
                    range = Integer.parseInt(imgSource.toString().substring(rangeSubLength, rangeSubLength + 1));
                    cost = Integer.parseInt(element.select(".guide-champion-item .cost").get(0).text().substring(1));
                } catch (NumberFormatException | MalformedURLException ex) {
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                    //MessageBox.Show(ex.ToString(), "An error occured loading data", MessageBoxButton.OK, MessageBoxImage.Error);
                }

                String name = champDoc.select(".guide-champion-detail__name").get(0).text();
                String health = TrimMulti(statsElements.get(0).select(".guide-champion-detail__base-stat__value").get(0).text());
                String spellMana = trim(spellElement.select("div.text-gray img+span").get(0).text()).substring("Mana: ".length());
                String damage = TrimMulti(statsElements.get(1).select(".guide-champion-detail__base-stat__value").get(0).text());
                String dps = TrimMulti(statsElements.get(2).select(".guide-champion-detail__base-stat__value").get(0).text());

                Elements spellDescriptionElements = spellElement.select("span.d-block.mt-1");
                String spellDescription = spellDescriptionElements.isEmpty() ? "" : trim(spellDescriptionElements.get(0).text());

                Elements spellEffectElements = spellElement.select(".guide-champion-detail__skill__stats");
                String spellEffect = spellEffectElements.isEmpty() ? "" : trim(spellEffectElements.get(0).text());

                String spellType = trim(spellElement.select("div.text-gray span").get(0).text());

                TftChampion.Spell spell = new TftChampion.Spell(spellImg.attr("alt").toLowerCase(), spellImg.attr("alt"),
                        getSource(spellImg), spellDescription, spellEffect, spellType, spellMana);
                boolean pbe = !element.select(".guide-champion-item.pbe").isEmpty();

                TftChampion champion = new TftChampion(id, name, getSource(iconElement), synergies, cost, health,
                        damage, dps, range, speed, armor, magicResistance, spell, pbe);

                for (Element itemElement : champDoc.select(".guide-champion-detail__recommend-items__content div")) {
                    String itemId = itemElement.select("img[alt]").get(0).attr("alt").toLowerCase();
                    TftItem item = getItems().get(itemId);
                    if (item != null) {
                        champion.getRecommendedItems().add(item);
                        item.getChampions().add(champion);
                    } else {
                        main.getLogger().warning("No item with id " + itemId + " found for champion " + champion.getId());
                    }
                }

                getChampions().put(champion.getId(), champion);

                for (TftSynergy synergy : champion.getSynergies()) {
                    synergy.getChampions().add(champion);
                }

                main.getLogger().info("Loaded Champion " + champion.getName() + "!");
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "An error occured loading data", JOptionPane.ERROR_MESSAGE);
        }
    }

    private URL getSource(Element image) throws MalformedURLException {
        if (image.attr("src").startsWith("about:")) {
            return new URL("https:" + image.attr("src").substring("about:".length()));
        } else if (image.attr("src").startsWith("//")) {
            return new URL("https:" + image.attr("src"));
        }

        return new URL(image.attr("src"));
    }

    private String TrimMulti(String text) {
        text = trim(text);
        return text.substring(0, text.lastIndexOf(' '));
    }

    private String trim(String text) {
        StringBuilder r = new StringBuilder();
        for (String s : text.trim().split("\n")) {
            if (s.trim().length() > 0) {
                if (r.length() > 0) {
                    r.append("\n");
                }
                r.append(s.trim());
            }
        }

        return r.toString();
    }
}