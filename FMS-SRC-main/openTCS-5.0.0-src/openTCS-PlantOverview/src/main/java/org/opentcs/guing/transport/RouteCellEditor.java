package org.opentcs.guing.transport;

import org.opentcs.guing.model.elements.GroupModel;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;

public class RouteCellEditor extends BasicComboBoxEditor {
  private JPanel panel = new JPanel();
  private JLabel labelColor = new JLabel();
  private JLabel labelItem = new JLabel();
  private GroupModel selectedValue;

  public RouteCellEditor() {
    panel.setLayout(new GridBagLayout());
    panel.setLayout(new FlowLayout(FlowLayout.LEFT));
    labelItem.setHorizontalAlignment(JLabel.LEFT);
    panel.add(labelColor);
    panel.add(labelItem);
  }

  public Component getEditorComponent() {
    return this.panel;
  }

  public Object getItem() {
    return this.selectedValue;
  }

  public void setItem(Object item) {
    if (item == null) {
      return;
    }
    GroupModel routeItem = (GroupModel) item;
    selectedValue = routeItem;
    labelItem.setText(selectedValue.getName());
    labelColor.setBackground(selectedValue.getPropertyColor().getColor());
    labelColor.setOpaque(true);
  }
}
