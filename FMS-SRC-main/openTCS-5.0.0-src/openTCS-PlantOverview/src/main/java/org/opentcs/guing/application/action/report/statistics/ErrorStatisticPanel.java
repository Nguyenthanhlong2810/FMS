package org.opentcs.guing.application.action.report.statistics;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.toedter.calendar.JDateChooser;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;
import org.hibernate.Session;
import org.hibernate.query.Query;
import com.aubot.hibernate.database.DatabaseSessionFactory;
import org.opentcs.guing.application.action.report.statistics.export.ExportReport;
import org.opentcs.guing.util.SynchronizedFileChooser;
import org.opentcs.hibernate.HibernateConfiguration;
import org.opentcs.hibernate.entities.VehicleHistoryEntity;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.STATISTICS_PATH;

/**
 *
 * @author nguye
 */
public class ErrorStatisticPanel
    extends UIPluggablePanel {

  private boolean initialized;

  private final HibernateConfiguration configuration;

  private JDateChooser txtDateFrom = new JDateChooser();

  private JDateChooser txtDateTo = new JDateChooser();

  private JButton btnStatistic = new JButton(BUNDLE.getString("statisticPanel.statisticButton"));

  private JButton btnExportExcel = new JButton((BUNDLE.getString("statisticPanel.ExportButton")));

  private DatabaseSessionFactory sessionFactory;

  private DefaultTableModel vehicleErrorDetailModel = new DefaultTableModel();

  private DefaultTableModel vehicleErrorStatsModel = new DefaultTableModel();

  private final Color HEADER_TABLE_COLOR = new Color(242, 112, 24);

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(STATISTICS_PATH);
  /**
   * Creates new form StatisticPanel
   */
  @Inject
  public ErrorStatisticPanel(HibernateConfiguration configuration,DatabaseSessionFactory sessionFactory) {
    this.configuration = requireNonNull(configuration, "StatisticsCollectorConfiguration");
    this.sessionFactory = requireNonNull(sessionFactory, "sessionFactory");
    initComponents();
    initComponentsExtra();
  }

  private void initComponentsExtra() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    txtDateFrom.setDateFormatString(BUNDLE.getString("statisticPanel.formatDate"));
    txtDateFrom.setDate(cal.getTime());
    txtDateTo.setDateFormatString(BUNDLE.getString("statisticPanel.formatDate"));
    cal.add(Calendar.MONTH, 1);
    txtDateTo.setDate(cal.getTime());
    pnlConditions.add(txtDateFrom);
    pnlConditions.add(txtDateTo);
    pnlConditions.add(btnStatistic);
    pnlExport.add(btnExportExcel);
    decorateTable(tblDetail,SwingConstants.CENTER);
    decorateTable(tblStats,SwingConstants.CENTER);
    createPanelDetailContent();
    btnStatistic.addActionListener(e -> {
      List<VehicleHistoryEntity> list = getDataFromDatabase();
      fillDataTableDetail(list);
      fillDataTableStats(list);
    });
    btnExportExcel.addActionListener(e->{
      showOpenFileChooser();
    });
    btnToStats.setVisible(false);
    btnToDetail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btnToDetail.addActionListener(e -> {
        CardLayout card = (CardLayout) pnlContent.getLayout();
        card.next(pnlContent);
        btnToDetail.setVisible(false);
        btnToStats.setVisible(true);
    });
    btnToStats.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btnToStats.addActionListener(e -> {
        CardLayout card = (CardLayout) pnlContent.getLayout();
        card.previous(pnlContent);
        btnToDetail.setVisible(true);
        btnToStats.setVisible(false);
    });
    btnExportExcel.setIcon(new ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/report_excel_24px.png")));
    TableFilterHeader detailTableFilter = new TableFilterHeader(tblDetail, AutoChoices.ENABLED);
//    TableFilterHeader statsTableFilter = new TableFilterHeader(tblStats, AutoChoices.ENABLED);
    tblDetail.setEnabled(false);
    tblStats.setEnabled(false);
  }

  private List<VehicleHistoryEntity> getDataFromDatabase(){
    List<VehicleHistoryEntity> list;
    Session session = sessionFactory.getSession();
    session.beginTransaction();

    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<VehicleHistoryEntity> criteria = builder.createQuery(VehicleHistoryEntity.class);
    Root<VehicleHistoryEntity> root = criteria.from(VehicleHistoryEntity.class);
    Predicate statePredicate = builder.like(root.get("state"), "ERROR");
    Predicate datePredicate = builder.between(root.get("timeLog"),
                                  new Timestamp(txtDateFrom.getDate().getTime()),
                                  new Timestamp(txtDateTo.getDate().getTime() + 86400 * 1000 - 1000));
    Predicate predicate = builder.and(statePredicate,datePredicate);
    criteria.select(root).where(predicate);
    Query<VehicleHistoryEntity> query = session.createQuery(criteria);
    list = query.list();
    session.getTransaction().commit();
    return list;
  }

  private void fillDataTableDetail(List<VehicleHistoryEntity> list) {
    vehicleErrorDetailModel.setRowCount(0);
    for (VehicleHistoryEntity vhe : list) {
      vehicleErrorDetailModel.addRow(new Object[]{
          vhe.getVehicle().getName(),
          vhe.getNote(),
          vhe.getTimeLog(),
          vhe.getPosition(),
          vhe.getBattery(),
          vhe.getVoltage(),
          vhe.getCurrent()
      });
    }
  }

  private void fillDataTableStats(List<VehicleHistoryEntity> list) {
    Map<Map.Entry<String, String>, Integer> vehicleErrors = new HashMap<>();
    list.forEach(vhe -> {
      Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(vhe.getVehicle().getName(), vhe.getNote());
      if (vehicleErrors.containsKey(entry)) {
        vehicleErrors.put(entry, vehicleErrors.get(entry) + 1);
      } else {
        vehicleErrors.put(entry, 1);
      }
    });

    Set<String> vehicleSet = vehicleErrors.keySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
    Set<String> errorSet = vehicleErrors.keySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet());
    vehicleErrorStatsModel.setColumnCount(0);
    vehicleErrorStatsModel.setRowCount(0);
    vehicleErrorStatsModel.addColumn(BUNDLE.getString("statisticPanel.columnHeaderVehicleErrorStats"));
    errorSet.forEach(vehicleErrorStatsModel::addColumn);
    vehicleSet.forEach(vehicle -> {
      Object[] data = new Object[errorSet.size() + 1];
      int index = 0;
      data[index++] = vehicle;
      for (String error : errorSet) {
        data[index++] = vehicleErrors.get(new AbstractMap.SimpleEntry<>(vehicle, error));
      }
      vehicleErrorStatsModel.addRow(data);
    });
    vehicleErrorStatsModel.fireTableDataChanged();
  }


  private void createPanelDetailContent(){
    vehicleErrorDetailModel.addColumn(BUNDLE.getString("statisticPanel.columnHeaderVehicleErrorDetail"));
    vehicleErrorDetailModel.addColumn(BUNDLE.getString("statisticPanel.columnHeaderErrorDetail"));
    vehicleErrorDetailModel.addColumn(BUNDLE.getString("statisticPanel.columnHeaderTimeLog"));
    vehicleErrorDetailModel.addColumn(BUNDLE.getString("statisticPanel.columnHeaderPosition"));
    vehicleErrorDetailModel.addColumn(BUNDLE.getString("statisticPanel.columnHeaderBattery"));
    vehicleErrorDetailModel.addColumn(BUNDLE.getString("statisticPanel.columnHeaderVoltage"));
    vehicleErrorDetailModel.addColumn(BUNDLE.getString("statisticPanel.columnHeaderCurrent"));
    tblDetail.setModel(vehicleErrorDetailModel);
    tblStats.setModel(vehicleErrorStatsModel);
  }

//  private SessionFactory createSessionFactory() {
//    Configuration sessionConfig = new Configuration();
//    sessionConfig.configure();
//    String connectionUrl = String.format("jdbc:postgresql://%s:%s/%s",
//        configuration.hostname(),
//        configuration.port(),
//        configuration.datasource());
//    sessionConfig.setProperty("hibernate.connection.url", connectionUrl);
//    sessionConfig.setProperty("hibernate.connection.username", configuration.username());
//    sessionConfig.setProperty("hibernate.connection.password", configuration.password());
//
//    return sessionConfig.buildSessionFactory();
//  }

  private void showOpenFileChooser(){
    SimpleDateFormat dateFormat = new SimpleDateFormat(BUNDLE.getString("statisticPanel.formatDate"));
    String dateFromStr = dateFormat.format(txtDateFrom.getDate());
    String dateToStr = dateFormat.format(txtDateTo.getDate());
    JFileChooser fileChooser = new SynchronizedFileChooser(null);
    fileChooser.setFileFilter(new FileNameExtensionFilter("Microsoft Excel", "xlsx"));
    String fileName = BUNDLE.getString("statisticPanel.vehicleErrorStatsSheet") +
            " " + dateFromStr + " - " + dateToStr;
    fileChooser.setSelectedFile(new File(fileName));
    int returnVal = fileChooser.showSaveDialog(this);
    if(returnVal == JFileChooser.APPROVE_OPTION){
      File file = fileChooser.getSelectedFile();
      ExportReport exportReport = null;
      if(pnlDetail.isVisible()){
        exportReport = new ExportReport(tblDetail, vehicleErrorDetailModel, dateFromStr, dateToStr,
                BUNDLE.getString("statisticPanel.vehicleErrorDetailTabelTitle"));
        exportReport.exportExcel(file.getPath(), BUNDLE.getString("statisticPanel.vehicleErrorDetailSheet"));
      }else if(pnlStats.isVisible()){
        exportReport = new ExportReport(tblStats,vehicleErrorStatsModel, dateFromStr, dateToStr,
                BUNDLE.getString("statisticPanel.vehicleErrorStatsTabelTitle"));
        exportReport.exportExcel(file.getPath(), BUNDLE.getString("statisticPanel.vehicleErrorStatsSheet"));
      }
    }
  }

  public void decorateTable(JTable table, int alignment) {
    DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
    renderer.setHorizontalAlignment(0);
    table.getTableHeader().setForeground(HEADER_TABLE_COLOR);
    renderer.setBackground(Color.RED);
    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
    rightRenderer.setHorizontalAlignment(alignment);

    TableModel tableModel = table.getModel();
    for (int columnIndex = 0; columnIndex < tableModel.getColumnCount(); columnIndex++) {
      table.getColumnModel().getColumn(columnIndex).setCellRenderer(rightRenderer);
    }
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
      return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    initialized = false;
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    pnlConditions = new javax.swing.JPanel();
    pnlExport = new javax.swing.JPanel();
    btnToStats = new javax.swing.JButton();
    btnToDetail = new javax.swing.JButton();
    pnlContent = new javax.swing.JPanel();
    pnlStats = new javax.swing.JPanel();
    pnlStatsContent = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    tblStats = new javax.swing.JTable();
    pnlDetail = new javax.swing.JPanel();
    pnlDetailContent = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane();
    tblDetail = new javax.swing.JTable();

    setLayout(new java.awt.BorderLayout());

    pnlConditions.setMinimumSize(new java.awt.Dimension(100, 50));
    pnlConditions.setPreferredSize(new java.awt.Dimension(767, 50));
    add(pnlConditions, java.awt.BorderLayout.PAGE_START);

    pnlExport.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    btnToStats.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/back_report_24px.png"))); // NOI18N
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/dialogs/statisticsPanel"); // NOI18N
    btnToStats.setText(bundle.getString("statisticPanel.btnToStats")); // NOI18N
    pnlExport.add(btnToStats);

    btnToDetail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/forward_report_24px.png"))); // NOI18N
    btnToDetail.setText(bundle.getString("statisticPanel.btnToDetail")); // NOI18N
    btnToDetail.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    pnlExport.add(btnToDetail);

    add(pnlExport, java.awt.BorderLayout.PAGE_END);

    pnlContent.setLayout(new java.awt.CardLayout());

    pnlStats.setLayout(new java.awt.BorderLayout());

    pnlStatsContent.setLayout(new java.awt.BorderLayout());

    tblStats.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    jScrollPane1.setViewportView(tblStats);

    pnlStatsContent.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    pnlStats.add(pnlStatsContent, java.awt.BorderLayout.CENTER);

    pnlContent.add(pnlStats, "stats");

    pnlDetail.setLayout(new java.awt.BorderLayout());

    pnlDetailContent.setLayout(new java.awt.BorderLayout());

    tblDetail.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    tblDetail.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
    jScrollPane2.setViewportView(tblDetail);

    pnlDetailContent.add(jScrollPane2, java.awt.BorderLayout.CENTER);

    pnlDetail.add(pnlDetailContent, java.awt.BorderLayout.CENTER);

    pnlContent.add(pnlDetail, "detail");

    add(pnlContent, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnToDetail;
  private javax.swing.JButton btnToStats;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JPanel pnlConditions;
  private javax.swing.JPanel pnlContent;
  private javax.swing.JPanel pnlDetail;
  private javax.swing.JPanel pnlDetailContent;
  private javax.swing.JPanel pnlExport;
  private javax.swing.JPanel pnlStats;
  private javax.swing.JPanel pnlStatsContent;
  private javax.swing.JTable tblDetail;
  private javax.swing.JTable tblStats;
  // End of variables declaration//GEN-END:variables

  @Override
  public void enableUI(boolean enable) {
    btnStatistic.setEnabled(enable);
    btnExportExcel.setEnabled(enable);
  }
}
