package org.opentcs.guing.application.action.report.form;



import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.IFilterEditor;
import net.coderazzi.filters.gui.TableFilterHeader;
import org.opentcs.database.entity.ErrorLog;
import org.opentcs.database.access.ReportDataHandler;
import org.opentcs.guing.application.action.report.config.MyTableCellRender;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.sql.Date;

public class ErrorPanel extends JPanel {
    ReportDataHandler dataHandler =  ReportDataHandler.getInstance();
    private final JTable tbl_Error =new JTable();

    public ErrorPanel(Date dateFrom, Date dateTo){
        intitComponents(dateFrom,dateTo);
    }

    /**
     * Create Component and display on Jpanel
     * @param dateFrom
     * @param dateTo
     */
    public void intitComponents(Date dateFrom, Date dateTo){
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.REPORT_PATH);
        String[] colNames = {
                labels.getString("reportTable.tableError.colIdLabel"),
                labels.getString("reportTable.tableError.colErrorVehicleLabel"),
                labels.getString("reportTable.tableError.colErrorCodeLabel"),
                labels.getString("reportTable.tableError.colErrorMessageLabel"),
                labels.getString("reportTable.tableError.colDateTimeLogLabel")
        };
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(1200,400));
        DefaultTableModel model = new DefaultTableModel(colNames,0) {

        };

        ArrayList<ErrorLog> arrayList = dataHandler.getErrorLogList(dateFrom,dateTo);
        for(int i = 0; i < arrayList.size();i++){
            model.addRow(new Object[]{i+1,
                    arrayList.get(i).getErrorVehicle(),
                    arrayList.get(i).getErrorCode(),
                    arrayList.get(i).getErrorMessage(),
                    arrayList.get(i).getDatetimelog()});
        }
        tbl_Error.setModel(model);
        configTable(tbl_Error);
        JScrollPane scrollPane = new JScrollPane(tbl_Error);
        add(scrollPane);
    }

    /**
     * Config Table
     * @param table The table will be ...
     */
    public void configTable(JTable table) {
        /*
         * Render tableCells
         */
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        renderer.setForeground(new Color(31, 72, 219));
        renderer.setBackground(new Color(222, 219, 217));

        table.getTableHeader().setDefaultRenderer(renderer);
        table.getTableHeader().setEnabled(true);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? new Color(196, 221, 245) : Color.WHITE);
                c.setForeground(Color.black);
                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        table.setEnabled(false);
        /*
            SetRowHeight
         */
        MyTableCellRender cellRenderLeft = new MyTableCellRender();
        cellRenderLeft.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setCellRenderer(cellRenderLeft);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setCellRenderer(cellRenderLeft);
        /*
            Set Filter For The Table
         */
        TableFilterHeader filterHeader = new TableFilterHeader(table, AutoChoices.ENABLED);
        //filterHeader.setPosition(TableFilterHeader.Position.TOP);
        filterHeader.setBackground(Color.ORANGE);
        IFilterEditor filterEditor = filterHeader.getFilterEditor(1);
        filterEditor = filterHeader.getFilterEditor(2);
        filterEditor = filterHeader.getFilterEditor(3);
        for(int i = 0; i< table.getRowCount(); i++){table.setRowHeight(i,35);}
    }

    /**
     * return Result count
     * @return
     */
    public int getResultTotal() {
        return tbl_Error.getRowCount();
    }

}
