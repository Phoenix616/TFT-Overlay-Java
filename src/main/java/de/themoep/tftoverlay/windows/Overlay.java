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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final JPanel mainPopupContainer;
    private final JPanel mainPanelHeader;
    private List<JPanel> headerEntries = new ArrayList<>();

    public Overlay(TftOverlay main) {
        super(main.getName() + " v" + main.getVersion());
        this.main = main;
        setIconImage(main.getIcon());

        setCursor(main.getCursor());

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
        UIManager.put("ToolTip.backgroundInactive", new ColorUIResource(BACKGROUND));
        UIManager.put("ToolTip.border", BORDER);
        UIManager.put("ToolTip.foreground", TEXT_COLOR);
        UIManager.put("ToolTip.foregroundInactive", new ColorUIResource(BACKGROUND));
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
        menuElement.setBackground(TEXT_COLOR);
        menuElement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu menu = new JPopupMenu("menu");
                    menu.setCursor(main.getCursor());
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

                    menu.add(new LabelButton(main, main.getLang("update-data"), c -> main.start(true)));
                    menu.add(new LabelButton(main, main.getLang("close"), c -> System.exit(1)));
                    menu.pack();
                    menu.show(menuElement, 10, 0);
                }
            }
        });
        menuElement.setToolTipText(main.getLang("menu"));
        mainPanelHeader.add(menuElement);

        // -- Champions panel --
        addChampionsGridPopup();

        // -- Synergies --

        addSynergyPopup(main.getProvider().getClasses().values(), "C", "classes");
        addSynergyPopup(main.getProvider().getOrigins().values(), "O", "origins");

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
                g.setColor(GRID_COLOR);
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
        addChild(classesContainer, new JLabel(main.getLang("classes")));

        JPanel originsContainer = new JPanel();
        originsContainer.setLayout(new BoxLayout(originsContainer, BoxLayout.X_AXIS));
        addChild(cornerCell, originsContainer);
        addChild(originsContainer, new JLabel(main.getLang("origins")));
        originsContainer.add(Box.createHorizontalGlue());

        for (TftClass tftClass : main.getProvider().getClasses().values()) {
            JLabel classLabel = new JLabel(tftClass.getName(), new ImageIcon(main.getImage(tftClass.getIconUrl(), 16, 16)), JLabel.CENTER);
            classLabel.setHorizontalTextPosition(JLabel.CENTER);
            classLabel.setVerticalTextPosition(JLabel.TOP);
            classLabel.setToolTipText(main.getLang("class-hover",
                    "name", tftClass.getName(),
                    "desc", tftClass.getDescription() + (!tftClass.getDescription().isEmpty() && !tftClass.getEffects().isEmpty() ? "<br>" : ""),
                    "effects", tftClass.getEffects(),
                    "champions", tftClass.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", ")),
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
                    "desc", origin.getDescription() + (!origin.getDescription().isEmpty() && !origin.getEffects().isEmpty() ? "<br>" : ""),
                    "effects", origin.getEffects(),
                    "champions", origin.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", ")),
                    "champion-count", String.valueOf(origin.getChampions().size())
            ));
            originLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(27, 27, 27)));
            addChild(popup, originLabel);

            for (TftClass tftClass : main.getProvider().getClasses().values()) {
                JPanel champCell = new JPanel();
                champCell.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, GRID_COLOR));

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

        addHeaderEntry(main.getLang("champions"), getCharButton("┼"), popup);
    }

    private void addSynergyPopup(Collection<? extends TftSynergy> synergies, String iconChar, String key) {
        JPanel popup = new JPanel();
        popup.setLayout(new BoxLayout(popup, BoxLayout.Y_AXIS));
        popup.setBackground(HOVER_BACKGROUND);
        popup.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        for (TftSynergy synergy : synergies) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 50)), BorderFactory.createEmptyBorder(3, 0, 3, 0)));

            List<TftChampion> championList = synergy.getChampions().stream()
                    .filter(c -> !c.isPbe() || PBE)
                    .sorted(Comparator.comparingInt(TftChampion::getCost))
                    .collect(Collectors.toList());

            JLabel infoLabel = new JLabel(main.getLang(key + "-info",
                    "name", synergy.getName(),
                    "desc", addHilights(synergy.getDescription()) + (!synergy.getDescription().isEmpty() && !synergy.getEffects().isEmpty() ? "<br>" : ""),
                    "effects", addHilights(synergy.getEffects()),
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
                champPanel.add(getChampionIcon(championList.get(i)));
                if (championList.size() > 3 && championList.size() % 2 > 0 && (championList.size() / 2 == i)) {
                    champPanel.add(new JLabel());
                }
            }
            addChild(champPanelContainer, champPanel);
            addChild(panel, champPanelContainer);

            addChild(popup, panel);
        }

        addHeaderEntry(main.getLang(key), getCharButton(iconChar), popup);
    }

    private void addItemBuilderPopup() {
        JPanel popup = new JPanel();
        popup.setLayout(new BoxLayout(popup, BoxLayout.X_AXIS));
        popup.setForeground(TEXT_COLOR);
        popup.setBackground(HOVER_BACKGROUND);
        popup.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel itemList = new JPanel();
        itemList.setBorder(BorderFactory.createEmptyBorder(ICON_SIZE + 6, 0, 0, 0));
        itemList.setLayout(new BoxLayout(itemList, BoxLayout.Y_AXIS));
        itemList.add(Box.createVerticalGlue());
        addChild(popup, itemList);

        JPanel infoContainer = new JPanel();
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
        infoContainer.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        addChild(popup, infoContainer);

        JPanel combinedItems = new JPanel();
        addChild(infoContainer, combinedItems);
        infoContainer.add(Box.createVerticalGlue());

        Map<TftItem, Integer> counts = new HashMap<>();
        Map<TftItem, JSpinner> spinnerMap = new HashMap<>();
        Multimap<TftItem, JLabel> combinationIcons = MultimapBuilder.hashKeys().arrayListValues().build();

        for (TftItem item : main.getProvider().getItems().values()) {
            if (item.getIngredients().isEmpty()) {
                JPanel line = new JPanel();
                line.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
                JLabel itemIcon = new JLabel(new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)));
                itemIcon.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16), COLORED_BORDER));
                itemIcon.setToolTipText(main.getLang("item-hover", getItemReplacements(item)));
                line.add(itemIcon);

                JLabel itemHeaderIcon = new JLabel(new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)));
                itemHeaderIcon.setBorder(COLORED_BORDER);
                itemHeaderIcon.setToolTipText(main.getLang("item-hover", getItemReplacements(item)));
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
                            combinedIcon.setToolTipText(main.getLang("item-hover", getItemReplacements(combinedItem)));

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
                        c.setCursor(main.getCursorClick());
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

                addChild(line, spinner);
                addChild(itemList, line);
            }
        }

        combinedItems.setLayout(new GridLayout(0, spinnerMap.size()));

        for (TftItem item : main.getProvider().getItems().values()) {
            if (item.getIngredients().isEmpty()) {
                for (TftItem otherItem : main.getProvider().getItems().values()) {
                    if (otherItem.getIngredients().isEmpty()) {
                        TftItem combinedItem = main.getProvider().getCombination(item, otherItem);

                        BufferedImage image = main.getImage(combinedItem.getIconUrl(), ICON_SIZE, ICON_SIZE);
                        JLabel combinedItemIcon = new JLabel(new ImageIcon(image));
                        combinedItemIcon.setBorder(BorderFactory.createLineBorder(item == otherItem ? new Color(100, 100, 100) : GRID_COLOR));
                        combinedItemIcon.setToolTipText(main.getLang("item-hover", getItemReplacements(combinedItem)));
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

        addChild(itemList, new LabelButton(main, main.getLang("reset"), e -> spinnerMap.values().forEach(s -> s.setValue(0))));

        addHeaderEntry(main.getLang("item-builder"), getCharButton("I"), popup);
    }

    private void addItemsPopups() {
        for (TftItem item : main.getProvider().getItems().values()) {
            if (item.getIngredients().isEmpty()) {

                JPanel combinations = new JPanel();
                combinations.setLayout(new BoxLayout(combinations, BoxLayout.Y_AXIS));
                combinations.setBackground(HOVER_BACKGROUND);

                JPanel line = new JPanel();
                line.setLayout(new FlowLayout(FlowLayout.LEADING));
                line.add(new JLabel(main.getLang("item-info", getItemReplacements(item)), new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)), JLabel.LEADING));
                addChild(combinations, line);

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
                    itemIcon.setToolTipText(main.getLang("item-hover", getItemReplacements(ingredient)));
                    itemIcon.setBorder(COLORED_BORDER);
                    combLine.add(itemIcon);

                    JLabel arrow = new JLabel(" > ");
                    arrow.setForeground(TEXT_COLOR);
                    arrow.setFont(HUGE_FONT);
                    combLine.add(arrow);

                    JLabel combinedLabel = new JLabel(main.getLang("item-info-combined", getItemReplacements(combinedItem)), new ImageIcon(main.getImage(combinedItem.getIconUrl(), ICON_SIZE, ICON_SIZE)), JLabel.LEADING);
                    combinedLabel.setToolTipText(main.getLang("item-hover", getItemReplacements(combinedItem)));
                    combinedLabel.setFont(FONT);
                    combLine.add(combinedLabel);
                    addChild(combinations, combLine);
                });

                addHeaderEntry(item.getName(), new JLabel("", new ImageIcon(main.getImage(item.getIconUrl(), ICON_SIZE, ICON_SIZE)), JLabel.CENTER), combinations);
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

    private String addHilights(String text) {
        return text.replaceAll("\\((\\d+)\\)", "<font color=\"#E69A2E\">($1)</font>")
                .replaceAll("((\\+| )\\d+(%|st|nd|rd|th|s| ?seconds| |))", "<font color=\"#FFFFFF\">$1</font>")
                .replaceAll("([Hh]ealth( Point(s?)|)|HP|heal(ing|)|Armor)", " <font color=\"#9AE62E\">$1</font>")
                .replaceAll("([Mm]ana|Spell Power|magical damage)", " <font color=\"#2EA7E6\">$1</font>")
                .replaceAll("([Aa]ttack( Speed| Range|s|)|AS|[Dd]amage)", " <font color=\"#E6482E\">$1</font>");
    }

    private String[] getItemReplacements(TftItem item) {
        return new String[]{
                "name", item.getName(),
                "champions", item.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", ")),
                "champions-with-icons", item.getChampions().stream()
                        .map(c -> "<br><img src=\"file:/" + main.getCachedImageFile(c.getIconUrl(), 24, 24).getAbsolutePath() + "\">"
                                + "&nbsp;" + c.getName()).collect(Collectors.joining("")),
                "desc", addHilights(item.getDescription()),
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

    private JLabel getChampionIcon(TftChampion champion) {
        JLabel championLabel = new JLabel("", new ImageIcon(main.getImage(champion.getIconUrl(), 24, 24)), JLabel.CENTER);
        championLabel.setToolTipText(main.getLang("champion-hover",
                "name", champion.getName(),
                "synergies", champion.getSynergies().stream()
                        .map(TftSynergy::getName).collect(Collectors.joining(", ")),
                "synergies-with-icons", champion.getSynergies().stream()
                        .map(s -> "<br><img src=\"file:/" + main.getCachedImageFile(s.getIconUrl(), 16, 16).getAbsolutePath() + "\">"
                                + "&nbsp;" + s.getName()).collect(Collectors.joining("")),
                "iconUrl", champion.getIconUrl().toExternalForm(),
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
                "spell-desc", addHilights(champion.getSpell().getDescription()),
                "spell-effect", addHilights(champion.getSpell().getEffect()),
                "spell-mana", champion.getSpell().getMana(),
                "spell-type", champion.getSpell().getType()
        ));
        championLabel.setBorder(BorderFactory.createLineBorder(champion.getColor()));
        return championLabel;
    }

    public void addHeaderEntry(String name, JLabel icon, JPanel container) {
        addHeaderEntry(name, icon, container, true);
    }

    public void addHeaderEntry(String name, JLabel icon, JPanel popup, boolean alignToButton) {
        icon.setBorder(COLORED_BORDER);
        //icon.setToolTipText(name);

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

    private void addChild(JComponent parent, JComponent child) {
        parent.add(child);
        child.setForeground(parent.getForeground());
        child.setBackground(new Color(0, 0, 0, 0));
    }

    private class WindowMover extends JPanel {
        private Point startClick;

        public WindowMover(JFrame parent) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursor(Cursor.getDefaultCursor());
                }

                @Override
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


