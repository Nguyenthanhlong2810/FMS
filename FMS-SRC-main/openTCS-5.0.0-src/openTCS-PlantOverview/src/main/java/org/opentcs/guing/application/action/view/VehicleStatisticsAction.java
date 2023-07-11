package org.opentcs.guing.application.action.view;

import com.aubot.hibernate.database.DatabaseSessionFactory;
import org.opentcs.guing.application.action.report.form.VehicleStatistics;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.UserMessageHelper;
import org.opentcs.hibernate.HibernateConfiguration;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;

public class VehicleStatisticsAction extends AbstractAction {

  private final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.STATISTICS_PATH);

  public final static String ID = "actions.openVehicleStatistics";

  private final HibernateConfiguration configuration;

  private final UserMessageHelper userMessageHelper;

  private final DatabaseSessionFactory sessionFactory;

  @Inject
  public VehicleStatisticsAction(HibernateConfiguration configuration,
                                 UserMessageHelper userMessageHelper,
                                 DatabaseSessionFactory sessionFactory) {
    this.configuration = configuration;
    this.userMessageHelper = userMessageHelper;
    this.sessionFactory = sessionFactory;

    putValue(NAME, bundle.getString("statisticPanel.title"));
  }


  @Override
  public void actionPerformed(ActionEvent e) {
    new VehicleStatistics(configuration,userMessageHelper, sessionFactory).setVisible(true);
  }
}
