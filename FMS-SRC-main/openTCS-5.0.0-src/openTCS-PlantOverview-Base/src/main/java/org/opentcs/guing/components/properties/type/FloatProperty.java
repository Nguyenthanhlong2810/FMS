package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.model.ModelComponent;

public class FloatProperty
    extends AbstractProperty{

    public FloatProperty(ModelComponent model) {
        this(model, 0);
    }

    public FloatProperty(ModelComponent model, float value) {
        super(model);
        setValue(value);
    }

    @Override
    public Object getComparableValue() {
        return String.format("%4.2f", fValue);
    }

    @Override
    public String toString() {
        return fValue instanceof Float ? Float.toString((float) fValue) : (String) fValue;
    }

    @Override
    public void copyFrom(Property property) {
        FloatProperty floatProperty = (FloatProperty) property;
        setValue(floatProperty.getValue());
    }
}
