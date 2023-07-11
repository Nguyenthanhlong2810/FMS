/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui;

import java.awt.Image;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static methods related to window icons.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public final class Icons {

    /**
     * This class's logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Icons.class);
    /**
     * Path to the openTCS window icons.
     */
    private static final String ICON_PATH = "/org/opentcs/util/gui/res/icons/";
    /**
     * File names of the openTCS window icons.
     */
    private static final String[] ICON_FILES = {"16ico.png",
            "32ico.png",
            "64ico.png",
            "128ico.png",
            "256ico.png"};

    private static  final String[] ICON_FILES_NAME =
                    {"ico16.png",
                    "ico32.png",
                    "ico64.png",
                    "ico128.png",
                    "ico256.png"};
    private static final String SPLASHFRAME_ICON = "256ico.png";
    /**
     * Prevents instantiation.
     */
    private Icons() {
        // Do nada.
    }

    /**
     * Get the icon for OpenTCS windows in different resolutions.
     *
     * @return List of icons
     */
    public static List<Image> getImages(){
        try {
            List<Image> icons = new LinkedList<>();
            for (String iconFile : ICON_FILES_NAME) {
                String iconURL = ICON_PATH + iconFile;
                final Image icon = ImageIO.read(Icons.class.getResource(iconURL));
                icons.add(icon);
            }
            return icons;
        }
        catch (IOException | IllegalArgumentException exc) {
            LOG.warn("Couldn't load icon images from path {}", ICON_PATH, exc);
            return new LinkedList<>();
        }
    }
    public static ImageIcon getImageIcon() {
        String url = ICON_PATH + SPLASHFRAME_ICON;
        ImageIcon image = new ImageIcon(url);
        return image;
    }
    public static List<Image> getOpenTCSIcons() {
        try {
            List<Image> icons = new LinkedList<>();
            for (String iconFile : ICON_FILES) {
                String iconURL = ICON_PATH + iconFile;
                final Image icon = ImageIO.read(Icons.class.getResource(iconURL));
                icons.add(icon);
            }
            return icons;
        }
        catch (IOException | IllegalArgumentException exc) {
            LOG.warn("Couldn't load icon images from path {}", ICON_PATH, exc);
            return new LinkedList<>();
        }
    }

}
