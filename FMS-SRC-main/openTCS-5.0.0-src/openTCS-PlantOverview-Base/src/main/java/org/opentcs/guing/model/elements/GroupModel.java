/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;

import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.properties.type.*;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SimpleFolder;

/**
 * A folder class that manages the visible state of its members.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class GroupModel
    extends SimpleFolder {

  /**
   * Key for the elements.
   */
  public static final String ELEMENTS = "groupElements";

  public static final String DESCRIPTION_ROUTE = "description";

  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  private boolean groupVisibleInAllDrawingViews = true;
  private boolean isHighlight = false;
  private final Map<String, Boolean> drawingViewVisibilityMap = new HashMap<>();
  public GroupModel() {
    this("");
  }

  public GroupModel(String name) {
    super(name);
    createProperties();
  }

  public boolean isGroupVisible() {
    return groupVisibleInAllDrawingViews;
  }

  public boolean isHighlight(){
    return isHighlight;
  }

  /**
   * Sets the visibility status this group in all drawing views to
   * <code>isGroupVisible</code>.
   *
   * @param isGroupVisible If this group should be shown or hidden
   * in all drawing views.
   */
  public void setGroupVisible(boolean isGroupVisible) {
    this.groupVisibleInAllDrawingViews = isGroupVisible;

    for (String key : drawingViewVisibilityMap.keySet()) {
      drawingViewVisibilityMap.put(key, isGroupVisible);
    }
  }

  public void setIsHighlight(boolean isHighlight) {
    this.isHighlight = isHighlight;
  }

  /**
   * Removes a drawing view from this group folder.
   *
   * @param title The title of the drawing view.
   */
  public void removeDrawingView(String title) {
    drawingViewVisibilityMap.remove(title);
    evaluateVisibilityInAllDrawingViews();
  }

  /**
   * Returns whether this group is visible in a drawing view.
   *
   * @param title The title of the drawing view.
   * @return Wehther this group is visible or not.
   */
  public boolean isGroupInDrawingViewVisible(String title) {
    if (drawingViewVisibilityMap.containsKey(title)) {
      return drawingViewVisibilityMap.get(title);
    }

    return true;
  }

  /**
   * Sets the visibility status of a drawing view. If it isn't known the
   * drawing view will be added.
   *
   * @param title The title of the drawing view.
   * @param visible If it is visible or not.
   */
  public void setDrawingViewVisible(String title, boolean visible) {
    drawingViewVisibilityMap.put(title, visible);

    if (!visible) {
      groupVisibleInAllDrawingViews = false;
    }
    else {
      evaluateVisibilityInAllDrawingViews();
    }
  }

  /**
   * Evaluates if <code>groupVisibleInAllDrawingViews</code> should
   * be set to true or false, depending on the states of every
   * drawing view variable.
   */
  private void evaluateVisibilityInAllDrawingViews() {
    groupVisibleInAllDrawingViews = true;

    for (Boolean visible : drawingViewVisibilityMap.values()) {
      if (!visible) {
        groupVisibleInAllDrawingViews = false;
        break;
      }
    }
  }

  @Override
  public void add(ModelComponent component) {
    super.add(component);
    if (!getPropertyElements().getItems().contains(component.getName())) {
      getPropertyElements().addItem(component.getName());
    }
  }

  @Override
  public void remove(ModelComponent component) {
    super.remove(component);
    getPropertyElements().getItems().remove(component.getName());
  }

  @Override  // AbstractModelComponent
  public String getTreeViewName() {
    String treeViewName =  "Route " + getName();

    return treeViewName;
  }

  @Override  // AbstractModelComponent
  public String getDescription() {
    return bundle.getString("groupModel.description");
  }

  public StringSetProperty getPropertyElements() {
    return (StringSetProperty) getProperty(ELEMENTS);
  }

  public StringProperty getDescriptionRoute() {
    return (StringProperty) getProperty(DESCRIPTION_ROUTE);
  }

  public IntegerProperty getIdWorkingRoute() {
    return (IntegerProperty) getProperty(ID_WORKING_ROUTE);
  }

  public ColorProperty getPropertyColor() {
    return (ColorProperty) getProperty(ElementPropKeys.WORKING_ROUTE_COLOR);
  }
  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  private void createProperties() {

    IntegerProperty pId = new IntegerProperty(this);
    pId.setDescription("ID Route");
    pId.setHelptext("ID Route");
    pId.setCollectiveEditable(false);
    setProperty(ID_WORKING_ROUTE, pId);
    pId.setModellingEditable(false);

    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("groupModel.property_name.description"));
    pName.setHelptext(bundle.getString("groupModel.property_name.helptext"));
    setProperty(NAME, pName);

    StringProperty pDescription = new StringProperty(this);
    pDescription.setDescription("Description");
    pDescription.setHelptext("The description of route");
    setProperty(DESCRIPTION_ROUTE, pDescription);

    ColorProperty pColor = new ColorProperty(this, Color.red);
    pColor.setDescription("Color");
    pColor.setHelptext("Color");
    setProperty(ElementPropKeys.WORKING_ROUTE_COLOR, pColor);

    StringSetProperty pElements = new StringSetProperty(this);
    pElements.setDescription(bundle.getString("groupModel.property_elements.description"));
    pElements.setModellingEditable(false);
    pElements.setOperatingEditable(false);
    setProperty(ELEMENTS, pElements);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("groupModel.property_miscellaneous.description"));
    pMiscellaneous.setHelptext(bundle.getString("groupModel.property_miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }

}
