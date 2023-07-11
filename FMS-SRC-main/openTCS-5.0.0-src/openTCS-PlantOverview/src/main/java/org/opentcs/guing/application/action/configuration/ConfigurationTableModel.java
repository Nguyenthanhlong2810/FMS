package org.opentcs.guing.application.action.configuration;

import org.opentcs.database.entity.AubotConfiguration;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class ConfigurationTableModel
    extends AbstractTableModel {

  private static final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.CONFIGURATION_PATH);

  private ArrayList<AubotConfiguration> confList;

  public ConfigurationTableModel(ArrayList<AubotConfiguration> arrayList) {
    this.confList = arrayList;
  }

  @Override
  public int getRowCount() {
    return confList.size();
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return confList.get(rowIndex).toArray()[columnIndex];
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return bundle.getString("table.header.name");
      case 1:
        return bundle.getString("table.header.value");
      case 2:
        return bundle.getString("table.header.description");
      default:
        return "???";
    }
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  public AubotConfiguration getConfiguration(int position) {
    return confList.get(position);
  }

  public void addElement(AubotConfiguration element) {
    confList.add(element);
    this.fireTableDataChanged();
  }

  public void removeElement(AubotConfiguration element) {
    confList.remove(element);
    this.fireTableDataChanged();
  }

  public void modifyElement(AubotConfiguration element) {
    for (int i = 0; i < confList.size(); i++) {
      if (confList.get(i).getId() == element.getId()) {
        confList.set(i, element);
        this.fireTableDataChanged();
        break;
      }
    }
  }

  public boolean duplicateName(AubotConfiguration element) {
    for (AubotConfiguration conf : confList) {
      if (conf.getName().equalsIgnoreCase(element.getName()) && conf.getId() != element.getId()) {
        return true;
      }
    }
    return false;
  }
}
