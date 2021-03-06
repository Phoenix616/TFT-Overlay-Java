package de.themoep.tftoverlay.elements;

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
import de.themoep.tftoverlay.windows.Overlay;
import lombok.Setter;

import javax.swing.JLabel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class LabelButton extends JLabel {

    @Setter private Consumer<MouseEvent> action;

    public LabelButton(String text, Consumer<MouseEvent> action) {
        super(text);
        this.action = action;

        setForeground(Overlay.TEXT_COLOR);
        setBorder(Overlay.BUTTON_BORDER);
        setBackground(Overlay.BACKGROUND);
        setOpaque(true);
        setCursor(TftOverlay.CURSOR_CLICK);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(Overlay.GRID_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Overlay.BACKGROUND);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    LabelButton.this.action.accept(e);
                }
            }
        });
    }


}
