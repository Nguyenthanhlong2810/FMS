package org.opentcs.guing.transport;

import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.model.elements.GroupModel;
import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RouteListCellRender extends JPanel implements ListCellRenderer<GroupModel> {

  private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
  private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
  protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

  private final OpenTCSView openTCSView;

  private JLabel labelColor = new JLabel();
  private JLabel labelItem = new JLabel();

  public RouteListCellRender(OpenTCSView openTCSView) {
    this.openTCSView = openTCSView;
    setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));

    labelItem.setHorizontalAlignment(JLabel.LEFT);
    labelColor.setPreferredSize(new Dimension(16, 16));
    labelColor.setBorder(BorderFactory.createLineBorder(Color.white));

    add(labelColor);
    add(labelItem);
  }

  @Override
  public Component getListCellRendererComponent(JList list, GroupModel value,
                                                int index, boolean isSelected, boolean cellHasFocus) {

    setComponentOrientation(list.getComponentOrientation());

    Color bg = null;
    Color fg = null;

    JList.DropLocation dropLocation = list.getDropLocation();
    if (dropLocation != null
            && !dropLocation.isInsert()
            && dropLocation.getIndex() == index) {

      bg = DefaultLookup.getColor(this, ui, "List.dropCellBackground");
      fg = DefaultLookup.getColor(this, ui, "List.dropCellForeground");

      isSelected = true;
    }

    if (isSelected) {
      setBackground(bg == null ? list.getSelectionBackground() : bg);
      setForeground(fg == null ? list.getSelectionForeground() : fg);
      openTCSView.focusGroup(value, -1);
    }
    else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }

    labelItem.setText(value.getName());
    labelColor.setBackground(value.getPropertyColor().getColor());
    labelColor.setOpaque(true);

    setEnabled(list.isEnabled());
    setFont(list.getFont());

    Border border = null;
    if (cellHasFocus) {
      if (isSelected) {
        border = DefaultLookup.getBorder(this, ui, "List.focusSelectedCellHighlightBorder");
      }
      if (border == null) {
        border = DefaultLookup.getBorder(this, ui, "List.focusCellHighlightBorder");
      }
    } else {
      border = getNoFocusBorder();
    }
    setBorder(border);

    return this;
  }

  private Border getNoFocusBorder() {
    Border border = DefaultLookup.getBorder(this, ui, "List.cellNoFocusBorder");
    if (System.getSecurityManager() != null) {
      if (border != null) return border;
      return SAFE_NO_FOCUS_BORDER;
    } else {
      if (border != null &&
              (noFocusBorder == null ||
                      noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
        return border;
      }
      return noFocusBorder;
    }
  }
}
