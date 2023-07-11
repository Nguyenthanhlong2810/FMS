/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.persistence;

import java.util.HashSet;
import static java.util.Objects.requireNonNull;

import java.util.Random;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.drivers.vehicle.VehicleCommInfoValidator;
import org.opentcs.guing.application.StatusPanel;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists data kept in {@link SystemModel}s to the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelKernelPersistor {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ModelKernelPersistor.class);
  /**
   * The status panel for logging error messages.
   */
  private final StatusPanel statusPanel;
  /**
   * Provides new instances to validate a system model.
   */
  private final Provider<ModelValidator> validatorProvider;

//  private final Set<VehicleCommInfoValidator> commAdapterValidators;

  private final ModelExportAdapter modelExportAdapter;

  /**
   * Creates a new instance.
   *
   * @param statusPanel A status panel for logging error messages.
   * @param validatorProvider Provides validators for system models.
   * @param modelExportAdapter Converts model data on export.
   */
  @Inject
  public ModelKernelPersistor(@Nonnull StatusPanel statusPanel,
                              @Nonnull Provider<ModelValidator> validatorProvider,
                              @Nonnull ModelExportAdapter modelExportAdapter) {
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.validatorProvider = requireNonNull(validatorProvider, "validatorProvider");
//    this.commAdapterValidators = requireNonNull(commAdapterValidators, "commAdapterValidators");
    this.modelExportAdapter = requireNonNull(modelExportAdapter, "modelExportAdapter");
  }

  /**
   * Persists the given model to the given kernel.
   *
   * @param systemModel The model to be persisted.
   * @param portal The plant model service used to persist to the kernel.
   * @param ignoreValidationErrors Whether to ignore any validation errors.
   * @return {@code true} if, and only if, the model was valid or validation errors were to be
   * ignored.
   * @throws IllegalStateException If there was a problem persisting the model on the kernel side.
   */
  public boolean persist(SystemModel systemModel,
                         KernelServicePortal portal,
                         boolean ignoreValidationErrors)
      throws IllegalStateException {
    requireNonNull(systemModel, "systemModel");
    requireNonNull(portal, "plantModelService");

    LOG.debug("Validating model...");
    long timeBefore = System.currentTimeMillis();
    if (!valid(systemModel) && !ignoreValidationErrors) {
      return false;
    }
    PlantModelCreationTO to = modelExportAdapter.convert(systemModel);
//    List<VehicleCreationTO> vehicles = to.getVehicles();
//    Set<String> validatorErrors = commAdapterValidators.stream()
//            .flatMap(validator -> validator.validateVehicles(vehicles).stream())
//            .collect(Collectors.toSet());
//    if (!validatorErrors.isEmpty()) {
//      notifyValidationErrors(validatorErrors);
//      return false;
//    }
    LOG.debug("Validating took {} milliseconds.", System.currentTimeMillis() - timeBefore);

    LOG.debug("Persisting model...");
    timeBefore = System.currentTimeMillis();
    systemModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(systemModel, "id", String.valueOf(new Random().nextInt(99))));
    portal.getPlantModelService().createPlantModel(modelExportAdapter.convert(systemModel), true);
    LOG.debug("Persisting to kernel took {} milliseconds.", System.currentTimeMillis() - timeBefore);

    return true;
  }

  private boolean valid(SystemModel systemModel) {
    ModelValidator validator = validatorProvider.get();
    boolean valid = true;
    for (ModelComponent component : systemModel.getAll()) {
      valid &= validator.isValidWith(systemModel, component);
    }
    //Report possible duplicates if we persist to the kernel
    if (!valid) {
      //Use a hash set to avoid duplicate errors
      Set<String> errors = new HashSet<>(validator.getErrors());
      validator.showSavingValidationWarning(statusPanel, errors);
    }
    return valid;
  }

  private void notifyValidationErrors(Set<String> errors) {
    ModelValidator validator = validatorProvider.get();
    validator.showSavingValidationWarning(statusPanel, errors);
  }

}
