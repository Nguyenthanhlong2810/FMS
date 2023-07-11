/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import java.util.*;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.util.Colors;

/**
 * An adapter for Groups.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class GroupAdapter
    extends AbstractProcessAdapter {

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject, ModelComponent modelComponent, SystemModel systemModel, TCSObjectService objectService, @Nullable
      ModelLayoutElement layoutElement) {
    Group group = requireNonNull((Group) tcsObject, "tcsObject");
    GroupModel model = (GroupModel) modelComponent;

    model.getPropertyName().setText(group.getName());
    model.getIdWorkingRoute().setValue(group.getId());
    model.getDescriptionRoute().setValue(group.getDescription());
    updateMiscModelProperties(model, group);

    if (layoutElement != null) {
      updateModelLayoutProperties(model, layoutElement);
    }
  }

  private void updateModelLayoutProperties(GroupModel model, ModelLayoutElement layoutElement) {
    String sGroupColor = layoutElement.getProperties().get(ElementPropKeys.WORKING_ROUTE_COLOR);
    if (sGroupColor != null) {
      model.getPropertyColor().setColor(Colors.decodeFromHexRGB(sGroupColor));
    }
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    return plantModel.withGroup(
        new GroupCreationTO(modelComponent.getName())
            .withMemberNames(getMemberNames((GroupModel) modelComponent))
            .withProperties(getKernelProperties(modelComponent))
                .withId(getKernelIdRoute((GroupModel) modelComponent))
                .withDescription(getKernelDescriptionRoute((GroupModel) modelComponent))

    ).withVisualLayouts(updatedLayouts(modelComponent, plantModel.getVisualLayouts(), systemModel));
  }

  private int getKernelIdRoute(GroupModel model) {
    return (int) model.getIdWorkingRoute().getValue();
  }

  private String getKernelDescriptionRoute(GroupModel model) {
    return model.getDescriptionRoute().toString();
  }

  private Set<String> getMemberNames(GroupModel groupModel) {
    Set<String> result = new LinkedHashSet<>();
    for (ModelComponent model : groupModel.getChildComponents()) {
      result.add(model.getName());
    }

    return result;
  }

  @Override
  protected VisualLayoutCreationTO updatedLayout(ModelComponent model,
                                                 VisualLayoutCreationTO layout,
                                                 SystemModel systemModel) {
    GroupModel groupModel = (GroupModel) model;

    return layout.withModelElement(
            new ModelLayoutElementCreationTO(groupModel.getName())
                    .withProperty(ElementPropKeys.WORKING_ROUTE_COLOR,
                            Colors.encodeToHexRGB(groupModel.getPropertyColor().getColor()))
    );
  }
}
