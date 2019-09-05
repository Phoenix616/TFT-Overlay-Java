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
import de.themoep.tftoverlay.elements.WindowMover;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RecordingWindow extends JFrame {
    private final TftOverlay main;
    private final JLabel label;
    private ScheduledFuture<?> thread;

    private long lastActive = 0;
    private AWTEventListener awtEventListener;

    public RecordingWindow (TftOverlay main) {
        super(main.getName() + " " + main.getLang("recording-window"));
        this.main = main;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        setBackground(new Color(0, 0, 0));
        setIconImage(main.getIcon());

        awtEventListener = event -> setLastActive();

        WindowMover mover = new WindowMover(this);
        mover.setOpaque(false);
        mover.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        add(mover);
        mover.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        label = new JLabel();
        label.setOpaque(false);
        mover.add(label);
    }

    public void start() {
        if (thread != null && !thread.isCancelled()) {
            thread.cancel(true);
        }

        Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
        Toolkit.getDefaultToolkit().addAWTEventListener(awtEventListener, AWTEvent.MOUSE_EVENT_MASK);

        thread = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> {
            if (lastActive + 100 > System.currentTimeMillis()) {
                BufferedImage img = new BufferedImage(main.getOverlay().getWidth(), main.getOverlay().getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = img.createGraphics();
                g2d.setBackground(new Color(0, 255, 19));
                main.getOverlay().printAll(g2d);
                label.setIcon(new ImageIcon(img));
                g2d.dispose();
                pack();
            }
        }), 0, 100, TimeUnit.MILLISECONDS);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void stop() {
        if (thread != null && !thread.isCancelled()) {
            thread.cancel(true);
        }
        setVisible(false);
        Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
    }

    public void setLastActive() {
        lastActive = System.currentTimeMillis();
    }
}
