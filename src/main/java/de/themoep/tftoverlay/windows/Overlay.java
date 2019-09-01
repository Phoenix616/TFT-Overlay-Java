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

import de.themoep.tftoverlay.TftOverlay;
import de.themoep.tftoverlay.data.TftChampion;
import de.themoep.tftoverlay.data.TftClass;
import de.themoep.tftoverlay.data.TftItem;
import de.themoep.tftoverlay.data.TftOrigin;
import de.themoep.tftoverlay.data.TftSynergy;
import de.themoep.tftoverlay.elements.LabelButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Overlay extends JFrame {

    public static final boolean PBE = false;

    public static final Font FONT = new Font("Dialog", Font.PLAIN, 12);
    public static final Font HUGE_FONT = FONT.deriveFont(Font.BOLD, 28);
    public static final Color BACKGROUND = new Color(10, 10, 10);
    public static final Color HOVER_BACKGROUND = new Color(10, 10, 10, 220);
    public static final Color TEXT_COLOR = new Color(230, 198, 123);
    public static final Color SECONDARY_TEXT_COLOR = new Color(230, 216, 183);
    public static final Border COLORED_BORDER = BorderFactory.createLineBorder(new Color(117, 87, 41));
    public static final Border BORDER = BorderFactory.createCompoundBorder(COLORED_BORDER, BorderFactory.createEmptyBorder(5, 5, 5, 5));
    public static final Border BUTTON_BORDER = BorderFactory.createCompoundBorder(BORDER, new EmptyBorder(0, 5, 0, 5));

    public static final int ICON_SIZE = 42;
    private final TftOverlay main;

    private final JPanel mainPopupContainer;
    private final JPanel mainPanelHeader;
    private List<JPanel> headerEntries = new ArrayList<>();

    public Overlay(TftOverlay main) {
        super(main.getName() + " v" + main.getVersion());
        this.main = main;
        setIconImage(main.getIcon());

        JPanel content = new JPanel();
        setContentPane(content);
        content.setFont(FONT);
        content.setOpaque(false);

        setAlwaysOnTop(true);
        setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        UIManager.put("TabbedPane.font", FONT);
        UIManager.put("ToolTip.background", new ColorUIResource(BACKGROUND));
        UIManager.put("ToolTip.border", BORDER);
        UIManager.put("ToolTip.foreground", TEXT_COLOR);
        UIManager.put("ToolTip.width", 100);
        ToolTipManager.sharedInstance().setInitialDelay(0);

        // --- Main Panel setup ---

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPopupContainer = new JPanel();
        mainPanelHeader = new JPanel();
        mainPanelHeader.setLayout(new FlowLayout(FlowLayout.LEADING));
        mainPanelHeader.setBackground(new Color(0, 0, 0, 0));

        addChild(mainPanel, mainPanelHeader);
        addChild(mainPanel, mainPopupContainer);

        // --- Menu ---

        WindowMover menuElement = new WindowMover(this);
        menuElement.setPreferredSize(new Dimension(10, ICON_SIZE - 2));
        menuElement.setBackground(new Color(158, 108, 54));
        menuElement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu menu = new JPopupMenu("menu");
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

                    menu.add(new LabelButton(main.getLang("close"), e1 -> System.exit(1)));
                    menu.pack();
                    menu.show(menuElement, 10, 0);
                }
            }
        });
        mainPanelHeader.add(menuElement);

        // -- Champions panel --
        addChampionsGridPopup();

        // -- Synergies --

        addSynergyPopup(main.getProvider().getClasses().values(), "C", "class");
        addSynergyPopup(main.getProvider().getOrigins().values(), "O", "origin");

        // --- Items ---

        addItemBuilderPopup();
        addItemsPopups();

        addChild(content, mainPanel);

        pack();
    }

    private void addChampionsGridPopup() {
        JPanel popup = new JPanel();
        popup.setLayout(new GridLayout(main.getProvider().getOrigins().size() + 1, main.getProvider().getClasses().size() + 1));
        popup.setForeground(TEXT_COLOR);
        popup.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        popup.setBackground(new Color(0, 0, 0, 200));
        JPanel cornerCell = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(36, 36, 36));
                g.drawLine(0, 0, super.getWidth(), super.getHeight());
                super.paintComponent(g);
            }
        };
        addChild(popup, cornerCell);
        cornerCell.setForeground(SECONDARY_TEXT_COLOR);
        cornerCell.setLayout(new BoxLayout(cornerCell, BoxLayout.Y_AXIS));

        JPanel classesContainer = new JPanel();
        classesContainer.setLayout(new BoxLayout(classesContainer, BoxLayout.X_AXIS));
        addChild(cornerCell, classesContainer);
        classesContainer.add(Box.createHorizontalGlue());
        addChild(classesContainer, new JLabel("Classes"));

        JPanel originsContainer = new JPanel();
        originsContainer.setLayout(new BoxLayout(originsContainer, BoxLayout.X_AXIS));
        addChild(cornerCell, originsContainer);
        addChild(originsContainer, new JLabel("Origins"));
        originsContainer.add(Box.createHorizontalGlue());

        for (TftClass tftClass : main.getProvider().getClasses().values()) {
            JLabel classLabel = new JLabel(tftClass.getName(), new ImageIcon(main.getImage(tftClass.getIconUrl(), 16, 16)), JLabel.CENTER);
            classLabel.setHorizontalTextPosition(JLabel.CENTER);
            classLabel.setVerticalTextPosition(JLabel.TOP);
            classLabel.setToolTipText(main.getLang("class-hover",
                    "name", tftClass.getName(),
                    "desc", breakLine(tftClass.getDescription()),
                    "effects", breakLine(tftClass.getEffects()),
                    "champions", breakLine(tftClass.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                    "champion-count", String.valueOf(tftClass.getChampions().size())
            ));
            classLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(27, 27, 27)));
            addChild(popup, classLabel);
        }

        for (TftOrigin origin : main.getProvider().getOrigins().values()) {
            JLabel originLabel = new JLabel(origin.getName(), new ImageIcon(main.getImage(origin.getIconUrl(), 16, 16)), JLabel.TRAILING);
            originLabel.setHorizontalTextPosition(JLabel.LEADING);
            originLabel.setToolTipText(main.getLang("origin-hover",
                    "name", origin.getName(),
                    "desc", breakLine(origin.getDescription()),
                    "effects", breakLine(origin.getEffects()),
                    "champions", breakLine(origin.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                    "champion-count", String.valueOf(origin.getChampions().size())
            ));
            originLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(27, 27, 27)));
            addChild(popup, originLabel);

            for (TftClass tftClass : main.getProvider().getClasses().values()) {
                JPanel champCell = new JPanel();
                champCell.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, new Color(36, 36, 36)));

                List<TftChampion> championList = tftClass.getChampions().stream()
                        .filter(c -> c.getSynergies().contains(origin))
                        .filter(c -> !c.isPbe() || PBE)
                        .sorted(Comparator.comparingInt(TftChampion::getCost))
                        .collect(Collectors.toList());

                int size = championList.size() > 0 ? 48 / championList.size() : 0;
                if (size > 24) {
                    size = 24;
                }

                for (TftChampion champion : championList) {
                    champCell.add(getChampionIcon(champion));
                }
                addChild(popup, champCell);
            }
        }

        addHeaderEntry(getCharButton("┼"), popup);
    }

    private void addSynergyPopup(Collection<? extends TftSynergy> synergies, String iconChar, String key) {
        JPanel popup = new JPanel();
        popup.setLayout(new BoxLayout(popup, BoxLayout.Y_AXIS));
        popup.setBackground(HOVER_BACKGROUND);
        popup.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));

        for (TftSynergy synergy : synergies) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(36, 36, 36)));

            List<TftChampion> championList = synergy.getChampions().stream()
                    .filter(c -> !c.isPbe() || PBE)
                    .sorted(Comparator.comparingInt(TftChampion::getCost))
                    .collect(Collectors.toList());

            JLabel infoLabel = new JLabel(breakLine(main.getLang(key + "-info",
                    "name", synergy.getName(),
                    "desc", synergy.getDescription() + (!synergy.getDescription().isEmpty() && !synergy.getEffects().isEmpty() ? "<br>" : ""),
                    "effects", synergy.getEffects(),
                    "champions", synergy.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", ")),
                    "champion-count", String.valueOf(championList.size())
            )), new ImageIcon(main.getImage(synergy.getIconUrl(), 24, 24)), JLabel.LEADING);
            infoLabel.setHorizontalAlignment(SwingConstants.LEADING);
            panel.add(infoLabel);
            infoLabel.setFont(FONT);

            JPanel champPanelContainer = new JPanel();
            JPanel champPanel = new JPanel();
            champPanel.setLayout(new GridLayout(championList.size() > 3 ? 2 : 1, 0));
            for (int i = 0; i < championList.size(); i++) {
                champPanel.add(getChampionIcon(championList.get(i)));
                if (championList.size() > 3 && championList.size() % 2 > 0 && (championList.size() / 2 == i)) {
                    champPanel.add(new JLabel());
                }
            }
            addChild(champPanelContainer, champPanel);
            addChild(panel, champPanelContainer);

            addChild(popup, panel);
        }

        addHeaderEntry(getCharButton(iconChar), popup);
    }

    private void addItemBuilderPopup() {
        JPanel popup = new JPanel();
        popup.setLayout(new BoxLayout(popup, BoxLayout.X_AXIS));
        popup.setForeground(TEXT_COLOR);
        popup.setBackground(HOVER_BACKGROUND);
        popup.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel itemList = new JPanel();
        itemList.setLayout(new BoxLayout(itemList, BoxLayout.Y_AXIS));
        itemList.add(Box.createVerticalGlue());
        addChild(popup, itemList);

        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
        infoContainer.setPreferredSize(new Dimension(300, 0));
        addChild(popup, infoContainer);

        //JLabel itemTitle = new JLabel(main.getLang("buildable-items-title"));
        //itemTitle.setLayout(new BoxLayout(itemTitle, BoxLayout.X_AXIS));
        //itemTitle.setVisible(false);
        //infoContainer.add(itemTitle);

        JPanel buildableItemsList = new JPanel();
        buildableItemsList.setLayout(new FlowLayout(FlowLayout.LEFT));
        addChild(infoContainer, buildableItemsList);

        //JLabel champsTitle = new JLabel(main.getLang("recommended-champs-title"));
        //champsTitle.setLayout(new BoxLayout(champsTitle, BoxLayout.X_AXIS));
        //champsTitle.setVisible(false);
        //infoContainer.add(champsTitle);

        //JPanel recommendedChampionsList = new JPanel();
        //recommendedChampionsList.setLayout(new FlowLayout(FlowLayout.LEFT));
        //addChild(infoContainer, recommendedChampionsList);

        Map<TftItem, Integer> counts = new HashMap<>();

        for (TftItem item : main.getProvider().getItems().values()) {
            if (item.getIngredients().isEmpty()) {
                JPanel line = new JPanel();
                line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
                JLabel itemIcon = new JLabel(new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)));
                itemIcon.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16), COLORED_BORDER));
                itemIcon.setToolTipText(main.getLang("item-hover",
                        "name", item.getName(),
                        "champions", breakLine(item.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                        "desc", breakLine(item.getDescription()),
                        "ingredient", breakLine(item.getIngredient().stream().map(TftItem::getName).collect(Collectors.joining(", "))),
                        "ingredients", item.getIngredients().stream().map(TftItem::getName).collect(Collectors.joining(", "))
                ));
                line.add(itemIcon);

                JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9001, 1));
                spinner.addChangeListener(e -> {
                    if ((Integer) spinner.getValue() == 0) {
                        counts.remove(item);
                    } else {
                        counts.put(item, (Integer) spinner.getValue());
                    }
                    buildableItemsList.removeAll();
                    //recommendedChampionsList.removeAll();

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
                    if (combineableItems.isEmpty()) {
                        //itemTitle.setVisible(false);
                    } else {
                        //itemTitle.setVisible(true);
                        Map<TftChampion, Integer> champCounts = new HashMap<>();
                        for (TftItem combinedItem : combineableItems) {
                            JLabel combinedIcon = new JLabel(new ImageIcon(main.getImage(combinedItem.getIconUrl(), ICON_SIZE, ICON_SIZE)));
                            combinedIcon.setToolTipText(main.getLang("item-hover",
                                    "name", combinedItem.getName(),
                                    "champions", breakLine(combinedItem.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                                    "desc", breakLine(combinedItem.getDescription()),
                                    "ingredient", breakLine(combinedItem.getIngredient().stream().map(TftItem::getName).collect(Collectors.joining(", "))),
                                    "ingredients", combinedItem.getIngredients().stream().map(TftItem::getName).collect(Collectors.joining(", "))
                            ));
                            buildableItemsList.add(combinedIcon);
                            for (TftChampion champion : combinedItem.getChampions()) {
                                champCounts.put(champion, champCounts.getOrDefault(champion, 0) + 1);
                            }
                        }

                        /* TODO: Proper team suggestions
                        List<TftChampion> champList = new ArrayList<>();

                        for (Map.Entry<TftChampion, Integer> entry : champCounts.entrySet()) {
                            if (entry.getValue() > 1 + Math.round(combineableItems.size() / 10.0)) {
                                champList.add(entry.getKey());
                            }
                        }

                        if (champList.isEmpty()) {
                            champsTitle.setVisible(false);
                        } else {
                            champsTitle.setVisible(true);
                            champList.sort(Comparator.comparingInt(TftChampion::getCost));
                            for (TftChampion champion : champList) {
                                recommendedChampionsList.add(getChampionIcon(champion));
                            }
                        }
                        */
                    }

                    pack();
                    repaint();
                });
                spinner.setFont(HUGE_FONT);
                spinner.getEditor().getComponent(0).setForeground(TEXT_COLOR);
                spinner.getEditor().getComponent(0).setBackground(new Color(0, 0, 0, 0));
                for (int i = 0; i < spinner.getComponentCount(); i++) {
                    Component c = spinner.getComponent(i);
                    c.setBackground(BACKGROUND);
                    if (c instanceof JButton) {
                        ((JButton) c).setBorder(COLORED_BORDER);
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

                addChild(line, spinner);
                addChild(itemList, line);
            }
        }

        addHeaderEntry(getCharButton("I"), popup);
    }

    private void addItemsPopups() {
        for (TftItem item : main.getProvider().getItems().values()) {
            if (item.getIngredients().isEmpty()) {

                JPanel combinations = new JPanel();
                combinations.setLayout(new BoxLayout(combinations, BoxLayout.Y_AXIS));
                combinations.setBackground(HOVER_BACKGROUND);
                combinations.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

                JPanel line = new JPanel();
                line.setLayout(new FlowLayout(FlowLayout.LEADING));
                line.add(new JLabel(main.getLang("item-info",
                        "name", item.getName(),
                        "desc", breakLine(item.getDescription()),
                        "ingredient", breakLine(item.getIngredient().stream().map(TftItem::getName).collect(Collectors.joining(", ")))
                ), new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)), JLabel.LEADING));
                addChild(combinations, line);

                item.getIngredient().stream().sorted(Comparator.comparing(o -> o.getOtherIngredient(item).getId())).forEachOrdered(combinedItem -> {
                    JPanel combLine = new JPanel();
                    combLine.setLayout(new FlowLayout(FlowLayout.LEADING));
                    combLine.setLayout(new BoxLayout(combLine, BoxLayout.X_AXIS));

                    JLabel plus = new JLabel(" + ");
                    plus.setForeground(TEXT_COLOR);
                    plus.setFont(HUGE_FONT);
                    combLine.add(plus);

                    TftItem ingredient = combinedItem.getOtherIngredient(item);
                    JLabel itemIcon = new JLabel(new ImageIcon(main.getImage(ingredient.getIconUrl(), ICON_SIZE, ICON_SIZE)));
                    itemIcon.setToolTipText(main.getLang("item-hover",
                            "name", ingredient.getName(),
                            "champions", breakLine(ingredient.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                            "desc", breakLine(ingredient.getDescription()),
                            "ingredient", breakLine(ingredient.getIngredient().stream().map(TftItem::getName).collect(Collectors.joining(", "))),
                            "ingredients", combinedItem.getIngredients().stream().map(TftItem::getName).collect(Collectors.joining(", "))
                    ));
                    itemIcon.setBorder(COLORED_BORDER);
                    combLine.add(itemIcon);

                    JLabel arrow = new JLabel(" > ");
                    arrow.setForeground(TEXT_COLOR);
                    arrow.setFont(HUGE_FONT);
                    combLine.add(arrow);

                    JLabel combinedLabel = new JLabel(main.getLang("item-info-combined",
                            "name", combinedItem.getName(),
                            "champions", breakLine(combinedItem.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                            "desc", breakLine(combinedItem.getDescription()),
                            "ingredients", item.getIngredients().stream().map(TftItem::getName).collect(Collectors.joining(", "))
                    ), new ImageIcon(main.getImage(combinedItem.getIconUrl(), ICON_SIZE, ICON_SIZE)), JLabel.LEADING);
                    combinedLabel.setToolTipText(main.getLang("item-hover",
                            "name", combinedItem.getName(),
                            "champions", breakLine(combinedItem.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                            "desc", breakLine(combinedItem.getDescription()),
                            "ingredient", breakLine(combinedItem.getIngredient().stream().map(TftItem::getName).collect(Collectors.joining(", "))),
                            "ingredients", combinedItem.getIngredients().stream().map(TftItem::getName).collect(Collectors.joining(", "))
                    ));
                    combinedLabel.setFont(FONT);
                    combLine.add(combinedLabel);
                    addChild(combinations, combLine);
                });

                addHeaderEntry(new JLabel("", new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)), JLabel.CENTER), combinations);
            }
        }
    }

    private JLabel getCharButton(String c) {
        JLabel button = new JLabel(c);
        button.setForeground(TEXT_COLOR);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        button.setFont(HUGE_FONT);
        button.setBorder(BORDER);
        button.setOpaque(true);
        button.setBackground(BACKGROUND);
        return button;
    }

    private JLabel getChampionIcon(TftChampion champion) {
        JLabel championLabel = new JLabel("", new ImageIcon(main.getImage(champion.getIconUrl(), 24, 24)), JLabel.CENTER);
        championLabel.setToolTipText(main.getLang("champion-hover",
                "name", champion.getName(),
                "synergies", breakLine(champion.getSynergies().stream()
                        .map(TftSynergy::getName).collect(Collectors.joining(", "))),
                "iconUrl", champion.getIconUrl().toExternalForm(),
                "cost", String.valueOf(champion.getCost()),
                "damage", champion.getDamage(),
                "dps", champion.getDps(),
                "health", champion.getHealth(),
                "armor", String.valueOf(champion.getArmor()),
                "magicresistance", String.valueOf(champion.getMagicResistance()),
                "range", String.valueOf(champion.getRange()),
                "speed", String.valueOf(champion.getSpeed()),
                "items", breakLine(champion.getRecommendedItems().stream()
                        .map(TftItem::getName).collect(Collectors.joining(", "))),
                "spell-name", champion.getSpell().getName(),
                "spell-desc", breakLine(champion.getSpell().getDescription()),
                "spell-effect", breakLine(champion.getSpell().getEffect()),
                "spell-mana", champion.getSpell().getMana(),
                "spell-type", champion.getSpell().getType()
        ));
        championLabel.setBorder(BorderFactory.createLineBorder(champion.getColor()));
        return championLabel;
    }

    public void addHeaderEntry(JLabel icon, JPanel container) {
        addHeaderEntry(icon, container, true);
    }

    public void addHeaderEntry(JLabel icon, JPanel popup, boolean alignToButton) {
        icon.setBorder(COLORED_BORDER);

        JPanel container = new JPanel();
        container.setVisible(false);
        container.add(popup);
        addChild(mainPopupContainer, container);

        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                for (JPanel entry : headerEntries) {
                    entry.setVisible(false);
                }
                container.setVisible(true);
                if (alignToButton) {
                    container.setBorder(BorderFactory.createEmptyBorder(0, icon.getX() - 10, 0, 0));
                }
                pack();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (icon.getHeight() + 1 > e.getY()) {
                    container.setVisible(false);
                    pack();
                }
            }
        });
        container.getComponent(0).addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (e.getX() > 0 && e.getX() < popup.getWidth()
                        && e.getY() > 0 && e.getY() < popup.getHeight()) {
                    return;
                }
                container.setVisible(false);
                pack();
            }
        });
        mainPanelHeader.add(icon);

        headerEntries.add(container);
    }

    private String breakLine(String string) {
        return breakLine(string, 640);
    }

    private String breakLine(String string, int width) {
        FontMetrics fontMetrics = getFontMetrics(FONT);
        String[] parts = string.split(" ");
        StringBuilder sb = new StringBuilder(parts[0]);
        int lineWidth = fontMetrics.stringWidth(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].endsWith("<br>") || parts[i].endsWith("<p") || parts[i].endsWith("</p>")) {
                sb.append(" ");
                lineWidth = 0;
            } else if (parts[i].startsWith("<br>") || parts[i].startsWith("<p") || parts[i].startsWith("</p>")) {
                sb.append(" ");
                lineWidth = fontMetrics.stringWidth(parts[i]) - 4;
            } else if (parts[i].contains("<br>") || parts[i].contains("<p") || parts[i].contains("</p>")) {
                sb.append(" ");
                int index = parts[i].lastIndexOf("<br>");
                if (index < 0) {
                    index = parts[i].lastIndexOf("<p");
                }
                if (index < 0) {
                    index = parts[i].lastIndexOf("</p>");
                }
                if (index >= 0) {
                    lineWidth = fontMetrics.stringWidth(parts[i].substring(index + 4));
                }
            } else if (lineWidth + fontMetrics.stringWidth(parts[i]) < width) {
                sb.append(" ");
                lineWidth += fontMetrics.stringWidth(parts[i]) + fontMetrics.stringWidth(" ");
            } else {
                sb.append("<br>");
                lineWidth = 0;
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    private void addChild(JComponent parent, JComponent child) {
        parent.add(child);
        child.setForeground(parent.getForeground());
        child.setBackground(new Color(0, 0, 0, 0));
    }

    private class WindowMover extends JPanel {
        private Point startClick;

        public WindowMover(JFrame parent) {
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    getComponentAt(startClick = e.getPoint());
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    parent.setLocation(
                            parent.getLocation().x + e.getX() - startClick.x,
                            parent.getLocation().y + e.getY() - startClick.y
                    );
                }
            });
        }
    }
}


