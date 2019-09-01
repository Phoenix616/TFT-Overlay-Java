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
import de.themoep.utils.lang.simple.LanguageManager;
import de.themoep.utils.lang.simple.Languaged;
import lombok.Getter;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.xml.bind.DatatypeConverter;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Getter
public class TftOverlay implements Languaged {
    private static MessageDigest HASHING;
    private static TftOverlay instance;

    private static final User USER = new Languaged.User() {};

    private Properties properties = new Properties();
    private LoadingScreen loading;
    private Overlay overlay;
    private DataProvider provider;
    private LanguageManager lang;
    private File cacheFolder;
    private BufferedImage icon;

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

        loading = new LoadingScreen(this);
        loading.setLocationRelativeTo(null);
        loading.setVisible(true);

        loadProvider();
        loading.addLine("Provider loaded!");


        overlay = new Overlay(this);
        loading.addLine("Overlay loaded!");

        loading.setVisible(false);
        loading.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        loading.dispose();

        overlay.setVisible(true);
    }

    private void loadProvider() {
        File dataCacheFolder = new File(cacheFolder, "data");
        dataCacheFolder.mkdirs();
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
        long timeStamp = 0;
        // TODO: Use patch version to check for updates
        if (cacheProps.getProperty("timestamp") != null) {
            try {
                timeStamp = Long.parseLong(cacheProps.getProperty("timestamp"));
            } catch (NumberFormatException ignored) {}
        }
        if (cacheProps.getProperty("this-prevents-any-updates") != null || timeStamp + TimeUnit.DAYS.toMillis(1) > System.currentTimeMillis()) {
            loading.addLine("Using cached data...");
            try {
                provider = new CachedDataProvider(this, dataCacheFolder);
                return;
            } catch (IOException e) {
                loading.addLine("Error:" + e.getMessage());
                e.printStackTrace();
            }
        }

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

        loading.addLine("Caching origins...");
        provider.getOrigins().values().forEach(o -> cache(new File(dataCacheFolder, "origins"), o));

        loading.addLine("Caching classes...");
        provider.getClasses().values().forEach(c -> cache(new File(dataCacheFolder, "classes"), c));

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

    public Image getImage(URL url, int width, int height) {
        File imageCacheFolder = new File(cacheFolder, "images");
        File scaledImageFile = new File(imageCacheFolder, hash(url.toString()) + "-" + width + "-" + height + ".png");
        File imageFile = new File(imageCacheFolder, hash(url.toString()) + ".png");
        BufferedImage image = null;
        if (imageCacheFolder.exists()) {
            if (scaledImageFile.exists()) {
                try {
                    return ImageIO.read(scaledImageFile);
                } catch (IOException e) {
                    loading.addLine(e.getMessage());
                    e.printStackTrace();
                }
            }
            if (imageFile.exists()) {
                try {
                    image = ImageIO.read(imageFile);
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
                ImageIO.write(image, "png", imageFile);
            }
            image = Scalr.resize(image, width, height);
            ImageIO.write(image, "png", scaledImageFile);
            return image;
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
