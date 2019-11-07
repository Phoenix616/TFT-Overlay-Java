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

import de.themoep.tftoverlay.data.Cacheable;
import de.themoep.tftoverlay.data.Providers.CachedDataProvider;
import de.themoep.tftoverlay.data.Providers.DataProvider;
import de.themoep.tftoverlay.data.Providers.LoLChessGgProvider;
import de.themoep.tftoverlay.windows.LoadingScreen;
import de.themoep.tftoverlay.windows.Overlay;
import de.themoep.tftoverlay.windows.RecordingWindow;
import de.themoep.utils.lang.simple.LanguageManager;
import de.themoep.utils.lang.simple.Languaged;
import lombok.Getter;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.xml.bind.DatatypeConverter;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

@Getter
public class TftOverlay implements Languaged {
    private static MessageDigest HASHING;
    private static TftOverlay instance;

    private static final User USER = new Languaged.User() {};

    public static Cursor CURSOR = Cursor.getDefaultCursor();
    public static Cursor CURSOR_CLICK = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    private Properties properties = new Properties();
    private LoadingScreen loading;
    private Overlay overlay = null;
    private RecordingWindow recordingWindow;

    private DataProvider provider;
    private LanguageManager lang;
    private File cacheFolder;
    private BufferedImage icon;
    private Map<String, BufferedImage> imageCache = new HashMap<>();

    public static void main(String[] args) {
        try {
            HASHING = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            try {
                HASHING = MessageDigest.getInstance("SHA1");
            } catch (NoSuchAlgorithmException e1) {
                System.out.println("Neither the MD5 nor the SHA1 hashing algorithms were found?");
                System.exit(1);
                return;
            }
        }

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Cannot run in headless mode!");
            System.exit(1);
            return;
        }

        try {
            BufferedImage cursorImage = ImageIO.read(TftOverlay.class.getClassLoader().getResourceAsStream("images/cursor-normal.png"));
            CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "Normal");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedImage cursorImage = ImageIO.read(TftOverlay.class.getClassLoader().getResourceAsStream("images/cursor-click.png"));
            CURSOR_CLICK = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "Click");
        } catch (IOException e) {
            e.printStackTrace();
        }

        instance = new TftOverlay();
    }

    private TftOverlay() {
        try {
            InputStream s = getResourceAsStream("app.properties");
            properties.load(s);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        try {
            icon = ImageIO.read(getResourceAsStream("images/TFT-Overlay-Icon.png"));
        } catch (IOException e) {
            icon = null;
        }

        cacheFolder = new File(getDataFolder(), "cache");
        cacheFolder.mkdirs();

        lang = new LanguageManager(this, "en", false);
        lang.setProvider(user -> Locale.getDefault().getLanguage());

        recordingWindow = new RecordingWindow(this);

        start(false);
    }

    public void start(boolean forceUpdate) {
        if (overlay != null) {
            overlay.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            overlay.setVisible(false);
            overlay.dispose();
        }
        loading = new LoadingScreen(this);
        loading.setLocationRelativeTo(null);
        loading.setVisible(true);

        loadProvider(forceUpdate);
        provider.setupCombinations();
        loading.addLine("Provider loaded!");


        loading.addLine("Loading Overlay...");
        overlay = new Overlay(this);
        loading.addLine("Overlay loaded!");

        loading.setVisible(false);
        loading.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        loading.dispose();

        overlay.setVisible(true);
    }

    private void loadProvider(boolean forceUpdate) {
        File dataCacheFolder = new File(cacheFolder, "data");
        File cachePropsFile = new File(dataCacheFolder, "cache.properties");
        Properties cacheProps = new Properties();
        if (cachePropsFile.exists()) {
            try {
                cacheProps.load(new FileReader(cachePropsFile));
            } catch (IOException e) {
                loading.addLine("Error:" + e.getMessage());
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        // TODO: Use patch version to check for updates
        if (!forceUpdate && cacheProps.getProperty("timestamp") != null && dataCacheFolder.exists()) {
            loading.addLine("Using cached data...");
            try {
                provider = new CachedDataProvider(this, dataCacheFolder);
                return;
            } catch (IOException e) {
                loading.addLine("Error:" + e.getMessage());
                e.printStackTrace();
            }
        }

        if (dataCacheFolder.exists()) {
            try {
                Files.walk(dataCacheFolder.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataCacheFolder.mkdirs();

        loading.addLine("Loading remote data...");
        provider = new LoLChessGgProvider(this);

        // Cache data

        loading.addLine("Caching data...");
        cacheProps.setProperty("timestamp", String.valueOf(System.currentTimeMillis()));
        try {
            cacheProps.store(new FileWriter(cachePropsFile), "Data cache properties");
        } catch (IOException e) {
            loading.addLine("Error:" + e.getMessage());
            e.printStackTrace();
        }

        loading.addLine("Caching items...");
        provider.getItems().values().forEach(i -> cache(new File(dataCacheFolder, "items"), i));

        loading.addLine("Caching synergies...");
        provider.getSynergies().values().forEach(s -> cache(new File(dataCacheFolder, "synergies"), s));

        loading.addLine("Caching champions...");
        provider.getChampions().values().forEach(c -> cache(new File(dataCacheFolder, "champions"), c));
    }

    private void cache(File folder, Cacheable cacheable) {
        folder.mkdirs();
        Properties props = new Properties();
        for (Map.Entry<String, Object> entry : cacheable.serialize().entrySet()) {
            props.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }
        try {
            props.store(new FileWriter(new File(folder, cacheable.getId() + ".properties")),
                    cacheable.getId() + " cache");
        } catch (IOException e) {
            loading.addLine("Error:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public File getDataFolder() {
        return Paths.get(".").toFile();
    }

    @Override
    public Logger getLogger() {
        return Logger.getGlobal();
    }

    public String getName() {
        return properties.getProperty("application.name");
    }

    public String getVersion() {
        return properties.getProperty("application.version");
    }

    public String getLang(String key, String... replacements) {
        return lang.getConfig(USER).get(key, replacements);
    }

    public File getCachedImageFile(URL url) {
        return new File(new File(cacheFolder, "images"), hash(url.toString()) + ".png");
    }

    public File getCachedImageFile(URL url, int width, int height) {
        File imageCacheFolder = new File(cacheFolder, "images");
        File scaledImageFile = new File(imageCacheFolder, hash(url.toString()) + "-" + width + "-" + height + ".png");

        if (!scaledImageFile.exists()) {
            getImage(url, width, height);
        }
        return scaledImageFile;
    }

    public BufferedImage getImage(URL url, int width, int height) {
        File imageCacheFolder = new File(cacheFolder, "images");
        File scaledImageFile = new File(imageCacheFolder, hash(url.toString()) + "-" + width + "-" + height + ".png");
        BufferedImage image = imageCache.get(scaledImageFile.getName());
        if (image != null) {
            return image;
        }
        File imageFile = getCachedImageFile(url);
        if (imageCacheFolder.exists()) {
            if (scaledImageFile.exists()) {
                try {
                    image = ImageIO.read(scaledImageFile);
                    if (image != null) {
                        imageCache.put(scaledImageFile.getName(), image);
                        return image;
                    }
                } catch (IOException e) {
                    loading.addLine(e.getMessage());
                    e.printStackTrace();
                }
            }
            image = imageCache.get(imageFile.getName());
            if (image == null && imageFile.exists()) {
                try {
                    image = ImageIO.read(imageFile);
                    if (image != null) {
                        imageCache.put(imageFile.getName(), image);
                    }
                } catch (IOException e) {
                    loading.addLine(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        imageCacheFolder.mkdirs();

        try {
            if (image == null) {
                image = ImageIO.read(url);
                if (image != null) {
                    imageCache.put(imageFile.getName(), image);
                    ImageIO.write(image, "png", imageFile);
                }
            }
            if (image != null) {
                image = Scalr.resize(image, width, height);
                imageCache.put(scaledImageFile.getName(), image);
                ImageIO.write(image, "png", scaledImageFile);
                return image;
            }
        } catch (IOException e) {
            loading.addLine(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String hash(String string) {
        return DatatypeConverter.printHexBinary(HASHING.digest(string.getBytes(StandardCharsets.UTF_8)));
    }
}
