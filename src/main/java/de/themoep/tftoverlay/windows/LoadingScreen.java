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

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class LoadingScreen extends JFrame {
    private final JLabel textElement;
    private String text = "";

    public LoadingScreen(TftOverlay main) throws HeadlessException {
        super(main.getName() + " v" + main.getVersion() + " is loading...");
        setIconImage(main.getIcon());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel();
        setContentPane(content);
        content.setOpaque(false);
        setAlwaysOnTop(true);
        setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        try {
            BufferedImage logoImage = ImageIO.read(main.getResourceAsStream("images/TFT-Overlay-Logo.png"));
            content.add(new JLabel(new ImageIcon(logoImage)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        content.add(textElement = new JLabel());
        textElement.setPreferredSize(new Dimension(0, 120));
        textElement.setOpaque(true);
        textElement.setBackground(Overlay.HOVER_BACKGROUND);
        textElement.setForeground(new Color(158, 108, 54));
        textElement.setBorder(Overlay.BORDER);
        setText(main.getName() + " " + main.getVersion() + " is loading...");
    }

    public void addLine(String line) {
        setText(text + "\n" + line);
    }

    public void setText(String text) {
        this.text = text;
        textElement.setText("<html>" + text.replace("\n", "<br>") + "</html>)");
        pack();
    }

    public String getText() {
        return text;
    }

    public void clearText() {
        textElement.setText("");
    }
}
