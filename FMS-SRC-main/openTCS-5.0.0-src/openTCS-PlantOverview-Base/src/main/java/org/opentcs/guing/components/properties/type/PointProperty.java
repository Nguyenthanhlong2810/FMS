package org.opentcs.guing.components.properties.type;

import org.opentcs.data.model.Point;
import org.opentcs.guing.model.ModelComponent;

import java.util.List;

public class PointProperty extends SelectionPointProperty<String> {

    public PointProperty(ModelComponent model) {
        super(model);
    }

    public PointProperty(ModelComponent model,
                             List<String> possibleValues,
                             Object value) {
        super(model, possibleValues, value);
    }

}
