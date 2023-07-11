package org.opentcs.guing.components.dialogs;

import javax.swing.*;
import java.awt.*;

public class RealProgressBar extends JPanel {

  private int value;

  public RealProgressBar() {
    super();
  }

  public void setValue(int value) {
    this.value = value;
    this.repaint();
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Graphics2D g2d = (Graphics2D) g;
    Color old = g2d.getColor();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(getForeground());

    int selectionHeight = getHeight() * value / 100;
    g2d.fillRoundRect(getX() + getWidth() / 2 - 12,
            getY() + getHeight() - selectionHeight, 24, selectionHeight, 10, 10);

    g2d.setColor(Color.lightGray);
    g2d.drawRoundRect(getX() + getWidth() / 2 - 12, getY(), 24, getHeight(), 10, 10);

    g2d.setColor(old);
  }
}
