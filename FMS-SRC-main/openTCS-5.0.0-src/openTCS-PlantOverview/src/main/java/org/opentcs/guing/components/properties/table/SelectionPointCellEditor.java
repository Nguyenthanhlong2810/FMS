package org.opentcs.guing.components.properties.table;

import org.opentcs.guing.components.properties.panel.PropertiesTableContent;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.ModelAttribute;
import org.opentcs.guing.components.properties.type.Selectable;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.UserMessageHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SelectionPointCellEditor extends AbstractPropertyCellEditor {

    /**
     * Creates a new instance of ComboBoxCellEditor
     *
     * @param comboBox
     * @param umh
     */
    ModelManager modelManager;
    public SelectionPointCellEditor(JComboBox<?> comboBox, UserMessageHelper umh, ModelManager modelManager) {
        super(comboBox, umh);
        this.modelManager = modelManager;
        comboBox.setFont(new Font("Dialog", Font.PLAIN, 12));
    }

    @Override
    @SuppressWarnings("unchecked")
    public JComboBox<Object> getComponent() {
        return (JComboBox<Object>) super.getComponent();
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {

        setValue(value);
        JComboBox<Object> comboBox = getComponent();
        java.util.List<PathModel> pathModels = modelManager.getModel().getPathModels();
        List<String> possiblePoints = new ArrayList<>();
        possiblePoints.add(" ");
        for(PathModel pathModel : pathModels){
            if(pathModel.getStartComponent().getName().equals(fProperty.getModel().getName())){
                possiblePoints.add(pathModel.getEndComponent().getName());
            }
        }
        ((Selectable) property()).setPossibleValues(possiblePoints);
        comboBox.setModel(new DefaultComboBoxModel<>(((Selectable) property()).getPossibleValues().toArray()));
        comboBox.setSelectedItem(property().getValue());
        return fComponent;
    }

    @Override
    public Object getCellEditorValue() {
        // Wenn das Objekt über den Popup-Dialog geändert wurde, wird dieser Wert übernommen
        if (property().getChangeState() == ModelAttribute.ChangeState.DETAIL_CHANGED) {
            Object value = property().getValue();  // DEBUG
        }
        else {
            // ...sonst den Wert direkt im Tabellenfeld auswählen
            Object selectedItem = getComponent().getSelectedItem();
            Object oldValue = property().getValue();
            property().setValue(selectedItem);

            if (!selectedItem.equals(oldValue)) {
                markProperty();
            }
        }

        return property();
    }

    /**
     * Liefert das Attribut.
     *
     * @return
     */
    protected AbstractProperty property() {
        return (AbstractProperty) fProperty;
    }
}