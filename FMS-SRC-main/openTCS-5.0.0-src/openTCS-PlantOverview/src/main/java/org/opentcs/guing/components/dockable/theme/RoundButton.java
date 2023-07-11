package org.opentcs.guing.components.dockable.theme;

import bibliothek.extension.gui.dock.theme.eclipse.RoundRectButton;
import bibliothek.gui.dock.themes.basic.action.BasicResourceInitializer;
import bibliothek.gui.dock.themes.basic.action.BasicTrigger;
import bibliothek.gui.dock.util.AbstractPaintableComponent;
import bibliothek.gui.dock.util.BackgroundComponent;
import bibliothek.gui.dock.util.BackgroundPaint;
import bibliothek.gui.dock.util.Transparency;
import bibliothek.util.Colors;

import java.awt.*;

public class RoundButton extends RoundRectButton {

  public RoundButton(BasicTrigger trigger, BasicResourceInitializer initializer) {
    super(trigger, initializer);
    setBackground(new Color(0, 59, 229, 128));
  }

  protected void paintComponent(Graphics g) {
    BackgroundPaint paint = this.getModel().getBackground();
    BackgroundComponent component = this.getModel().getBackgroundComponent();
    if (paint == null) {
      this.doPaintBackground(g);
    } else {
      AbstractPaintableComponent paintable = new AbstractPaintableComponent(component, this, paint) {
        protected void foreground(Graphics g) {
          doPaintForeground(g);
        }

        protected void background(Graphics g) {
          doPaintBackground(g);
        }

        protected void border(Graphics g) {
        }

        protected void children(Graphics g) {
        }

        protected void overlay(Graphics g) {
        }

        public bibliothek.gui.dock.util.Transparency getTransparency() {
          return Transparency.DEFAULT;
        }
      };
      paintable.paint(g);
    }

  }

  private void doPaintBackground(Graphics g) {
    Color background = new Color(0, 0, 0, 32);
    Color pressBackground = new Color(0, 0, 0, 48);
    Color border = null;
    if (this.getModel().isMousePressed()) {
      border = pressBackground.brighter();
      background = pressBackground;
    } else if (this.getModel().isSelected() || this.getModel().isMouseInside()) {
      border = background.brighter();
    }

    int w = this.getWidth() - 1;
    int h = this.getHeight() - 1;
    if (border != null) {
      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setColor(background);
      g2d.fillOval(0, 0, w, h);
      if (this.getModel().isMousePressed()) {
        g2d.setColor(border);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(0, 0, w, h);
      }
    }

  }

  private void doPaintForeground(Graphics g) {
    Color background = this.getBackground();
    if (this.getModel().isMousePressed()) {
      background = Colors.undiffMirror(background, 0.6D);
    } else if (this.getModel().isSelected() || this.getModel().isMouseInside()) {
      background = Colors.undiffMirror(background, 0.3D);
    }

    int w = this.getWidth() - 1;
    int h = this.getHeight() - 1;
    this.paintChildren(g);
    if (this.hasFocus() && this.isFocusable() && this.isEnabled()) {
      g.setColor(Colors.diffMirror(background, 0.4D));
      g.drawLine(2, 3, 2, 4);
      g.drawLine(3, 2, 4, 2);
      g.drawLine(w - 2, 3, w - 2, 4);
      g.drawLine(w - 3, 2, w - 4, 2);
      g.drawLine(2, h - 3, 2, h - 4);
      g.drawLine(3, h - 2, 4, h - 2);
      g.drawLine(w - 2, h - 3, w - 2, h - 4);
      g.drawLine(w - 3, h - 2, w - 4, h - 2);
    }
  }

  private void doPaintBorder(Graphics g) {

  }
}
