package org.opentcs.guing.application.action.report.form;


import org.opentcs.database.entity.VehicleRunLog;
import org.opentcs.database.access.ReportDataHandler;
import org.opentcs.guing.application.action.report.config.MyTableCellRender;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;

public class VehiclePanel extends JPanel {
    private final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.REPORT_PATH);
    private JTable tbl_vehicle;

    public VehiclePanel(Date datefrom, Date dateto, String state){
        setUp(datefrom, dateto, state);
        renderTable();
    }
    /**
     * Create Component and display on Jpanel
     * @param dateStart thick thighs save lives
     * @param dateEnd thick thighs save lives
     */
    public void setUp(Date dateStart, Date dateEnd, String state){
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(900,400));
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn(labels.getString("reportTable.table.number"));
        model.addColumn(labels.getString("reportTable.tablevehicleReport.vehiclename"));
        model.addColumn(labels.getString("reportTable.tablevehicleReport.status"));
        model.addColumn(labels.getString("reportTable.tablevehicleReport.datetimelog"));
        model.addColumn(labels.getString("reportTable.tablevehicleReport.energylevel"));
        model.addColumn(labels.getString("reportTable.tablevehicleReport.voltage"));
        model.addColumn(labels.getString("reportTable.tablevehicleReport.current"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
        ArrayList<VehicleRunLog> arrayList = ReportDataHandler.getInstance().getVehicleListWithState(dateStart,dateEnd,state);
        for (int i = 0; i < arrayList.size(); i++) {
            model.addRow(new Object[]{i+1, arrayList.get(i).getVehicleName(), arrayList.get(i).getStatus(), simpleDateFormat.format(arrayList.get(i).getDateTimeLog()), arrayList.get(i).getEnergyLevel(),
                arrayList.get(i).getVoltage(), arrayList.get(i).getCurrent()});
        }
        tbl_vehicle = new JTable(model);
        for(int i = 0; i<tbl_vehicle.getRowCount();i++){
            tbl_vehicle.setRowHeight(i,30);
        }
        tbl_vehicle.getColumnModel().getColumn(0).setPreferredWidth(30);
        tbl_vehicle.getColumnModel().getColumn(1).setPreferredWidth(100);
        tbl_vehicle.getColumnModel().getColumn(2).setPreferredWidth(200);
        tbl_vehicle.getColumnModel().getColumn(3).setPreferredWidth(200);
        JScrollPane scrollPane = new JScrollPane(tbl_vehicle);
        add(scrollPane);
    }

    /**
     * Set cell in table
     */
    public void renderTable() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        renderer.setForeground(new Color(31, 72, 219));
        renderer.setBackground(new Color(222, 219, 217));
        tbl_vehicle.getTableHeader().setDefaultRenderer(renderer);
        MyTableCellRender colL = new MyTableCellRender();
        colL.setHorizontalAlignment(JLabel.LEFT);
        tbl_vehicle.getColumnModel().getColumn(2).setCellRenderer(colL);
       tbl_vehicle.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
           @Override
           public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
           {
               final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
              c.setBackground(row % 2 == 0 ? new Color(196, 221, 245) : Color.WHITE);
               c.setForeground(Color.BLACK);
              setHorizontalAlignment(CENTER);
                return c;
            }
        });
    }

    /**
     * return
     * @return Result count
     */
    public int getResultTotal() {
        return tbl_vehicle.getRowCount();
    }
}
