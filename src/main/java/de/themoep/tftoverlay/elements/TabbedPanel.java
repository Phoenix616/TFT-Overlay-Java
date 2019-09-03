package de.themoep.tftoverlay.elements;

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
import de.themoep.tftoverlay.Utils;
import de.themoep.tftoverlay.windows.Overlay;
import lombok.Getter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class TabbedPanel extends JPanel {
    private static final Border HILIGHT_BORDER = BorderFactory.createLineBorder(Overlay.TEXT_COLOR);
    private final JFrame parent;
    private final boolean alignToButton;
    private final boolean requiresClick;
    private final JPanel header;
    private final JPanel popupContainer;

    private final Map<JLabel, JPanel> entries = new LinkedHashMap<>();

    public TabbedPanel(JFrame parent) {
        this(parent, true,  false);
    }

    public TabbedPanel(JFrame parent, boolean alignToButton, boolean requiresClick) {
        super();

        this.parent = parent;
        this.alignToButton = alignToButton;
        this.requiresClick = requiresClick;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        popupContainer = new JPanel();
        header = new JPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEADING));
        header.setBackground(new Color(0, 0, 0, 0));

        Utils.addChild(this, header);
        Utils.addChild(this, popupContainer);
    }

    public JPanel addEntry(String name, JLabel icon, JPanel popup) {
        return addEntry(name, icon, popup, false);
    }

    public JPanel addEntry(String name, JLabel icon, JPanel popup, boolean showByDefault) {
        icon.setBorder(Overlay.COLORED_BORDER);
        //icon.setToolTipText(name);

        JPanel container = new JPanel();
        container.setVisible(false);
        container.add(popup);
        Utils.addChild(popupContainer, container);

        if (requiresClick) {
            icon.setCursor(TftOverlay.CURSOR_CLICK);
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setActive(icon, container);
                }
            });
        } else {
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setActive(icon, container);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (icon.getHeight() + 1 > e.getY()) {
                        icon.setBackground(Overlay.BACKGROUND);
                        icon.setBorder(Overlay.COLORED_BORDER);
                        container.setVisible(false);
                        parent.pack();
                    }
                }
            });
            popup.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    Point location = SwingUtilities.convertPoint(popupContainer, popup.getLocation(), popup);
                    if (e.getX() > 0 && e.getX() < location.getX() + popup.getWidth()
                            && e.getY() > 0 && e.getY() < location.getY() + popup.getHeight()) {
                        return;
                    }
                    icon.setBackground(Overlay.BACKGROUND);
                    icon.setBorder(Overlay.COLORED_BORDER);
                    container.setVisible(false);
                    parent.pack();
                }
            });
        }
        header.add(icon);
        entries.put(icon, container);
        if (showByDefault) {
            setActive(icon, container);
        }
        return container;
    }

    private void hideActive() {
        for (Map.Entry<JLabel, JPanel> entry : entries.entrySet()) {
            entry.getKey().setBackground(Overlay.BACKGROUND);
            entry.getKey().setBorder(Overlay.COLORED_BORDER);
            entry.getValue().setVisible(false);
        }
    }

    private void setActive(JLabel icon, JPanel container) {
        hideActive();
        icon.setBorder(HILIGHT_BORDER);
        container.setVisible(true);
        if (alignToButton) {
            container.setBorder(BorderFactory.createEmptyBorder(0, icon.getX() - 10, 0, 0));
        }
        parent.pack();
    }

}
