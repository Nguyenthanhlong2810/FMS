package org.opentcs.guing.application.menus.menubar;

import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.inject.Inject;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class LanguageMenu extends JMenu {

  public static final String ID = "application.language";

  private static final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.MENU_PATH);

  private final String[] locales = {"en", "de", "vi"};

  @Inject
  public LanguageMenu() {
    super(labels.getString("languageMenu.text"));
    for (String locate : locales) {
      JMenuItem languageItem = add(createLanguageMenuItem(locate));
      languageItem.setText(locate);
    }
  }

  AbstractAction createLanguageMenuItem(String locale) {
    return new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null,
                labels.getFormatted("languageMenu.confirmText", locale),
                labels.getString("languageMenu.confirm"),
                JOptionPane.YES_NO_OPTION)
                != JOptionPane.OK_OPTION) {
          return;
        }
        final String filePath = "config/opentcs-plantoverview-defaults-baseline.properties";
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
          inputStream = new FileInputStream(filePath);
          Properties properties = new Properties();
          properties.load(inputStream);
          properties.setProperty("plantoverviewapp.locale", locale);

          outputStream = new FileOutputStream(filePath);
          properties.store(outputStream, "Modify locate");
          JOptionPane.showMessageDialog(null, labels.getString("languageMenu.changeLanguage"));
        } catch (IOException ex) {
          ex.printStackTrace();
        } finally {
          try {
            inputStream.close();
            outputStream.close();
          } catch (IOException ioException) {
            ioException.printStackTrace();
          }
        }
      }
    };
  }
}
