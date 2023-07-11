package org.opentcs.guing.application.action.report.statistics.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.opentcs.guing.application.action.report.statistics.ErrorStatisticPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.poi.ss.usermodel.FontFamily.ROMAN;
import static org.opentcs.guing.util.I18nPlantOverview.STATISTICS_PATH;

public class ExportReport {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(STATISTICS_PATH);

  private JTable jTable = new JTable();

  private DefaultTableModel tableModel = new DefaultTableModel();

  private String title = "";

  private String NAME_COMPANY = BUNDLE.getString("exportReport.nameCompany");

  private String ADDRESS  = BUNDLE.getString("exportReport.addressCompany");

  private String dateFrom;

  private String dateTo;

  public ExportReport(JTable jTable, DefaultTableModel tableModel) {
    this.jTable = jTable;
    this.tableModel = tableModel;
  }

  public ExportReport(JTable jTable, DefaultTableModel tableModel, String dateFrom, String dateTo, String title) {
    this.jTable = jTable;
    this.tableModel = tableModel;
    this.title = title;
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
  }

  public void exportExcel(String nameFilePath, String nameSheet){

    Map<Integer, Object[]> data = createDataExport();
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet(nameSheet);
    decorateTitleExcelFile(sheet);

    Set<Integer> keyset = data.keySet();
    int rownum = 8;
    for (Integer key : keyset) {
      Row row = sheet.createRow(rownum++);
      Object[] objArr = data.get(key);
      int col = 0;
      for (int i = 0; i < objArr.length; i++) {
        Cell cell = row.createCell(col++);
        cell.setCellValue(objArr[i].toString());
        sheet.autoSizeColumn(i);
        sheet.setColumnWidth(i,sheet.getColumnWidth(i) * 2);
        setBorderCell(cell);
      }
    }

    try {
      if (!nameFilePath.endsWith(".xlsx")) {
        nameFilePath += ".xlsx";
      }
      FileOutputStream out = new FileOutputStream(nameFilePath);
      workbook.write(out);
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Map<Integer, Object[]> createDataExport() {
    Map<Integer, Object[]> data = new TreeMap<>();
    Object[] objs = new Object[jTable.getColumnCount() + 1];
    objs[0] = "STT";
    for (int i = 0; i < jTable.getColumnCount(); i++) {
      objs[i + 1] = jTable.getColumnName(i);
    }
    data.put(0, objs);
    for (int i = 0; i < jTable.getRowCount(); i++) {
      Object[] objects = new Object[jTable.getColumnCount() + 1];
      objects[0] = i + 1;
      for (int j = 0; j < jTable.getColumnCount(); j++) {
        objects[j + 1] = tableModel.getValueAt(i, j) == null ? "" : tableModel.getValueAt(i, j);
      }
      data.put(i + 1, objects);
    }
    return data;
  }


  private int mergeCell(XSSFSheet sheet, int firstRow,int lastRow, int firstCol, int lastCol){
    return sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
  }

  private void decorateTitleExcelFile(XSSFSheet sheet){
    InputStream inputStream = ErrorStatisticPanel.class.getClassLoader()
        .getResourceAsStream("aubot/image/aubot_logo.png");
//    try {
//      int pictureIdx = sheet.getWorkbook().addPicture(inputStream, Workbook.PICTURE_TYPE_PNG);
//      inputStream.close();
//      CreationHelper helper = sheet.getWorkbook().getCreationHelper();
//      Drawing drawing = sheet.createDrawingPatriarch();
//      ClientAnchor anchor = helper.createClientAnchor();
//      anchor.setCol1(0);
//      anchor.setRow1(0);
////      anchor.setCol2(2);
////      anchor.setRow2(2);
//      anchor.setDx1(0);
//      anchor.setDy1(0);
//      anchor.setDx2(239);
//      anchor.setDy2(100);
//      drawing.createPicture(anchor, pictureIdx);
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//
    XSSFCellStyle style = sheet.getWorkbook().createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setWrapText(true);
//
//    mergeCell(sheet,0,0, 2,jTable.getColumnCount());
    XSSFCell cell = sheet.createRow(0).createCell( 2);
//    cell.setCellValue(NAME_COMPANY);
//    cell.setCellStyle(style);
//
//    mergeCell(sheet,1,1, 2,jTable.getColumnCount());
//    cell = sheet.createRow(1).createCell( 2);
//    cell.setCellValue(ADDRESS);
//    cell.setCellStyle(style);

    mergeCell(sheet, 2,4,0,jTable.getColumnCount());
    sheet.getColumnWidth(jTable.getColumnCount() - 1);
    cell = sheet.createRow(2).createCell(0);
    cell.setCellValue(title);
    cell.setCellStyle(getStyleTitleExcelFile(sheet.getWorkbook()));

    mergeCell(sheet, 5,5,0,jTable.getColumnCount());
    cell = sheet.createRow(5).createCell(0);
    cell.setCellValue(BUNDLE.getString("exportReport.dateFrom") + " : " +
            dateFrom + " - " + BUNDLE.getString("exportReport.dateTo") + " : " +
            dateTo);
    cell.setCellStyle(style);

    int positionOfCreateDateLabel = jTable.getColumnCount() - 2;
    mergeCell(sheet, 6,6,positionOfCreateDateLabel + 1,positionOfCreateDateLabel + 2);

    cell = sheet.createRow(6).createCell(positionOfCreateDateLabel + 1);
    cell.setCellValue(BUNDLE.getString("exportReport.createDateLabel") + ": " +
            new SimpleDateFormat(BUNDLE.getString("exportReport.formateDate")).format(new Date()));


  }

  private XSSFCellStyle getStyleTitleExcelFile(XSSFWorkbook workbook){
    XSSFFont font = workbook.createFont();
    font.setBold(true);
    font.setFamily(ROMAN);
    font.setFontHeightInPoints((short) 18);
    XSSFCellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setFont(font);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    return style;
  }

  private void setBorderCell(Cell cell){
    CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
    cellStyle.setBorderTop(BorderStyle.THIN);
    cellStyle.setBorderBottom(BorderStyle.THIN);
    cellStyle.setBorderLeft(BorderStyle.THIN);
    cellStyle.setBorderRight(BorderStyle.THIN);
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cell.setCellStyle(cellStyle);
  }
}
