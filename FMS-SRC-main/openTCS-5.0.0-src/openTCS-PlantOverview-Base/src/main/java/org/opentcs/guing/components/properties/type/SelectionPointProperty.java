package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.model.ModelComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectionPointProperty <E>
        extends AbstractProperty
        implements Selectable<E> {

    /**
     * Die möglichen Werte.
     */
    private List<E> fPossibleValues;

    /**
     * Standardkonstruktor.
     *
     * @param model
     */
    public SelectionPointProperty(ModelComponent model) {
        this(model, new ArrayList<>(), "");
    }

    /**
     * Creates a new instance of SelectionProperty
     *
     * @param model
     *
     * @param possibleValues
     * @param value
     */
    public SelectionPointProperty(ModelComponent model, List<E> possibleValues,
                             Object value) {
        super(model);
        setPossibleValues(possibleValues);
        fValue = value;
    }

    @Override
    public Object getComparableValue() {
        return fValue;
    }

    /**
     * Setzt die möglichen Werte im Nachhinein.
     *
     * @param possibleValues Ein Array mit den möglichen Werte.
     */
    @Override
    public void setPossibleValues(List<E> possibleValues) {
        fPossibleValues = Objects.requireNonNull(possibleValues, "possibleValues is null");
    }

    @Override
    public void setValue(Object value) {
        fValue = value;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public List<E> getPossibleValues() {
        return fPossibleValues;
    }

    @Override
    public void copyFrom(Property property) {
        AbstractProperty selectionProperty = (AbstractProperty) property;
        setValue(selectionProperty.getValue());
    }
}
