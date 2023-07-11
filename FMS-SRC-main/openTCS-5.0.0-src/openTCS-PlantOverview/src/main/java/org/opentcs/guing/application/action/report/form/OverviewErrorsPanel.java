package org.opentcs.guing.application.action.report.form;

import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;
import org.opentcs.database.entity.errorchart.VehicleErrorCount;
import org.opentcs.database.access.ReportDataHandler;
import org.opentcs.guing.application.action.report.config.MyTableCellRender;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;

public class OverviewErrorsPanel extends JPanel {
    ReportDataHandler dataHandler = ReportDataHandler.getInstance();
    private final JTable tbl_OverviewError = new JTable();
    public OverviewErrorsPanel(Date dateStart, Date dateEnd){
        setup(dateStart,dateEnd);
    }


    private void setup(Date dateStart, Date dateEnd) {
        ArrayList<String> errorNamesList = dataHandler.getErrorMessageList(dateStart,dateEnd);
            String[] colNames = {"Number","Vehicle Name"};
            for (String errorName: errorNamesList) {
                colNames = addElement(colNames,errorName);
            }
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(1200,400));
        DefaultTableModel model = new DefaultTableModel(colNames,0)
        {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
            ArrayList<VehicleErrorCount> list = dataHandler.getErrorCountList(dateStart,dateEnd);
            for (int i = 0 ; i<list.size();i++){
                Object[] row = new Object[]{};
                row = addElement(row,i+1);
                row = addElement(row,list.get(i).getVehicleName());
                for (int j=0;j< Arrays.stream(list.get(i).getCount()).count();j++)
                {
                    row = addElement(row,list.get(i).getCount()[j]);
                }
                model.addRow(row);
            }
        tbl_OverviewError.setModel(model);
        configTable(tbl_OverviewError);
        JScrollPane scrollPane = new JScrollPane(tbl_OverviewError);
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
        renderer.setBackground(Color.white);

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
        for (int i =0 ; i< table.getColumnCount();i++){
            if (i==0){
                table.getColumnModel().getColumn(0).setPreferredWidth(20);
            }
            else {
                table.getColumnModel().getColumn(i).setPreferredWidth(100);
            }
        }
        /*
            Set Filter For The Table
         */
        TableFilterHeader filterHeader = new TableFilterHeader(table, AutoChoices.ENABLED);
        filterHeader.setBackground(Color.ORANGE);
        table.setRowHeight(35);
        //for(int i = 0; i< table.getRowCount(); i++){table.setRowHeight(i,35);}
    }

    static String[] addElement(String[] a, String e) {

        a  = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }
    static Object[] addElement(Object[] objects, Object e) {

        objects  = Arrays.copyOf(objects, objects.length + 1);
        objects[objects.length - 1] = e;
        return objects;
    }
}
