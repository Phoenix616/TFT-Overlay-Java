package de.themoep.tftoverlay;

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

import de.themoep.tftoverlay.windows.Overlay;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class Utils {

    public static <T> Map<T, T> map(T... t) {
        Map<T, T> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < t.length; i += 2) {
            map.put(t[i], t[i+1]);
        }
        return map;
    }

    public static void addChild(JComponent parent, JComponent child) {
        parent.add(child);
        child.setForeground(parent.getForeground());
        child.setBackground(new Color(0, 0, 0, 0));
    }

    public static void addTooltip(JComponent component, String text) {
        JPopupMenu tooltip = new JPopupMenu(text);
        tooltip.setBackground(Overlay.HOVER_BACKGROUND);
        tooltip.setBorder(Overlay.BORDER);
        tooltip.setLayout(new GridLayout(0, 1));
        JLabel label = new JLabel(text);
        label.setForeground(Overlay.TEXT_COLOR);
        tooltip.add(label);
        tooltip.pack();
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (component.isShowing()) {
                    tooltip.show(component, component.getWidth() + 20, component.getHeight() + 20);
                    tooltip.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point location = SwingUtilities.convertPoint(component, component.getLocation(), e.getComponent());
                if (e.getX() > 0 && e.getX() < location.getX() + component.getWidth()
                        && e.getY() > 0 && e.getY() < location.getY() + component.getHeight()) {
                    return;
                }
                tooltip.setVisible(false);
            }
        });
    }

    public static String addHilights(String text) {
        return text.replaceAll("\\((\\d+)\\)", "<font color=\"#E69A2E\">($1)</font>")
                .replaceAll("(([+ ])\\d+(%|st|nd|rd|th|s| ?seconds| |))", "<font color=\"#FFFFFF\">$1</font>")
                .replaceAll("([Hh]ealth( Point(s?)|)|HP|heal(ing|)|Armor)", " <font color=\"#9AE62E\">$1</font>")
                .replaceAll("([Mm]ana|Spell Power|magic(al|) damage|Magic)", " <font color=\"#2EA7E6\">$1</font>")
                .replaceAll("([Aa]ttack(ing| [Ss]peed| [Rr]ange|s|)|AS|[Dd]amag(es?|img))", " <font color=\"#E6482E\">$1</font>");
    }
}
