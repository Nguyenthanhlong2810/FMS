package org.opentcs.guing.application.action.report.form;

import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;
import org.opentcs.database.entity.VehicleActiveLog;
import org.opentcs.database.access.ReportDataHandler;
import org.opentcs.guing.application.action.report.config.MyTableCellRender;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class VehicleActiveTimePanel extends JPanel {
    private final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.REPORT_PATH);
    private JTable tbl_vehicleActiveLog; // represent to table tbl_vehiclelog in database
    public VehicleActiveTimePanel(Date dateStart,Date dateEnd){
        setUp(dateStart,dateEnd);
        renderTable();
    }
    /**
     * Create Component and display data on Jpanel
     * @param dateStart thick thighs save lives
     * @param dateEnd thick thighs save lives
     */
    private void setUp(Date dateStart, Date dateEnd) {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(900,400));
        DefaultTableModel model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn(labels.getString("reportTable.table.number"));
        model.addColumn(labels.getString("reportTable.TableTimeActive.vehicleName"));
        model.addColumn(labels.getString("reportTable.TableTimeActive.timeStart"));
        model.addColumn(labels.getString("reportTable.TableTimeActive.timeStop"));
        model.addColumn(labels.getString("reportTable.TableTimeActive.energyStart"));
        model.addColumn(labels.getString("reportTable.TableTimeActive.energyStop"));
        model.addColumn(labels.getString("reportTable.TableTimeActive.voltage"));
        model.addColumn(labels.getString("reportTable.TableTimeActive.current"));
        model.addColumn(labels.getString("reportTable.TableTimeActive.runTime"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
        ArrayList<VehicleActiveLog> arrayList = ReportDataHandler.getInstance().getVehicleActiveLog(dateStart,dateEnd);
        for (int i = 0; i < arrayList.size(); i++) {
            model.addRow(new Object[]{i+1,
                    arrayList.get(i).getVehicleName(),
                    simpleDateFormat.format(arrayList.get(i).getStartTime()),
                    simpleDateFormat.format(arrayList.get(i).getStopTime()),
                    arrayList.get(i).getStartEnergy()+"%",
                    arrayList.get(i).getStopEnergy()+"%",
                    arrayList.get(i).getVoltage(),
                    arrayList.get(i).getCurrent(),
                    arrayList.get(i).getRunTime()});
        }
        tbl_vehicleActiveLog = new JTable(model);
//        for(int i = 0; i<tbl_vehicleActiveLog.getRowCount();i++){
//            tbl_vehicleActiveLog.setRowHeight(i,30);
//        }
        tbl_vehicleActiveLog.setRowHeight(30);
        tbl_vehicleActiveLog.getColumnModel().getColumn(0).setPreferredWidth(30);
        tbl_vehicleActiveLog.getColumnModel().getColumn(1).setPreferredWidth(100);
        tbl_vehicleActiveLog.getColumnModel().getColumn(2).setPreferredWidth(180);
        tbl_vehicleActiveLog.getColumnModel().getColumn(3).setPreferredWidth(180);
        tbl_vehicleActiveLog.getColumnModel().getColumn(6).setPreferredWidth(180);
        JScrollPane scrollPane = new JScrollPane(tbl_vehicleActiveLog);
        add(scrollPane);
    }

    /**
     * render table
     */
    private void renderTable() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        renderer.setForeground(new Color(31, 72, 219));
        renderer.setBackground(new Color(222, 219, 217));
        tbl_vehicleActiveLog.getTableHeader().setDefaultRenderer(renderer);
        MyTableCellRender colL = new MyTableCellRender();
        colL.setHorizontalAlignment(JLabel.CENTER);
        tbl_vehicleActiveLog.getColumnModel().getColumn(2).setCellRenderer(colL);
        tbl_vehicleActiveLog.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
        TableFilterHeader filterHeader = new TableFilterHeader(tbl_vehicleActiveLog, AutoChoices.ENABLED);
        filterHeader.setBackground(Color.ORANGE);
    }

    /**
     * return
     * @return Result count
     */
    public int getResultTotal() {return tbl_vehicleActiveLog.getRowCount();}
}
