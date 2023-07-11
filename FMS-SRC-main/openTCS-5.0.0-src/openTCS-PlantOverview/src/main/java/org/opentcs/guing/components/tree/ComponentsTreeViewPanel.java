/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.inject.Inject;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.persistence.ModelManager;

/**
 * The TreeViewPanel for components.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class ComponentsTreeViewPanel
    extends AbstractTreeViewPanel {

  /**
   * Creates a new instance.
   *
   * @param undoRedoManager The undo redo manager
   */
  @Inject
  public ComponentsTreeViewPanel(UndoRedoManager undoRedoManager,
                                 ModelManager modelManager) {
    super(undoRedoManager, modelManager);
  }

  @Override // EditableComponent
  public void cutSelectedItems() {
    bufferSelectedItems(true);
  }

  @Override // EditableComponent
  public void copySelectedItems() {
    bufferSelectedItems(false);
  }

  @Override // EditableComponent
  public void pasteBufferedItems() {
    restoreItems(bufferedUserObjects, bufferedFigures);
    // Also make "Paste" undoable
    fUndoRedoManager.addEdit(new AbstractTreeViewPanel.PasteEdit(bufferedUserObjects, bufferedFigures));
  }

  @Override // EditableComponent
  public void duplicate() {
    bufferSelectedItems(false);
    restoreItems(bufferedUserObjects, bufferedFigures);
    fUndoRedoManager.addEdit(new AbstractTreeViewPanel.PasteEdit(bufferedUserObjects, bufferedFigures));
  }

  @Override // EditableComponent
  public void delete() {
    bufferSelectedItems(true);

    if (bufferedUserObjects.isEmpty() && bufferedFigures.isEmpty()) {
      return; // nothing to undo/redo
    }

    List<PathModel> deletedPaths = new ArrayList<>();
    if(bufferedFigures instanceof PathConnection) {
      deletedPaths.add(((PathConnection) bufferedFigures).getModel());
    }
    fUndoRedoManager.addEdit(new AbstractTreeViewPanel.DeleteEdit(bufferedUserObjects, bufferedFigures));
    for (PathModel deletedPath : deletedPaths){
      PointModel start = (PointModel) deletedPath.getStartComponent();
      PointModel end = (PointModel) deletedPath.getEndComponent();
      if(start.getLeftPoint().getValue().equals(end.getName())){
        start.getLeftPoint().setValue("");
      }else if(start.getRightPoint().getValue().equals(end.getName())){
        start.getRightPoint().setValue("");
      }
    }
  }

  @Override // EditableComponent
  public void selectAll() {
    // Sample implementation (HH 2014-04-08): 
    // Select all components in the currently focused tree folder
    // TODO: select all components except folders
    UserObject selectedItem = getSelectedItem();

    if (selectedItem != null) {
      ModelComponent parent = selectedItem.getParent();

      if (parent != null) {
        selectItems(new HashSet<>(parent.getChildComponents()));
      }
    }
  }

  @Override // EditableComponent
  public void clearSelection() {
    // Not used for our tree:
    // JTree's default action for <Ctrl> + <Shift> + A already does the job.
  }

  @Override // EditableComponent
  public boolean isSelectionEmpty() {
    // Not used for tree ?
    return true;
  }
}
