package de.themoep.tftoverlay.windows;
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
import de.themoep.tftoverlay.elements.LabelButton;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Overlay extends JFrame {

    public static final boolean PBE = false;
    public static final Color BACKGROUND_COLOR = new Color(10, 10, 10);
    public static final Color TEXT_COLOR = new Color(230, 198, 123);
    public static final Border COLORED_BORDER = BorderFactory.createLineBorder(new Color(117, 87, 41));
    public static final Border BORDER = BorderFactory.createCompoundBorder(COLORED_BORDER, BorderFactory.createEmptyBorder(5, 5, 5, 5));
    public static final Border BUTTON_BORDER = BorderFactory.createCompoundBorder(BORDER, new EmptyBorder(0, 5, 0, 5));

    public Overlay(TftOverlay main) {
        super(main.getName() + " v" + main.getVersion());

        JPanel content = new JPanel();
        setContentPane(content);
        content.setFont(new Font("Sans-Serif", Font.PLAIN, 16));
        content.setOpaque(false);

        setAlwaysOnTop(true);
        setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        Font font = new Font("Dialog", Font.PLAIN, 12);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("ToolTip.background", new ColorUIResource(BACKGROUND_COLOR));
        UIManager.put("ToolTip.border", BORDER);
        UIManager.put("ToolTip.foreground", TEXT_COLOR);
        UIManager.put("ToolTip.width", 100);
        ToolTipManager.sharedInstance().setInitialDelay(0);

        JPanel itemCombinationContainer = new JPanel();

        JPanel items = new JPanel();
        items.setLayout(new FlowLayout(FlowLayout.LEADING));
        items.setBackground(new Color(0, 0, 0, 0));
        for (TftItem item : main.getProvider().getItems().values()) {
            if (item.getIngredients().isEmpty()) {
                JLabel itemLabel = new JLabel("", new ImageIcon(main.getImage(item.getIconUrl(), 42, 42)), JLabel.CENTER);
                //itemLabel.setToolTipText(main.getLang("item-hover",
                //        "name", item.getName(),
                //        "champions", breakLine(item.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                //        "desc", breakLine(item.getDescription()),
                //        "ingredient", breakLine(item.getIngredient().stream().map(TftItem::getName).collect(Collectors.joining(", "))),
                //        "ingredients", item.getIngredients().stream().map(TftItem::getName).collect(Collectors.joining(", "))
                //));

                JPanel itemCombinations = new JPanel();
                itemCombinations.setLayout(new BoxLayout(itemCombinations, BoxLayout.Y_AXIS));
                itemCombinations.setBackground(new Color(0, 0, 0, 200));

                JPanel line = new JPanel();
                line.setLayout(new FlowLayout(FlowLayout.LEADING));
                line.add(new JLabel(main.getLang("item-info",
                        "name", item.getName(),
                        "desc", breakLine(item.getDescription()),
                        "ingredient", breakLine(item.getIngredient().stream().map(TftItem::getName).collect(Collectors.joining(", ")))
                ), new ImageIcon(main.getImage(item.getIconUrl(), 42, 42)), JLabel.LEADING));
                addChild(itemCombinations, line);

                for (TftItem combinedItem : item.getIngredient()) {
                    JPanel combLine = new JPanel();
                    combLine.setLayout(new FlowLayout(FlowLayout.LEADING));
                    combLine.setLayout(new BoxLayout(combLine, BoxLayout.X_AXIS));

                    Font hugeFont = new Font("Dialog", Font.BOLD, 28);
                    JLabel plus = new JLabel(" + ");
                    plus.setForeground(TEXT_COLOR);
                    plus.setFont(hugeFont);
                    combLine.add(plus);

                    combLine.add(new JLabel(new ImageIcon(main.getImage(combinedItem.getOtherIngredient(item).getIconUrl(), 42, 42))));

                    JLabel arrow = new JLabel(" > ");
                    arrow.setForeground(TEXT_COLOR);
                    arrow.setFont(hugeFont);
                    combLine.add(arrow);

                    JLabel combinedLabel = new JLabel(main.getLang("item-info-combined",
                            "name", combinedItem.getName(),
                            "champions", breakLine(combinedItem.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                            "desc", breakLine(combinedItem.getDescription()),
                            "ingredients", item.getIngredients().stream().map(TftItem::getName).collect(Collectors.joining(", "))
                    ), new ImageIcon(main.getImage(combinedItem.getIconUrl(), 42, 42)), JLabel.LEADING);
                    combinedLabel.setFont(font);
                    combLine.add(combinedLabel);
                    addChild(itemCombinations, combLine);
                }
                itemCombinations.setVisible(false);
                itemCombinations.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                itemCombinationContainer.add(itemCombinations);

                itemLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        itemCombinations.setVisible(true);
                        pack();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        itemCombinations.setVisible(false);
                        pack();
                    }
                });
                items.add(itemLabel);
            }
        }

        JPanel champions = new JPanel();
        champions.setLayout(new GridLayout(main.getProvider().getOrigins().size() + 1, main.getProvider().getClasses().size() + 1));
        champions.setForeground(TEXT_COLOR);
        champions.setBackground(new Color(0, 0, 0, 200));

        JLabel cornerLabel = new JLabel("");
        champions.add(cornerLabel);

        for (TftClass tftClass : main.getProvider().getClasses().values()) {
            JLabel classLabel = new JLabel(tftClass.getName(), new ImageIcon(main.getImage(tftClass.getIconUrl(), 16, 16)), JLabel.CENTER);
            classLabel.setToolTipText(main.getLang("class-hover",
                    "name", tftClass.getName(),
                    "desc", breakLine(tftClass.getDescription()),
                    "effects", breakLine(tftClass.getEffects()),
                    "champions", breakLine(tftClass.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                    "champion-count", String.valueOf(tftClass.getChampions().size())
            ));
            classLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(27, 27, 27)));
            addChild(champions, classLabel);
        }


        for (TftOrigin origin : main.getProvider().getOrigins().values()) {
            JLabel originLabel = new JLabel(origin.getName(), new ImageIcon(main.getImage(origin.getIconUrl(), 16, 16)), JLabel.CENTER);
            originLabel.setToolTipText(main.getLang("origin-hover",
                    "name", origin.getName(),
                    "desc", breakLine(origin.getDescription()),
                    "effects", breakLine(origin.getEffects()),
                    "champions", breakLine(origin.getChampions().stream().map(TftChampion::getName).collect(Collectors.joining(", "))),
                    "champion-count", String.valueOf(origin.getChampions().size())
            ));
            originLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(27, 27, 27)));
            addChild(champions, originLabel);

            for (TftClass tftClass : main.getProvider().getClasses().values()) {
                JPanel champCell = new JPanel();
                champCell.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, new Color(36, 36, 36)));
                champCell.setBackground(new Color(0, 0, 0, 0));
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
                    JLabel championLabel = new JLabel("", new ImageIcon(main.getImage(champion.getIconUrl(), size, size)), JLabel.CENTER);
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
                    champCell.add(championLabel);
                }
                addChild(champions, champCell);
            }
        }

        JPanel titleBar = new JPanel();
        titleBar.setLayout(new FlowLayout(FlowLayout.LEADING));

        WindowMover menuElement = new WindowMover(this);
        menuElement.setPreferredSize(new Dimension(10, 30));
        menuElement.setBackground(new Color(255, 164, 0));
        menuElement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu menu = new JPopupMenu("menu");
                    menu.setBackground(BACKGROUND_COLOR);
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
        titleBar.add(menuElement);

        titleBar.add(new LabelButton(main.getLang("items"), e -> {
            champions.setVisible(false);
            items.setVisible(true);
            itemCombinationContainer.setVisible(true);
            pack();
        }));

        titleBar.add(new LabelButton(main.getLang("champions"), e -> {
            champions.setVisible(true);
            items.setVisible(false);
            itemCombinationContainer.setVisible(false);
            pack();
        }));

        addChild(content, titleBar);
        champions.setVisible(false);
        content.add(champions);
        content.add(items);
        addChild(content, itemCombinationContainer);

        pack();
    }

    private String breakLine(String string) {
        return breakLine(string, 100);
    }

    private String breakLine(String string, int length) {
        String[] parts = string.split(" ");
        StringBuilder sb = new StringBuilder(parts[0]);
        int lineLength = parts[0].length();
        for (int i = 1; i < parts.length; i++) {
            if (lineLength + parts[i].length() < length) {
                sb.append(" ");
                lineLength += parts[i].length() + 1;
            } else {
                sb.append("<br>");
                lineLength = 0;
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
        private final JFrame parent;

        public WindowMover(JFrame parent) {
            this.parent = parent;

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    startClick = e.getPoint();
                    getComponentAt(startClick);
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


