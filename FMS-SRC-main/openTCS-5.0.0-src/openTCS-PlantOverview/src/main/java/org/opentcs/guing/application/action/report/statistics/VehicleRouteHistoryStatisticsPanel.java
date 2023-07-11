package org.opentcs.guing.application.action.report.statistics;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.aubot.hibernate.database.DatabaseSessionFactory;
import com.toedter.calendar.JDateChooser;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;
import org.hibernate.Session;
import org.opentcs.guing.application.action.report.statistics.export.ExportReport;
import org.opentcs.guing.util.SynchronizedFileChooser;
import org.opentcs.hibernate.HibernateConfiguration;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.STATISTICS_PATH;
/**
 *
 * @author nguye
 */
public class VehicleRouteHistoryStatisticsPanel
    extends UIPluggablePanel {

  private boolean initialized;

  private final HibernateConfiguration configuration;

  private DatabaseSessionFactory sessionFactory;

  private DefaultTableModel vehicleRouteHistoryStatisticsModel = new DefaultTableModel();

  private DefaultTableModel statisticByDateModel = new DefaultTableModel();

  private JButton btnStatistics = new JButton(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.statisticButton"));

  private  JButton btnExportReport = new JButton(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.ExportButton"));

  private JDateChooser txtDateFrom = new JDateChooser();

  private JDateChooser txtDateTo = new JDateChooser();

  private final Color HEADER_TABLE_COLOR = new Color(242, 112, 24);

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(STATISTICS_PATH);

  /**
   * Creates new form VehicleHistoryStatisticsPanel
   */

  @Inject
  public VehicleRouteHistoryStatisticsPanel(HibernateConfiguration configuration,
                                            DatabaseSessionFactory sessionFactory) {
    this.configuration = requireNonNull(configuration, "StatisticsCollectorConfiguration");
    this.sessionFactory = requireNonNull(sessionFactory, "sessionFactory");
    initComponents();
    initComponentsExtra();
  }

  @Override
  public void enableUI(boolean enable) {
    btnStatistics.setEnabled(enable);
    btnExportReport.setEnabled(enable);
  }

  private void initComponentsExtra() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    txtDateFrom.setDateFormatString(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.formatDate"));
    txtDateFrom.setDate(cal.getTime());
    txtDateTo.setDateFormatString(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.formatDate"));
    cal.add(Calendar.MONTH, 1);
    txtDateTo.setDate(cal.getTime());
    pnButtons.add(txtDateFrom);
    pnButtons.add(txtDateTo);
    pnButtons.add(btnStatistics);
    pnExport.add(btnExportReport);
    btnStatistics.addActionListener(e->{
      fillDataTableDetail(getDataFromDatabase());
      fillDataTableDetailByDate(getDataFromDatabaseByDate());
    });
    btnExportReport.addActionListener(e->{
      showSaveFileChooser();
    });
    btnDetail.setVisible(false);
    btnDetail.addActionListener(e->{
      CardLayout cardLayout = (CardLayout) pnReport.getLayout();
      cardLayout.next(pnReport);
      btnDetail.setVisible(false);
      btnStatisticByDate.setVisible(true);
    });
    btnStatisticByDate.addActionListener(e->{
      CardLayout cardLayout = (CardLayout) pnReport.getLayout();
      cardLayout.previous(pnReport);
      btnStatisticByDate.setVisible(false);
      btnDetail.setVisible(true);
    });
    createPanelDetailContent();
    decorateTable(tblDetails, SwingConstants.CENTER);
    decorateTable(tbStatisticByDate, SwingConstants.CENTER);
    btnExportReport.setIcon(new ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/report_excel_24px.png")));
    tbStatisticByDate.setModel(statisticByDateModel);
    new TableFilterHeader(tblDetails, AutoChoices.ENABLED);
    //new TableFilterHeader(tbStatisticByDate, AutoChoices.ENABLED);
    tblDetails.setEnabled(false);
    tbStatisticByDate.setEnabled(false);
  }

  private void showSaveFileChooser(){
    SimpleDateFormat dateFormat = new SimpleDateFormat(BUNDLE.getString("statisticPanel.formatDate"));
    String dateFromStr = dateFormat.format(txtDateFrom.getDate());
    String dateToStr = dateFormat.format(txtDateTo.getDate());
    JFileChooser fileChooser = new SynchronizedFileChooser(null);
    fileChooser.setFileFilter(new FileNameExtensionFilter("Microsoft Excel", "xlsx"));
    if(pnDetails.isVisible()){
      String fileName = BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.sheet") +
          " " + dateFromStr + " - " + dateToStr;
      fileChooser.setSelectedFile(new File(fileName));
      int returnVal = fileChooser.showSaveDialog(this);
      if(returnVal == JFileChooser.APPROVE_OPTION){
        File file = fileChooser.getSelectedFile();
        ExportReport exportReport = new ExportReport(tblDetails, vehicleRouteHistoryStatisticsModel, dateFromStr, dateToStr,
            BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.titleTable"));
        exportReport.exportExcel(file.getPath(), BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.sheet"));
      }
    }else if(pnStatisticByDate.isVisible()){
      String fileName = BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.sheetByDate") +
          " " + dateFromStr + " - " + dateToStr;
      fileChooser.setSelectedFile(new File(fileName));
      int returnVal = fileChooser.showSaveDialog(this);
      if(returnVal == JFileChooser.APPROVE_OPTION){
        File file = fileChooser.getSelectedFile();
        ExportReport exportReport = new ExportReport(tbStatisticByDate, statisticByDateModel, dateFromStr, dateToStr,
            BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.titleTableByDate"));
        exportReport.exportExcel(file.getPath(), BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.sheet"));
      }
    }
  }

  public void decorateTable(JTable table, int alignment) {
    table.getTableHeader().setForeground(HEADER_TABLE_COLOR);
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(alignment);
    table.setDefaultRenderer(Object.class, renderer);
    table.setShowHorizontalLines(true);
    table.setShowVerticalLines(true);
  }

  private void createPanelDetailContent(){
    vehicleRouteHistoryStatisticsModel.addColumn(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.columnHeaderVehicleName"));
    vehicleRouteHistoryStatisticsModel.addColumn(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.columnHeaderCompleteAt"));
    vehicleRouteHistoryStatisticsModel.addColumn(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.columnHeaderRouteName"));
    vehicleRouteHistoryStatisticsModel.addColumn(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.columnHeaderRounds"));
    tblDetails.setModel(vehicleRouteHistoryStatisticsModel);
  }

  private List<Object[]> getDataFromDatabase(){
    List<Object[]> list ;
    Session session = sessionFactory.getSession();
    session.beginTransaction();

    String dateFrom = new Timestamp(txtDateFrom.getDate().getTime()).toString();
    String dateTo = new Timestamp(txtDateTo.getDate().getTime() + 86400 * 1000 - 1000).toString();
    String formatDate = BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.formatDate");
    String hql = "SELECT vrh.vehicle.name, to_char(vrh.completeAt, '" + formatDate + "'), vrh.route.name, count(vrh.id) " +
                 "FROM VehicleRouteHistoryEntity vrh " +
                 "WHERE vrh.completeAt >= '" + dateFrom + "' and vrh.completeAt <= '" + dateTo + "' " +
                 "GROUP BY vrh.vehicle.name, to_char(vrh.completeAt, '" + formatDate + "'), vrh.route.name";
    list = session.createQuery(hql).list();

    session.getTransaction().commit();
    return list;
  }

  private void fillDataTableDetail(List<Object[]> list) {
    vehicleRouteHistoryStatisticsModel.setRowCount(0);
    for (Object[] ob : list) {
      vehicleRouteHistoryStatisticsModel.addRow(new Object[]{
          ob[0],
          ob[1],
          //vrhe.getBattery(),
          ob[2],
          ob[3]
      });
    }
  }

  private List<Object[]> getDataFromDatabaseByDate(){
    List<Object[]> list ;
    Session session = sessionFactory.getSession();
    session.beginTransaction();

    String dateFrom = new Timestamp(txtDateFrom.getDate().getTime()).toString();
    String dateTo = new Timestamp(txtDateTo.getDate().getTime() + 86400 * 1000 - 1000).toString();
    String formatDate = BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.formatDate");
    String hql = "SELECT vrh.vehicle.name, to_char(vrh.completeAt, '" + formatDate + "'), count(vrh.id) " +
        "FROM VehicleRouteHistoryEntity vrh " +
        "WHERE vrh.completeAt >= '" + dateFrom + "' and vrh.completeAt <= '" + dateTo + "' " +
        "GROUP BY vrh.vehicle.name, to_char(vrh.completeAt, '" + formatDate + "') " +
        "ORDER BY to_char(vrh.completeAt, '" + formatDate + "') asc";
    list = session.createQuery(hql).list();
    session.getTransaction().commit();
    return list;
  }

  private void fillDataTableDetailByDate(List<Object[]> list) {
    Map<Map.Entry<String, String>, Integer> vrheMap = new HashMap<>();
    list.forEach(ob -> {
      Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(ob[0].toString(), ob[1].toString());
      if (vrheMap.containsKey(entry)) {
        vrheMap.put(entry, vrheMap.get(entry) + ((Integer) ob[2]));
      }else{
        vrheMap.put(entry, ((Number) ob[2]).intValue());
      }
    });

    Set<String> vehicleSet = vrheMap.keySet().stream().map(Map.Entry::getKey).collect(Collectors.toCollection(TreeSet::new));
    Set<String> dateSet = new LinkedHashSet<>();
    Calendar cal = Calendar.getInstance();
    cal.setTime(txtDateFrom.getDate());
    DateFormat dateFormat = new SimpleDateFormat(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.formatDate"));
    do {
      dateSet.add(dateFormat.format(cal.getTime()));
      cal.add(Calendar.DATE, 1);
    } while ((cal.getTime().getTime()) <= (txtDateTo.getDate().getTime()));

    statisticByDateModel.setRowCount(0);
    statisticByDateModel.setColumnCount(0);
    statisticByDateModel.addColumn(BUNDLE.getString("vehicleRouteHistoryStatisticsPanel.columnHeaderVehicleName"));
    dateSet.forEach(statisticByDateModel::addColumn);
    for(String vehicle : vehicleSet){
      Object[] data = new Object[dateSet.size() + 1];
      int index = 0;
      data[index++] = vehicle;
      for (String date : dateSet) {
        Object value = vrheMap.get(new AbstractMap.SimpleEntry<>(vehicle, date));
        data[index++] = value == null ? 0 : value;
      }
      statisticByDateModel.addRow(data);
    }
    statisticByDateModel.fireTableDataChanged();
    for(int i = 0; i < tbStatisticByDate.getColumnCount(); i++) {
      tbStatisticByDate.getColumnModel().getColumn(i).setMinWidth(100);
    }
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

    pnTitle = new javax.swing.JPanel();
    pnButtons = new javax.swing.JPanel();
    pnReport = new javax.swing.JPanel();
    pnDetails = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    tblDetails = new javax.swing.JTable();
    pnStatisticByDate = new javax.swing.JPanel();
    jScrollPane3 = new javax.swing.JScrollPane();
    tbStatisticByDate = new javax.swing.JTable();
    pnExport = new javax.swing.JPanel();
    btnDetail = new javax.swing.JButton();
    btnStatisticByDate = new javax.swing.JButton();

    setLayout(new java.awt.BorderLayout());

    pnTitle.setPreferredSize(new java.awt.Dimension(10, 50));
    pnTitle.setLayout(new java.awt.BorderLayout());
    pnTitle.add(pnButtons, java.awt.BorderLayout.PAGE_START);

    add(pnTitle, java.awt.BorderLayout.PAGE_START);

    pnReport.setLayout(new java.awt.CardLayout());

    pnDetails.setLayout(new java.awt.BorderLayout());

    tblDetails.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    jScrollPane1.setViewportView(tblDetails);

    pnDetails.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    pnReport.add(pnDetails, "card4");

    pnStatisticByDate.setLayout(new java.awt.BorderLayout());

    jScrollPane3.setToolTipText("");

    tbStatisticByDate.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    tbStatisticByDate.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    jScrollPane3.setViewportView(tbStatisticByDate);

    pnStatisticByDate.add(jScrollPane3, java.awt.BorderLayout.CENTER);

    pnReport.add(pnStatisticByDate, "card4");

    add(pnReport, java.awt.BorderLayout.CENTER);

    pnExport.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    btnDetail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/back_report_24px.png"))); // NOI18N
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/dialogs/statisticsPanel"); // NOI18N
    btnDetail.setText(bundle.getString("statisticPanel.btnToByRoute")); // NOI18N
    pnExport.add(btnDetail);

    btnStatisticByDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/forward_report_24px.png"))); // NOI18N
    btnStatisticByDate.setText(bundle.getString("statisticPanel.btnToByDate")); // NOI18N
    btnStatisticByDate.setActionCommand(bundle.getString("statisticPanel.btnToByDate")); // NOI18N
    pnExport.add(btnStatisticByDate);

    add(pnExport, java.awt.BorderLayout.PAGE_END);
  }// </editor-fold>//GEN-END:initComponents

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


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnDetail;
  private javax.swing.JButton btnStatisticByDate;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JPanel pnButtons;
  private javax.swing.JPanel pnDetails;
  private javax.swing.JPanel pnExport;
  private javax.swing.JPanel pnReport;
  private javax.swing.JPanel pnStatisticByDate;
  private javax.swing.JPanel pnTitle;
  private javax.swing.JTable tbStatisticByDate;
  private javax.swing.JTable tblDetails;
  // End of variables declaration//GEN-END:variables
}
