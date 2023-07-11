package org.opentcs.guing.application.action.report.function;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import org.jfree.ui.TextAnchor;
import org.opentcs.database.entity.errorchart.VehicleErrorCount;
import org.opentcs.database.entity.VehicleActiveLog;
import org.opentcs.database.entity.VehicleTotalDistance;
import org.opentcs.database.entity.TimeActivity;
import org.opentcs.database.access.ReportDataHandler;
import org.opentcs.database.entity.errorchart.ErrorChartObject;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;
import java.awt.*;
import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;


public class MakeChart {
    private final ReportDataHandler dataHandler = ReportDataHandler.getInstance();
    private static MakeChart instance = null;
    private MakeChart() {}
    public static MakeChart getInstance() {
        if (instance == null) {
            instance = new MakeChart();
        }
        return instance;
    }
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.REPORT_PATH);
    /**
     * @param dateStart StartDate for Select SQL from DataBase
     * @param dateEnd   EndDate for Select SQL from DataBase
     * @param chartName  Decided which Chart gonna be create
     * @return ChartPanel
     */
    public ChartPanel createChart(Date dateStart, Date dateEnd, ChartNames chartName){
        JFreeChart chartPanelData = null;
        DefaultCategoryDataset categoryDataset = null;
        DefaultPieDataset pieDataset = null;
        switch (chartName){
            case ACTIVE_TIME_COLUMN_CHART:
                ArrayList<VehicleActiveLog> activeLogList = dataHandler.getVehicleActiveLogsForChart(dateStart,dateEnd);
                categoryDataset = new DefaultCategoryDataset();
                for (VehicleActiveLog object : activeLogList){
                    categoryDataset.setValue(object.getTotalActiveTime(),
                            labels.getString("reportChart.label.vehicleRunTime"),
                            object.getVehicleName());
                }
                String chartTitle1 = "Vehicle's Run Time Count By Hour From "+ dateStart + " to " + dateEnd;
                chartPanelData = ChartFactory.createStackedBarChart3D(
                        chartTitle1,
                        labels.getString("reportChart.label.vehicleLabel"),
                        labels.getString("reportChart.label.TotalRunTime"),
                        categoryDataset,PlotOrientation.VERTICAL,true,true,false);
                CategoryItemRenderer renderer = ((CategoryPlot)chartPanelData.getPlot()).getRenderer();
                //display colunm value on top of da column
                renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
                renderer.setBaseItemLabelsVisible(true);
                ItemLabelPosition position = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER);
                renderer.setBasePositiveItemLabelPosition(position);
                break;
            case ACTIVE_TIME_ROUND_CHART:
                activeLogList =  dataHandler.getVehicleActiveLogsForChart(dateStart,dateEnd);
                pieDataset = new DefaultPieDataset();
                for (VehicleActiveLog object : activeLogList){
                    pieDataset.setValue(object.getVehicleName()==null?"null":object.getVehicleName(),
                            object.getTotalActiveTime());
                }
                chartPanelData = ChartFactory.createPieChart3D("Active Time Overview Chart",
                        pieDataset,true,true,false);
                PiePlot plotTwist = (PiePlot) chartPanelData.getPlot();
                plotTwist.setCircular(true);
                plotTwist.setLabelGenerator(new StandardPieSectionLabelGenerator(
                        "{0} = {2}", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()
                ));
                plotTwist.setNoDataMessage("No data available");
                break;
            case ERROR_COLUMN_CHART:
                ArrayList<ErrorChartObject> columnList = dataHandler.getErrorList(dateStart, dateEnd);
                 categoryDataset = new DefaultCategoryDataset();
                for (ErrorChartObject object : columnList)
                {
                    categoryDataset.setValue(object.getNumberOfError(),
                            labels.getString("reportChart.label.numOfError"),
                            object.getVehicleName());
                }
                chartPanelData = ChartFactory.createBarChart3D(
                        labels.getString("reportChart.label.chartName"),
                        labels.getString("reportChart.label.vehicleLabel"),
                        labels.getString("reportChart.label.errorLabel"),
                        categoryDataset,PlotOrientation.HORIZONTAL,true,true,false);
                renderer = ((CategoryPlot)chartPanelData.getPlot()).getRenderer();
                //display colunm value on top of da column
                renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
                renderer.setBaseItemLabelsVisible(true);
                position = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER);
                renderer.setBasePositiveItemLabelPosition(position);
                break;
            case ERROR_ROUND_CHART:
                ArrayList<ErrorChartObject> roundlist = dataHandler.getErrorList(dateStart, dateEnd);
                pieDataset = new DefaultPieDataset();
                for (ErrorChartObject object : roundlist){
                    pieDataset.setValue(object.getVehicleName()==null?"null":object.getVehicleName(),
                            object.getNumberOfError());
                }
                chartPanelData = ChartFactory.createPieChart3D("Error Overview Chart",
                        pieDataset,true,true,false);
                plotTwist = (PiePlot) chartPanelData.getPlot();
                plotTwist.setCircular(true);
                plotTwist.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} = {2}", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()
                ));
                plotTwist.setNoDataMessage("No data available");
                break;
            case ERROR_STACKED_COLUMN_CHART:
                //set up Dataset
                ArrayList<VehicleErrorCount> listErrorCount = dataHandler.getErrorCountList(dateStart,dateEnd);
                categoryDataset = new DefaultCategoryDataset();
                for (VehicleErrorCount vehicleErrorCount : listErrorCount){
                    for (int i = 0; i< Arrays.stream(vehicleErrorCount.getCount()).count();i++)
                    {
                        categoryDataset.setValue(
                                vehicleErrorCount.getCount()[i],
                                vehicleErrorCount.getErrorCodeList().get(i),
                                vehicleErrorCount.getVehicleName());
                    }
                }
                String chartTitle = "Vehicle's ErrorCount By Time From "+ dateStart + " to " + dateEnd;
                chartPanelData = ChartFactory.createStackedBarChart3D(
                        chartTitle,  // chart title
                        "Vehicles",                  // domain axis label
                        "Total Error",                     // range axis label
                        categoryDataset,                     // data
                        PlotOrientation.VERTICAL,    // the plot orientation
                        true,                        // legend
                        true,                        // tooltips
                        false
                );
                break;
            case DISTANCE_COLUMN_CHART:
                ArrayList<VehicleTotalDistance> listVehicleTotalDistance = dataHandler.getTotalDistance(dateStart, dateEnd);
                categoryDataset = new DefaultCategoryDataset();
                for (VehicleTotalDistance object : listVehicleTotalDistance)
                {
                    categoryDataset.setValue(object.getTotalDistance(),
                                            labels.getString("freechart.barchart.distance"),
                                            object.getVehicleProcess()==null?"null":object.getVehicleProcess());
                }
                chartPanelData = ChartFactory.createBarChart3D(labels.getString("freechart.barchart.statisticalbarchart"),
                        labels.getString("freechart.barchart.vehicle"),
                        labels.getString("freechart.barchart.totaldistance"),
                        categoryDataset,
                        PlotOrientation.HORIZONTAL,true, true, false);
                renderer = ((CategoryPlot)chartPanelData.getPlot()).getRenderer();
                //display colunm value on top of da column
                renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
                renderer.setBaseItemLabelsVisible(true);
                position = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER);
                renderer.setBasePositiveItemLabelPosition(position);
                break;

            case DISTANCE_ROUND_CHART:
                ArrayList<VehicleTotalDistance> list = dataHandler.getTotalDistance(dateStart,dateEnd);
                pieDataset = new DefaultPieDataset();
                for (VehicleTotalDistance object : list){
                    pieDataset.setValue(object.getVehicleProcess()==null?"null":object.getVehicleProcess(),
                                        object.getTotalDistance());
                }
                chartPanelData = ChartFactory.createPieChart3D(labels.getString("freechart.piechart.statiscalpiechart"),
                        pieDataset,true,true,false);
                break;
            case TIME_COLUMN_CHART:
                ArrayList<TimeActivity> listTimeChart =dataHandler.getTimeActivityList(dateStart,dateEnd);
                categoryDataset  = new DefaultCategoryDataset();
                for (TimeActivity object: listTimeChart)
                {
                    categoryDataset.setValue(object.getTotalactivity(),
                            labels.getString("timeChart.label.Activity"),
                            object.getVehicle_process()==null?"null":object.getVehicle_process());
                    categoryDataset.setValue(object.getNoActivity(),
                            labels.getString("timeChart.label.NoActivity"),
                            object.getVehicle_process()==null?"null":object.getVehicle_process());
                }
                chartPanelData = ChartFactory.createBarChart3D(
                        labels.getString("timeChart.label.ChartName"),
                        labels.getString("timeChart.label.Vehicles"),
                        labels.getString("timeChart.label.TimeActive"),
                        categoryDataset,PlotOrientation.VERTICAL,true,true,false);
                renderer = ((CategoryPlot)chartPanelData.getPlot()).getRenderer();
                //display colunm value on top of da column
                renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
                renderer.setBaseItemLabelsVisible(true);
                position = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER);
                renderer.setBasePositiveItemLabelPosition(position);
                break;
            default:
                break;
        }

        ChartPanel chartPanel = new ChartPanel(chartPanelData);
        if (chartName == ChartNames.ERROR_STACKED_COLUMN_CHART) {
            chartPanel.setPreferredSize(new Dimension(700, 300));
        }else {
            chartPanel.setPreferredSize(new Dimension(700, 450));
        }
        return  chartPanel;
    }

}
