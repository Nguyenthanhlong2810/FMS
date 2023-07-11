package org.opentcs.guing.application.action.report.form;



import org.opentcs.database.entity.TransPortOrder;
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

public class ActivitiesPanel extends JPanel {
    private final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.REPORT_PATH);
    private JTable tbl_activities ;

    public ActivitiesPanel(Date dateFrom, Date dateTo){
        intitComponents(dateFrom,dateTo);
        setTable();
    }

    /**
     * Create Component and display on Jpanel
     * @param dateFrom
     * @param dateTo
     */
    public void intitComponents(Date dateFrom, Date dateTo){
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(1200,400));
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn(labels.getString("reportTable.table.number"));
        model.addColumn(labels.getString("reportTable.tableactivityReport.toname"));
        model.addColumn(labels.getString("reportTable.tableactivityReport.createtime"));
        model.addColumn(labels.getString("reportTable.tableactivityReport.assignedtime"));
        model.addColumn(labels.getString("reportTable.tableactivityReport.vehicleprocess"));
        model.addColumn(labels.getString("reportTable.tableactivityReport.finishedtime"));
        model.addColumn(labels.getString("reportTable.tableactivityReport.success"));
        model.addColumn(labels.getString("reportTable.tableactivityReport.crossdeadLine"));
        model.addColumn(labels.getString("reportTable.tableactivityReport.distance"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
        ArrayList<TransPortOrder> arrayList = ReportDataHandler.getInstance().getActivitiesList(dateFrom,dateTo);
        for(int i = 0; i < arrayList.size();i++){
            model.addRow(new Object[]{i+1,arrayList.get(i).getToName(),simpleDateFormat.format(arrayList.get(i).getCreateTime()), arrayList.get(i).getAssignedTime() == null ? "" : simpleDateFormat.format(arrayList.get(i).getAssignedTime()),
                    arrayList.get(i).getVehicle(),arrayList.get(i).getFinishedTime()==null?"":simpleDateFormat.format(arrayList.get(i).getFinishedTime()),arrayList.get(i).isSuccess()==true?"Success":"",
                    arrayList.get(i).isCrossDeadLine() ? "Crossdealine":"", String.format("%.3f",arrayList.get(i).getDistance()/1000)});
        }
        tbl_activities = new JTable(model);
       for(int i = 0; i<tbl_activities.getRowCount();i++){
           tbl_activities.setRowHeight(i,30);
       }
        tbl_activities.getColumnModel().getColumn(0).setPreferredWidth(20);
        tbl_activities.getColumnModel().getColumn(1).setPreferredWidth(250);
        tbl_activities.getColumnModel().getColumn(2).setPreferredWidth(100);
        tbl_activities.getColumnModel().getColumn(3).setPreferredWidth(100);
        tbl_activities.getColumnModel().getColumn(4).setPreferredWidth(100);
        tbl_activities.getColumnModel().getColumn(5).setPreferredWidth(100);
        tbl_activities.getColumnModel().getColumn(6).setPreferredWidth(15);
        tbl_activities.getColumnModel().getColumn(7).setPreferredWidth(55);
        tbl_activities.setShowGrid(false);
        tbl_activities.getDefaultRenderer(model.getColumnClass(5));
        JScrollPane scrollTable = new JScrollPane(tbl_activities);
        add(scrollTable);
    }
    /**
     * Set cell in table
     */
    public void setTable() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        renderer.setForeground(new Color(31, 72, 219));
        renderer.setBackground(new Color(222, 219, 217));
        tbl_activities.getTableHeader().setDefaultRenderer(renderer);
        MyTableCellRender colR = new MyTableCellRender();
        colR.setHorizontalAlignment(JLabel.RIGHT);
        tbl_activities.getColumnModel().getColumn(8).setCellRenderer(colR);
        MyTableCellRender colL = new MyTableCellRender();
        colL.setHorizontalAlignment(JLabel.LEFT);
        tbl_activities.getColumnModel().getColumn(1).setCellRenderer(colL);
        tbl_activities.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
     * @return Result count
     */
    public int getResultTotal() {
        return tbl_activities.getRowCount();
    }

}
