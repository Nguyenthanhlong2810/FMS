/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp.exchange;

import cfg.aubot.commadapter.tcp.telegrams.OrderRequest;

import javax.swing.*;
import java.awt.*;

/**
 * Renders order telegrams when displayed in a list.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OrderListCellRenderer
    extends DefaultListCellRenderer {

  @Override
  public Component getListCellRendererComponent(JList<?> list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus) {
    Component component = super.getListCellRendererComponent(list,
                                                             value,
                                                             index,
                                                             isSelected,
                                                             cellHasFocus);

    if (value instanceof OrderRequest) {
      OrderRequest request = (OrderRequest) value;
      JLabel label = (JLabel) component;

      StringBuilder sb = new StringBuilder();
      sb.append('#');
      sb.append(request.getOrderId());
      sb.append(": Dest.: ");
      sb.append(request.getDestinationId());
      sb.append(", Act.: ");
      sb.append(request.getDestinationAction());
      sb.append("...");

      label.setText(sb.toString());
    }

    return component;
  }
}
