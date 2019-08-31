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

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.HeadlessException;

public class LoadingScreen extends JFrame {
    private final JLabel textElement;
    private String text = "";

    public LoadingScreen(TftOverlay main) throws HeadlessException {
        super(main.getName() + " v" + main.getVersion() + " is loading...");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 200));
        textElement = new JLabel();
        setText(main.getName() + " is loading...");
        add(textElement);
    }

    public void addLine(String line) {
        setText(text + "\n" + line);
    }

    public void setText(String text) {
        this.text = text;
        textElement.setText("<html>" + text.replace("\n", "<br>") + "</html>)");
    }

    public String getText() {
        return text;
    }

    public void clearText() {
        textElement.setText("");
    }
}
