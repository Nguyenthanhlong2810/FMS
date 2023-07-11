package org.opentcs.guing.components.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

public class ScrollablePanel
        extends JPanel
        implements Scrollable, ComponentListener, ContainerListener {

  private static final int ORIGIN_WIDTH = 180;

  private static final int OFFSET = 10;

  private final Dimension dimension = new Dimension(ORIGIN_WIDTH, 202);

  public ScrollablePanel() {
    super();
    setLayout(new ModifiedFlowLayout(FlowLayout.LEFT, OFFSET, OFFSET));
    addComponentListener(this);
    addContainerListener(this);
  }

  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 10;
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 5;
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  @Override
  public void componentResized(ComponentEvent e) {
    int width = e.getComponent().getWidth();
    if (width < ORIGIN_WIDTH) {
      return;
    }
    int bonus = (width - OFFSET) % (ORIGIN_WIDTH + OFFSET);
    int childColumn = (width - OFFSET) / (ORIGIN_WIDTH + OFFSET);
    dimension.width = ORIGIN_WIDTH + bonus / childColumn;
    doLayout();
    revalidate();
  }

  @Override
  public void componentMoved(ComponentEvent e) {

  }

  @Override
  public void componentShown(ComponentEvent e) {

  }

  @Override
  public void componentHidden(ComponentEvent e) {

  }

  @Override
  public void componentAdded(ContainerEvent e) {
    e.getChild().setPreferredSize(dimension);
  }

  @Override
  public void componentRemoved(ContainerEvent e) {

  }
}
