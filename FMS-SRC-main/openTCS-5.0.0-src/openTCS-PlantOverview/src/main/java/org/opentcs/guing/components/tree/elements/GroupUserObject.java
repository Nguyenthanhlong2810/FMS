/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import javax.swing.*;

import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.persistence.ModelManager;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A folder for a Group in the "Group" tree.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class GroupUserObject
    extends AbstractUserObject {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model object
   * @param view The openTCS view
   * @param editor The drawing editor
   * @param modelManager The model manager
   */
  @Inject
  public GroupUserObject(@Assisted GroupModel model,
                         OpenTCSView view,
                         OpenTCSDrawingEditor editor,
                         ModelManager modelManager) {
    super(model, view, editor, modelManager);
  }

  @Override
  public GroupModel getModelComponent() {
    return (GroupModel) super.getModelComponent();
  }

  @Override
  public ImageIcon getIcon() {
    BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = image.createGraphics();

    graphics.setPaint(getModelComponent().getPropertyColor().getColor());
    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

    return new ImageIcon(image);
  }
}
