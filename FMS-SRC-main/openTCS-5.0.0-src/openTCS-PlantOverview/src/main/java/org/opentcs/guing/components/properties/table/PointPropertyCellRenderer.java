package org.opentcs.guing.components.properties.table;

import org.opentcs.guing.components.properties.type.PointProperty;

import javax.swing.*;
import java.awt.*;


public class PointPropertyCellRenderer extends StandardPropertyCellRenderer {
    public PointPropertyCellRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
        PointProperty property = (PointProperty) value;
        label.setText((String) property.getValue());
        decorate(table, row, column, label, value);

        return this;
    }
}
