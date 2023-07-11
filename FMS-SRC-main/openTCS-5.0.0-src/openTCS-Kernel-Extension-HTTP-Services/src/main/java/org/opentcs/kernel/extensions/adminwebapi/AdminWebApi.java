/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.adminwebapi;

import com.aubot.hibernate.entities.UserEntity;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.kernel.extensions.adminwebapi.v1.V1RequestHandler;
import org.opentcs.kernel.extensions.controllers.UserController;
import org.opentcs.kernel.extensions.servicewebapi.HttpConstants;
import spark.HaltException;
import spark.Service;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

/**
 * Provides an HTTP interface for basic administration needs.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AdminWebApi
    implements KernelExtension {

  /**
   * The interface configuration.
   */
  private final AdminWebApiConfiguration configuration;
  /**
   * Handles requests for API version 1.
   */
  private final V1RequestHandler v1RequestHandler;

  private final UserController userController;
  /**
   * The actual HTTP service.
   */
  private Service service;
  /**
   * Whether this kernel extension is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param configuration The interface configuration.
   * @param v1RequestHandler Handles requests for API version 1.
   */
  @Inject
  public AdminWebApi(AdminWebApiConfiguration configuration,
                     V1RequestHandler v1RequestHandler,
                     UserController userController) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.v1RequestHandler = requireNonNull(v1RequestHandler, "v1RequestHandler");
    this.userController = requireNonNull(userController, "userController");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    service = Service.ignite()
        .ipAddress(configuration.bindAddress())
        .port(configuration.bindPort());

    service.get("/", userController::login, new ThymeleafTemplateEngine());
    service.get("/login", userController::login, new ThymeleafTemplateEngine());
    service.get("/logout", userController::logout);
    service.post("/handle-login", userController::handleLogin,new ThymeleafTemplateEngine());

    service.path("/v1", () -> {
                service.get("/version", v1RequestHandler::handleGetVersion);
                service.get("/status", v1RequestHandler::handleGetStatus);
                service.delete("/kernel", v1RequestHandler::handleDeleteKernel);
            }
    );

    service.before("/user/*", ((request, response) -> {
          if (request.session().attribute("account") == null) {
              request.session().attribute("lastUrl", request.pathInfo());
              response.redirect("/login");
          }
          UserEntity user = request.session().attribute("account");
          if(user != null && !user.isAdmin()){
              request.session().removeAttribute("account");
              service.halt(500,"Permission is denied!");
          }
    }));
    service.path("/user", () -> {
        service.get("/user-list", userController::getUsers, new ThymeleafTemplateEngine());

        service.get("/user-add", userController::addUser, new ThymeleafTemplateEngine());
        service.post("/handle-add-user", userController::handleAddUser, new ThymeleafTemplateEngine());

        service.get("/user-modify", userController::modifyUser, new ThymeleafTemplateEngine());
        service.post("/handle-edit-user", userController::handleEditUser,new ThymeleafTemplateEngine());
        service.get("/handle-remove-user", userController::handleRemoveUser);
    });

    service.exception(IllegalArgumentException.class, (exception, request, response) -> {
                    response.status(400);
                    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    response.body(exception.getMessage());
                  });
    service.exception(IllegalStateException.class, (exception, request, response) -> {
                    response.status(500);
                    response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
                    response.body(exception.getMessage());
                  });
    service.exception(HaltException.class, (exception, request, response) -> {
        response.status(101);
        response.type(HttpConstants.CONTENT_TYPE_TEXT_PLAIN_UTF8);
        response.body(exception.getMessage());
    });

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    service.stop();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

}
