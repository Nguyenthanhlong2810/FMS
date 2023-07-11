package org.opentcs.guing.application.action.report.form;


import org.jfree.chart.ChartPanel;
import org.opentcs.guing.application.action.report.function.ChartNames;
import org.opentcs.guing.application.action.report.function.MakeChart;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.gui.Icons;
import javax.swing.*;
import java.awt.*;

import java.sql.Date;
import java.util.List;

public class Chart extends JFrame {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.REPORT_PATH);
    public Chart(Date dateStart, Date dateEnd, int select){
        setTitle(labels.getString("freechart.frame.title"));
        List<Image> icons = Icons.getOpenTCSIcons();
        setIconImage(icons.get(0));
        gui(dateStart, dateEnd, select);
    }
    MakeChart makeChart = MakeChart.getInstance();
    public void gui(Date dateStart, Date dateEnd,int select){
        this.setSize(new Dimension(1400,800));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        switch (select){
            case 0:
                /*
                 *  create Distance ColumnChart and RoundChart
                 */
                ChartPanel chartDistance1 = makeChart.createChart(dateStart,dateEnd, ChartNames.DISTANCE_COLUMN_CHART);
                ChartPanel chartDistance2 = makeChart.createChart(dateStart,dateEnd, ChartNames.DISTANCE_ROUND_CHART);
                add(chartDistance1);
                add(chartDistance2);
                break;
            case 1:
                /*
                 * create TimeColumnChart
                 */
                ChartPanel charTime = makeChart.createChart(dateStart,dateEnd, ChartNames.TIME_COLUMN_CHART);
                JScrollPane scrollChartTime = new JScrollPane(charTime);
                add(scrollChartTime);
                break;
            case 2 :
                /*
                 * Config Report ColumnChart and RoundChart
                 */
                this.setLayout(new BorderLayout());
                ChartPanel errorColumnChart = makeChart.createChart(dateStart,dateEnd, ChartNames.ERROR_COLUMN_CHART);
                ChartPanel errorRoundChart = makeChart.createChart(dateStart,dateEnd, ChartNames.ERROR_ROUND_CHART);
                ChartPanel errorStackedColumnChart = makeChart.createChart(dateStart,dateEnd,ChartNames.ERROR_STACKED_COLUMN_CHART);
                add(errorStackedColumnChart, BorderLayout.PAGE_START);
                JPanel panel = new JPanel(new GridLayout());
                panel.add(errorColumnChart);
                panel.add(errorRoundChart);
                add(panel);
                break;
            case 3:
                this.setLayout(new GridLayout());
                ChartPanel activeLogColumnChart = makeChart.createChart(dateStart,dateEnd,ChartNames.ACTIVE_TIME_COLUMN_CHART);
                add(activeLogColumnChart);
                ChartPanel activeLogRoundChart = makeChart.createChart(dateStart,dateEnd,ChartNames.ACTIVE_TIME_ROUND_CHART);
                add(activeLogRoundChart);
                break;
            default:
                break;
        }
        this.validate();
    }







}
