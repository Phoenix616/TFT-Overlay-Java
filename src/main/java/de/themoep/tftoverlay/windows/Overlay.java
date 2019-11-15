package de.themoep.tftoverlay.windows;
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
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.tftoverlay.TftOverlay;
import de.themoep.tftoverlay.Utils;
import de.themoep.tftoverlay.data.Cacheable;
import de.themoep.tftoverlay.data.TftChampion;
import de.themoep.tftoverlay.data.TftItem;
import de.themoep.tftoverlay.data.TftSynergy;
import de.themoep.tftoverlay.elements.LabelButton;
import de.themoep.tftoverlay.elements.LabelCheckbox;
import de.themoep.tftoverlay.elements.TabbedPanel;
import de.themoep.tftoverlay.elements.WindowMover;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Overlay extends JFrame {

    public static final boolean PBE = false;

    public static final Font FONT = new Font("Sans-Serif", Font.PLAIN, 12);
    public static final Font HUGE_FONT = FONT.deriveFont(Font.BOLD, 28);
    public static final Color BACKGROUND = new Color(10, 10, 10);
    public static final Color HOVER_BACKGROUND = new Color(10, 10, 10, 200);
    public static final Color GRID_COLOR = new Color(36, 36, 36);
    public static final Color TEXT_COLOR = new Color(230, 198, 123);
    public static final Color SECONDARY_TEXT_COLOR = new Color(230, 216, 183);
    public static final Border COLORED_BORDER = BorderFactory.createLineBorder(new Color(117, 87, 41));
    public static final Border BORDER = BorderFactory.createCompoundBorder(COLORED_BORDER, BorderFactory.createEmptyBorder(5, 5, 5, 5));
    public static final Border BUTTON_BORDER = BorderFactory.createCompoundBorder(BORDER, new EmptyBorder(0, 5, 0, 5));

    public static final int ICON_SIZE = 42;
    private final TftOverlay main;

    public Overlay(TftOverlay main) {
        super(main.getName());
        this.main = main;
        setIconImage(main.getIcon());

        setCursor(TftOverlay.CURSOR);

        JPanel content = new JPanel();
        setContentPane(content);
        content.setFont(FONT);
        content.setOpaque(false);

        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(145, 145, 145, 0));

        UIManager.put("TabbedPane.font", FONT);
        UIManager.put("ToolTip.background", new ColorUIResource(BACKGROUND));
        UIManager.put("ToolTip.backgroundInactive", new ColorUIResource(BACKGROUND));
        UIManager.put("ToolTip.border", BORDER);
        UIManager.put("ToolTip.foreground", TEXT_COLOR);
        UIManager.put("ToolTip.foregroundInactive", new ColorUIResource(BACKGROUND));
        UIManager.put("ToolTip.width", 100);
        ToolTipManager.sharedInstance().setInitialDelay(0);

        // --- Main Panel setup ---

        TabbedPanel mainPanel = new TabbedPanel(this);

        // --- Menu ---

        WindowMover menuElement = new WindowMover(this);
        menuElement.setPreferredSize(new Dimension(10, ICON_SIZE - 2));
        menuElement.setBackground(TEXT_COLOR);
        LabelCheckbox recordingCheckbox = new LabelCheckbox(main.getLang("recording-window"), b -> {
            if (b) {
                main.getRecordingWindow().start();
            } else {
                main.getRecordingWindow().stop();
            }
        });
        menuElement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu menu = new JPopupMenu("menu");
                    menu.setCursor(TftOverlay.CURSOR);
                    menu.setBackground(BACKGROUND);
                    menu.setBorder(COLORED_BORDER);
                    menu.setLayout(new GridLayout(0, 1));
                    for (String s : main.getLang("credits", "version", main.getVersion().split(" ")[0]).split("\n")) {
                        JLabel credits = new JLabel(s);
                        menu.add(credits);
                        Border inset = BorderFactory.createEmptyBorder(5, 10, 0, 10);
                        credits.setBorder(inset);
                        credits.setForeground(TEXT_COLOR);
                    }

                    menu.add(recordingCheckbox);
                    menu.add(new LabelButton(main.getLang("update-data"), c -> main.start(true)));
                    menu.add(new LabelButton(main.getLang("close"), c -> System.exit(1)));
                    menu.pack();
                    menu.show(menuElement, 10, 0);
                }
            }
        });
        menuElement.setToolTipText(main.getLang("menu"));
        mainPanel.getHeader().add(menuElement);

        addChampionsPopup(mainPanel);

        // --- Items ---

        addItemsPopup(mainPanel);
        addItemsPopups(mainPanel);

        Utils.addChild(content, mainPanel);

        pack();
    }

    private void addChampionsPopup(TabbedPanel mainPanel) {
        TabbedPanel champPopup = new TabbedPanel(mainPanel.getParent(), false, true);
        champPopup.setBackground(HOVER_BACKGROUND);

        Multimap<String, TftSynergy> synergyTypes = MultimapBuilder.hashKeys().arrayListValues().build();
        for (TftSynergy synergy : main.getProvider().getSynergies().values()) {
            synergyTypes.put(synergy.getType(), synergy);
        }

        // -- Champions panel --
        addChampionsGridPopup(champPopup, synergyTypes);
        addListPopup(main.getProvider().getChampions(), champPopup, 6, (input, c) -> {
            if (input.isEmpty() || c.getId().startsWith(input))
                return true;

            if (input.length() > 2) {
                if (c.getId().contains(input))
                    return true;

                if (c.getSpell() != null)
                    if (c.getSpell().getEffect().toLowerCase().contains(input) || c.getSpell().getDescription().toLowerCase().contains(input))
                        return true;

                for (TftItem item : c.getRecommendedItems())
                    if (item.getId().contains(input))
                        return true;
            }

            return false;
        }, (c, entryPanel) -> {
            JLabel icon = getChampionIcon(c, 36);
            entryPanel.setToolTipText(icon.getToolTipText());
            entryPanel.add(icon);

            Utils.addChild(entryPanel, new JLabel(main.getLang("champion-info", getReplacements(c))));
            Utils.addChild(entryPanel, new JLabel(main.getLang("spell-info", getReplacements(c))));

            if (!c.getRecommendedItems().isEmpty()) {
                JPanel itemsPanel = new JPanel();
                itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
                Utils.addChild(entryPanel, itemsPanel);
                Utils.addChild(itemsPanel, new JLabel(main.getLang("recommended-items")));
                c.getRecommendedItems().stream().sorted(Comparator.comparing(TftItem::getId)).forEach(item -> {
                    JLabel itemIcon = new JLabel(item.getName(), new ImageIcon(main.getImage(item.getIconUrl(), 24, 24)), SwingConstants.LEADING);
                    Utils.addTooltip(itemIcon, main.getLang("item-hover-short", getReplacements(item)));
                    Utils.addChild(itemsPanel, itemIcon);
                    itemIcon.setForeground(SECONDARY_TEXT_COLOR);
                });
            }
        });

        // -- Synergies --
        for (String synergyName : synergyTypes.keySet()) {
            addSynergyPopup(champPopup, synergyTypes.get(synergyName), synergyName);
        }

        mainPanel.addEntry("champions", getCharButton("C"), champPopup);
    }

    private void addChampionsGridPopup(TabbedPanel parent, Multimap<String, TftSynergy> synergyTypes) {
        JPanel popup = new JPanel();
        Iterator<Collection<TftSynergy>> it = synergyTypes.asMap().values().iterator();
        Collection<TftSynergy> synOnes = it.next();
        Collection<TftSynergy> synTwos = it.next();
        popup.setLayout(new GridLayout(synTwos.size() + 1, synOnes.size() + 1));
        popup.setForeground(TEXT_COLOR);
        popup.setBackground(new Color(0, 0, 0, 0));
        JPanel cornerCell = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(GRID_COLOR);
                g.drawLine(0, 0, super.getWidth(), super.getHeight());
                super.paintComponent(g);
            }
        };
        Utils.addChild(popup, cornerCell);
        cornerCell.setForeground(SECONDARY_TEXT_COLOR);
        cornerCell.setLayout(new BoxLayout(cornerCell, BoxLayout.Y_AXIS));

        JPanel synOneContainer = new JPanel();
        synOneContainer.setLayout(new BoxLayout(synOneContainer, BoxLayout.X_AXIS));
        Utils.addChild(cornerCell, synOneContainer);
        synOneContainer.add(Box.createHorizontalGlue());
        Utils.addChild(synOneContainer, new JLabel(synOnes.iterator().next().getType()));

        JPanel synTwoContainer = new JPanel();
        synTwoContainer.setLayout(new BoxLayout(synTwoContainer, BoxLayout.X_AXIS));
        Utils.addChild(cornerCell, synTwoContainer);
        Utils.addChild(synTwoContainer, new JLabel(synTwos.iterator().next().getType()));
        synTwoContainer.add(Box.createHorizontalGlue());

        for (TftSynergy synergy : synOnes) {
            JLabel synOneLabel = new JLabel(synergy.getName(), new ImageIcon(main.getImage(synergy.getIconUrl(), 16, 16)), JLabel.CENTER);
            synOneLabel.setHorizontalTextPosition(JLabel.CENTER);
            synOneLabel.setVerticalTextPosition(JLabel.TOP);
            Utils.addTooltip(synOneLabel, main.getLang("synergy-hover",
                    "type", synergy.getType(),
                    "name", synergy.getName(),
                    "iconUrl", synergy.getIconUrl().toExternalForm(),
                    "iconPath", "file:/" + main.getCachedImageFile(synergy.getIconUrl(), 16, 16).getAbsolutePath(),
                    "desc", synergy.getDescription() + (!synergy.getDescription().isEmpty() && !synergy.getEffects().isEmpty() ? "<br>" : ""),
                    "effects", synergy.getEffects(),
                    "champions", synergy.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", ")),
                    "champion-count", String.valueOf(synergy.getChampions().size())
            ));
            synOneLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(27, 27, 27)));
            Utils.addChild(popup, synOneLabel);
        }

        for (TftSynergy synTwo : synTwos) {
            JLabel synTwolabel = new JLabel(synTwo.getName(), new ImageIcon(main.getImage(synTwo.getIconUrl(), 16, 16)), JLabel.CENTER);
            synTwolabel.setHorizontalTextPosition(JLabel.CENTER);
            synTwolabel.setVerticalTextPosition(JLabel.TOP);
            Utils.addTooltip(synTwolabel, main.getLang("synergy-hover",
                    "type", synTwo.getType(),
                    "name", synTwo.getName(),
                    "iconUrl", synTwo.getIconUrl().toExternalForm(),
                    "iconPath", "file:/" + main.getCachedImageFile(synTwo.getIconUrl(), 16, 16).getAbsolutePath(),
                    "desc", synTwo.getDescription() + (!synTwo.getDescription().isEmpty() && !synTwo.getEffects().isEmpty() ? "<br>" : ""),
                    "effects", synTwo.getEffects(),
                    "champions", synTwo.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", ")),
                    "champion-count", String.valueOf(synTwo.getChampions().size())
            ));
            synTwolabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(27, 27, 27)));
            Utils.addChild(popup, synTwolabel);

            for (TftSynergy synOne : synOnes) {
                JPanel champCell = new JPanel();
                champCell.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, GRID_COLOR));

                List<TftChampion> championList = synOne.getChampions().stream()
                        .filter(c -> c.getSynergies().contains(synTwo))
                        .filter(c -> !c.isPbe() || PBE)
                        .sorted(Comparator.comparingInt(TftChampion::getCost))
                        .collect(Collectors.toList());

                int size = championList.size() > 0 ? 48 / championList.size() : 0;
                if (size > 24) {
                    size = 24;
                }

                for (TftChampion champion : championList) {
                    champCell.add(getChampionIcon(champion, size));
                }
                Utils.addChild(popup, champCell);
            }
        }

        parent.addEntry(main.getLang("grid"), getTextButton(main.getLang("grid"), FONT), popup, true);
    }

    private <T extends Cacheable> void addListPopup(Map<String, T> entries, TabbedPanel parent,
                                                    int show, BiFunction<String, T, Boolean> searchFilter,
                                                    BiConsumer<T, JPanel> entryCreator) {
        JPanel popup = new JPanel();
        popup.setLayout(new BoxLayout(popup, BoxLayout.Y_AXIS));
        popup.setForeground(TEXT_COLOR);
        popup.setBackground(new Color(0, 0, 0, 0));

        Map<T, JPanel> entryPanels = new LinkedHashMap<>();

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        Utils.addChild(popup, header);

        JTextField searchBar = new JTextField();
        searchBar.setToolTipText(main.getLang("search"));
        Consumer<String> inputHandler = input -> {
            Set<String> found = entries.values().stream()
                    .filter(entry -> searchFilter.apply(input, entry))
                    .map(Cacheable::getId)
                    .collect(Collectors.toSet());

            int shown = 0;
            for (Map.Entry<T, JPanel> entry : entryPanels.entrySet()) {
                if (shown < show && (found.isEmpty() || found.contains(entry.getKey().getId()))) {
                    entry.getValue().setVisible(true);
                    shown++;
                } else {
                    entry.getValue().setVisible(false);
                }
                pack();
            }
        };
        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                SwingUtilities.invokeLater(() -> inputHandler.accept(searchBar.getText().toLowerCase()));
            }
        });
        searchBar.setBorder(BORDER);
        searchBar.setFont(HUGE_FONT);
        Utils.addChild(header, searchBar);

        LabelButton searchButton = new LabelButton(main.getLang("search"), c -> inputHandler.accept(searchBar.getText().toLowerCase()));
        searchButton.setFont(HUGE_FONT);
        header.add(searchButton);

        searchBar.setBackground(BACKGROUND);

        JPanel entryList = new JPanel();
        entryList.setLayout(new BoxLayout(entryList, BoxLayout.Y_AXIS));
        Utils.addChild(popup, entryList);
        //champList.setBackground(BACKGROUND);
        //champList.setForeground(TEXT_COLOR);
        //JScrollPane champScroll = new JScrollPane(champList);
        //champScroll.setPreferredSize(new Dimension(420, 640));
        //Utils.addChild(popup, champScroll);

        entries.values().stream().sorted(Comparator.comparing(Cacheable::getId)).forEach(c -> {
            JPanel entryPanel = new JPanel();
            entryPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            entryPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GRID_COLOR));
            Utils.addChild(entryList, entryPanel);

            entryCreator.accept(c, entryPanel);

            entryPanels.put(c, entryPanel);
        });

        int shown = 0;
        for (Map.Entry<T, JPanel> entry : entryPanels.entrySet()) {
            if (shown < show && searchFilter.apply("", entry.getKey())) {
                shown++;
            } else {
                entry.getValue().setVisible(false);
            }
        }

        parent.addEntry(main.getLang("list"), getTextButton(main.getLang("list"), FONT), popup);
    }

    private void addSynergyPopup(TabbedPanel parent, Collection<? extends TftSynergy> synergies, String typeName) {
        JPanel popup = new JPanel();
        popup.setLayout(new BoxLayout(popup, BoxLayout.Y_AXIS));
        popup.setBackground(new Color(0, 0, 0, 0));

        for (TftSynergy synergy : synergies) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)), BorderFactory.createEmptyBorder(3, 0, 3, 0)));

            List<TftChampion> championList = synergy.getChampions().stream()
                    .filter(c -> !c.isPbe() || PBE)
                    .sorted(Comparator.comparingInt(TftChampion::getCost))
                    .collect(Collectors.toList());

            JLabel infoLabel = new JLabel(main.getLang("synergy-info",
                    "name", synergy.getName(),
                    "desc", Utils.addHilights(synergy.getDescription()) + (!synergy.getDescription().isEmpty() && !synergy.getEffects().isEmpty() ? "<br>" : ""),
                    "effects", Utils.addHilights(synergy.getEffects()),
                    "champions", synergy.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", ")),
                    "champion-count", String.valueOf(championList.size())
            ), new ImageIcon(main.getImage(synergy.getIconUrl(), 24, 24)), JLabel.LEADING);
            infoLabel.setHorizontalAlignment(SwingConstants.LEADING);
            panel.add(infoLabel);
            infoLabel.setFont(FONT);

            JPanel champPanelContainer = new JPanel();
            JPanel champPanel = new JPanel();
            champPanel.setLayout(new GridLayout(championList.size() > 3 ? 2 : 1, 0));
            for (int i = 0; i < championList.size(); i++) {
                champPanel.add(getChampionIcon(championList.get(i), 24));
                if (championList.size() > 3 && championList.size() % 2 > 0 && (championList.size() / 2 == i)) {
                    champPanel.add(new JLabel());
                }
            }
            Utils.addChild(champPanelContainer, champPanel);
            Utils.addChild(panel, champPanelContainer);

            Utils.addChild(popup, panel);
        }

        parent.addEntry(typeName, getTextButton(typeName, FONT), popup);
    }

    private void addItemsPopup(TabbedPanel mainPanel) {
        TabbedPanel itemsPopup = new TabbedPanel(mainPanel.getParent(), false, true);
        itemsPopup.setBackground(HOVER_BACKGROUND);

        // -- Items panel --
        addItemBuilderPopup(itemsPopup);
        addListPopup(main.getProvider().getItems(), itemsPopup, 10, (input, i) -> {
            if (input.isEmpty())
                return i.getIngredients().isEmpty();

            if (i.getId().startsWith(input))
                return true;

            if (input.length() > 2) {

                for (TftItem ingredient : i.getIngredients()) {
                    if (ingredient.getId().contains(input))
                        return true;
                }

                for (TftItem ingredient : i.getIngredient()) {
                    if (ingredient.getId().contains(input))
                        return true;
                }
                if (i.getId().contains(input))
                    return true;

                if (i.getDescription().toLowerCase().contains(input))
                    return true;

                for (TftChampion champion : i.getChampions())
                    if (champion.getId().contains(input))
                        return true;
            }

            return false;
        }, (item, entryPanel) -> {
            JLabel itemIcon = new JLabel(new ImageIcon(main.getImage(item.getIconUrl(), 36, 36)));
            itemIcon.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16), COLORED_BORDER));
            String[] replacements = getReplacements(item);
            entryPanel.add(itemIcon);

            Utils.addChild(entryPanel, new JLabel(main.getLang("item-info", replacements)));

            if (!item.getIngredient().isEmpty()) {
                JPanel itemsPanel = new JPanel();
                itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
                Utils.addChild(entryPanel, itemsPanel);
                Utils.addChild(itemsPanel, new JLabel(main.getLang("item-ingredient-for")));

                JPanel itemsIcons = new JPanel();
                Utils.addChild(itemsPanel, itemsIcons);
                itemsIcons.setLayout(new BoxLayout(itemsIcons, BoxLayout.X_AXIS));

                item.getIngredient().stream().sorted(Comparator.comparing(TftItem::getId)).forEach(craftable -> {
                    JLabel ingredientIcon = new JLabel(new ImageIcon(main.getImage(craftable.getIconUrl(), 24, 24)));
                    Utils.addTooltip(ingredientIcon, main.getLang("item-hover", getReplacements(craftable)));
                    itemsIcons.add(ingredientIcon);
                });
            }
            if (!item.getIngredients().isEmpty()) {
                JPanel itemsPanel = new JPanel();
                itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
                Utils.addChild(entryPanel, itemsPanel);
                Utils.addChild(itemsPanel, new JLabel(main.getLang("item-ingredients")));
                for (TftItem ingredient : item.getIngredients()) {
                    JLabel ingredientIcon = new JLabel(ingredient.getName(), new ImageIcon(main.getImage(ingredient.getIconUrl(), 24, 24)), SwingConstants.LEADING);
                    Utils.addTooltip(ingredientIcon, main.getLang("item-hover-short", getReplacements(ingredient)));
                    Utils.addChild(itemsPanel, ingredientIcon);
                    ingredientIcon.setForeground(SECONDARY_TEXT_COLOR);
                }
            }
            if (!item.getChampions().isEmpty()) {
                JPanel champPanel = new JPanel();
                champPanel.setLayout(new BoxLayout(champPanel, BoxLayout.Y_AXIS));
                Utils.addChild(entryPanel, champPanel);
                Utils.addChild(champPanel, new JLabel(main.getLang("recommended-champs-title")));

                JPanel champIcons = new JPanel();
                Utils.addChild(champPanel, champIcons);
                champIcons.setLayout(new GridLayout(item.getChampions().size() > 3 ? 2 : 1, 0));
                for (TftChampion champion : item.getChampions()) {
                    champIcons.add(getChampionIcon(champion, 24));
                }
            }
        });

        mainPanel.addEntry("items", getCharButton("I"), itemsPopup);
    }

    private void addItemBuilderPopup(TabbedPanel parent) {
        JPanel popup = new JPanel();
        popup.setLayout(new BoxLayout(popup, BoxLayout.X_AXIS));
        popup.setForeground(TEXT_COLOR);
        popup.setBackground(HOVER_BACKGROUND);
        popup.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel itemList = new JPanel();
        itemList.setBorder(BorderFactory.createEmptyBorder(ICON_SIZE + 6, 0, 0, 0));
        itemList.setLayout(new BoxLayout(itemList, BoxLayout.Y_AXIS));
        itemList.add(Box.createVerticalGlue());
        Utils.addChild(popup, itemList);

        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
        infoContainer.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        Utils.addChild(popup, infoContainer);

        JPanel combinedItems = new JPanel();
        Utils.addChild(infoContainer, combinedItems);
        infoContainer.add(Box.createVerticalGlue());

        Map<TftItem, Integer> counts = new HashMap<>();
        Map<TftItem, JSpinner> spinnerMap = new HashMap<>();
        Multimap<TftItem, JLabel> combinationIcons = MultimapBuilder.hashKeys().arrayListValues().build();

        for (TftItem item : main.getProvider().getItems().values()) {
            if (item.getIngredients().isEmpty() && !item.getIngredient().isEmpty()) {
                JPanel line = new JPanel();
                line.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
                JLabel itemIcon = new JLabel(new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)));
                itemIcon.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16), COLORED_BORDER));
                itemIcon.setToolTipText(main.getLang("item-hover", getReplacements(item)));
                line.add(itemIcon);

                JLabel itemHeaderIcon = new JLabel(new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)));
                itemHeaderIcon.setBorder(COLORED_BORDER);
                itemHeaderIcon.setToolTipText(main.getLang("item-hover", getReplacements(item)));
                combinedItems.add(itemHeaderIcon);

                JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9001, 1));
                spinnerMap.put(item, spinner);
                spinner.addChangeListener(e -> {
                    if ((Integer) spinner.getValue() == 0) {
                        counts.remove(item);
                    } else {
                        counts.put(item, (Integer) spinner.getValue());
                    }

                    List<TftItem> combineableItems = new ArrayList<>();
                    for (TftItem combinedItem : main.getProvider().getItems().values()) {
                        if (combinedItem.getIngredients().size() < 2) {
                            continue;
                        }
                        if (combinedItem.getIngredients().get(0) == combinedItem.getIngredients().get(1)) {
                            if (counts.getOrDefault(combinedItem.getIngredients().get(0), 0) > 1) {
                                combineableItems.add(combinedItem);
                            }
                        } else if (counts.containsKey(combinedItem.getIngredients().get(0))
                                && counts.containsKey(combinedItem.getIngredients().get(1))) {
                            combineableItems.add(combinedItem);
                        }
                    }
                    for (Map.Entry<TftItem, JLabel> entry : combinationIcons.entries()) {
                        entry.getValue().setBorder(BorderFactory.createLineBorder(entry.getKey().getIngredients().get(0) == entry.getKey().getIngredients().get(1) ? new Color(100, 100, 100) : GRID_COLOR));
                        entry.getValue().setEnabled(false);
                    }
                    if (!combineableItems.isEmpty()) {
                        for (TftItem combinedItem : combineableItems) {
                            JLabel combinedIcon = new JLabel(new ImageIcon(main.getImage(combinedItem.getIconUrl(), ICON_SIZE, ICON_SIZE)));
                            combinedIcon.setToolTipText(main.getLang("item-hover", getReplacements(combinedItem)));

                            if (combinationIcons.containsKey(combinedItem)) {
                                for (JLabel label : combinationIcons.get(combinedItem)) {
                                    label.setBorder(BorderFactory.createLineBorder(TEXT_COLOR));
                                    label.setEnabled(true);
                                }
                            }
                        }
                    }

                    pack();
                    repaint();
                });
                spinner.setFont(HUGE_FONT);
                spinner.getEditor().getComponent(0).setForeground(TEXT_COLOR);
                spinner.getEditor().getComponent(0).setBackground(BACKGROUND);
                for (int i = 0; i < spinner.getComponentCount(); i++) {
                    Component c = spinner.getComponent(i);
                    c.setBackground(BACKGROUND);
                    if (c instanceof JButton) {
                        ((JButton) c).setBorder(COLORED_BORDER);
                        c.setCursor(TftOverlay.CURSOR_CLICK);
                        c.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                c.setBackground(Overlay.GRID_COLOR);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                c.setBackground(Overlay.BACKGROUND);
                            }
                        });
                    }
                }
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(2);
                spinner.setBorder(COLORED_BORDER);
                spinner.addMouseWheelListener(e -> {
                    int target = (Integer) spinner.getValue() - (int) e.getPreciseWheelRotation();
                    if (target > 0) {
                        spinner.setValue(target);
                    } else {
                        spinner.setValue(0);
                    }
                });

                Utils.addChild(line, spinner);
                Utils.addChild(itemList, line);
            }
        }

        combinedItems.setLayout(new GridLayout(0, spinnerMap.size()));

        for (TftItem item : main.getProvider().getItems().values()) {
            if (item.getIngredients().isEmpty() && !item.getIngredient().isEmpty()) {
                for (TftItem otherItem : main.getProvider().getItems().values()) {
                    if (otherItem.getIngredients().isEmpty()) {
                        TftItem combinedItem = main.getProvider().getCombination(item, otherItem);
                        if (combinedItem == null) {
                            continue;
                        }

                        BufferedImage image = main.getImage(combinedItem.getIconUrl(), ICON_SIZE, ICON_SIZE);
                        JLabel combinedItemIcon = new JLabel(new ImageIcon(image));
                        combinedItemIcon.setBorder(BorderFactory.createLineBorder(item == otherItem ? new Color(100, 100, 100) : GRID_COLOR));
                        combinedItemIcon.setToolTipText(main.getLang("item-hover", getReplacements(combinedItem)));
                        combinedItemIcon.setEnabled(false);

                        BufferedImage disabledImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = disabledImage.createGraphics();
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER , 0.5f));
                        g2d.drawImage(image, 0, 0, null);
                        g2d.dispose();
                        combinedItemIcon.setDisabledIcon(new ImageIcon(disabledImage));

                        combinedItems.add(combinedItemIcon);
                        combinationIcons.put(combinedItem, combinedItemIcon);
                    }
                }
            }
        }

        Utils.addChild(itemList, new LabelButton(main.getLang("reset"), e -> spinnerMap.values().forEach(s -> s.setValue(0))));

        parent.addEntry(main.getLang("item-builder"), getTextButton(main.getLang("item-builder"), FONT), popup, true);
    }

    private void addItemsPopups(TabbedPanel mainPanel) {
        for (TftItem item : main.getProvider().getItems().values()) {
            if (item.getIngredients().isEmpty() && !item.getIngredient().isEmpty()) {

                JPanel combinations = new JPanel();
                combinations.setLayout(new BoxLayout(combinations, BoxLayout.Y_AXIS));
                combinations.setBackground(HOVER_BACKGROUND);

                JPanel line = new JPanel();
                line.setLayout(new FlowLayout(FlowLayout.LEADING));
                line.add(new JLabel(main.getLang("item-info", getReplacements(item)), new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)), JLabel.LEADING));
                Utils.addChild(combinations, line);

                item.getIngredient().stream().sorted(Comparator.comparing(o -> o.getOtherIngredient(item).getId())).forEachOrdered(combinedItem -> {
                    JPanel combLine = new JPanel();
                    combLine.setLayout(new FlowLayout(FlowLayout.LEADING));
                    combLine.setLayout(new BoxLayout(combLine, BoxLayout.X_AXIS));
                    combLine.setBorder(BorderFactory.createEmptyBorder(2, 5, 5, 2));

                    JLabel plus = new JLabel(" + ");
                    plus.setForeground(TEXT_COLOR);
                    plus.setFont(HUGE_FONT);
                    combLine.add(plus);

                    TftItem ingredient = combinedItem.getOtherIngredient(item);
                    JLabel itemIcon = new JLabel(new ImageIcon(main.getImage(ingredient.getIconUrl(), ICON_SIZE, ICON_SIZE)));
                    itemIcon.setToolTipText(main.getLang("item-hover", getReplacements(ingredient)));
                    itemIcon.setBorder(COLORED_BORDER);
                    combLine.add(itemIcon);

                    JLabel arrow = new JLabel(" > ");
                    arrow.setForeground(TEXT_COLOR);
                    arrow.setFont(HUGE_FONT);
                    combLine.add(arrow);

                    JLabel combinedLabel = new JLabel(main.getLang("item-info-combined", getReplacements(combinedItem)), new ImageIcon(main.getImage(combinedItem.getIconUrl(), ICON_SIZE, ICON_SIZE)), JLabel.LEADING);
                    combinedLabel.setToolTipText(main.getLang("item-hover", getReplacements(combinedItem)));
                    combinedLabel.setFont(FONT);
                    combLine.add(combinedLabel);
                    Utils.addChild(combinations, combLine);
                });

                mainPanel.addEntry(item.getName(), new JLabel("", new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)), JLabel.CENTER), combinations);
            }
        }
    }

    private JLabel getCharButton(String c) {
        JLabel button = getTextButton(c, HUGE_FONT);
        button.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        return button;
    }

    private JLabel getTextButton(String c, Font font) {
        JLabel button = new JLabel(c);
        button.setForeground(TEXT_COLOR);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFont(font.deriveFont(Font.BOLD));
        button.setBorder(BORDER);
        button.setOpaque(true);
        button.setBackground(BACKGROUND);
        return button;
    }

    private String[] getReplacements(TftItem item) {
        return new String[]{
                "name", item.getName(),
                "iconUrl", item.getIconUrl().toExternalForm(),
                "iconPath", "file:/" + main.getCachedImageFile(item.getIconUrl(), 36, 36).getAbsolutePath(),
                "champions", item.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", ")),
                "champions-with-icons", item.getChampions().stream()
                        .map(c -> "<br><img src=\"file:/" + main.getCachedImageFile(c.getIconUrl(), 24, 24).getAbsolutePath() + "\">"
                                + "&nbsp;" + c.getName()).collect(Collectors.joining("")),
                "desc", Utils.addHilights(item.getDescription()),
                "ingredient", item.getIngredient().stream().map(TftItem::getName).collect(Collectors.joining(", ")),
                "ingredient-with-icons", item.getIngredient().stream()
                        .map(i -> "<img src=\"file:/" + main.getCachedImageFile(i.getIconUrl(), 24, 24).getAbsolutePath() + "\">"
                                + "&nbsp;" + i.getName()).collect(Collectors.joining("<br>")),
                "ingredients", item.getIngredients().stream().map(TftItem::getName).collect(Collectors.joining(", ")),
                "ingredients-with-icons", item.getIngredients().stream()
                        .map(i -> "<img src=\"file:/" + main.getCachedImageFile(i.getIconUrl(), 24, 24).getAbsolutePath() + "\">"
                                + "&nbsp;" + i.getName()).collect(Collectors.joining("<br>")),
        };
    }

    private String[] getReplacements(TftChampion champion) {
        return new String[]{"name", champion.getName(),
                "synergies", champion.getSynergies().stream()
                        .map(TftSynergy::getName).collect(Collectors.joining(", ")),
                "synergy-icons", champion.getSynergies().stream()
                        .map(s -> "<img src=\"file:/" + main.getCachedImageFile(s.getIconUrl(), 16, 16).getAbsolutePath() + "\">").collect(Collectors.joining()),
                "synergies-with-icons", champion.getSynergies().stream()
                        .map(s -> "<img src=\"file:/" + main.getCachedImageFile(s.getIconUrl(), 16, 16).getAbsolutePath() + "\">"
                                + "&nbsp;" + s.getName()).collect(Collectors.joining("<br>")),
                "iconUrl", champion.getIconUrl().toExternalForm(),
                "iconPath", "file:/" + main.getCachedImageFile(champion.getIconUrl(), 36, 36).getAbsolutePath(),
                "cost", String.valueOf(champion.getCost()),
                "damage", champion.getDamage(),
                "dps", champion.getDps(),
                "health", champion.getHealth(),
                "armor", String.valueOf(champion.getArmor()),
                "magicresistance", String.valueOf(champion.getMagicResistance()),
                "range", String.valueOf(champion.getRange()),
                "speed", String.valueOf(champion.getSpeed()),
                "items", champion.getRecommendedItems().stream()
                .map(TftItem::getName).collect(Collectors.joining(", ")),
                "items-with-icons", champion.getRecommendedItems().stream()
                        .map(i -> "<br><img src=\"file:/" + main.getCachedImageFile(i.getIconUrl(), 24, 24).getAbsolutePath() + "\">"
                                + "&nbsp;" + i.getName()).collect(Collectors.joining("")),
                "spell-name", champion.getSpell().getName(),
                "spell-iconUrl", champion.getSpell().getIconUrl().toExternalForm(),
                "spell-iconPath", "file:/" + main.getCachedImageFile(champion.getSpell().getIconUrl(), 16, 16).getAbsolutePath(),
                "spell-desc", Utils.addHilights(champion.getSpell().getDescription()),
                "spell-effect", Utils.addHilights(champion.getSpell().getEffect()),
                "spell-mana", champion.getSpell().getMana(),
                "spell-type", champion.getSpell().getType()
        };
    }

    private JLabel getChampionIcon(TftChampion champion, int size) {
        JLabel championLabel = new JLabel("", new ImageIcon(main.getImage(champion.getIconUrl(), size, size)), JLabel.CENTER);
        Utils.addTooltip(championLabel, main.getLang("champion-hover", getReplacements(champion)));
        championLabel.setBorder(BorderFactory.createLineBorder(champion.getColor()));
        return championLabel;
    }

}


