package org.opentcs.kernel.periodreport;

import org.opentcs.database.entity.ErrorLog;
import org.opentcs.database.access.ReportDataHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DefaultMailDetailReport extends ReportMail {

    private ReportDataHandler dataHandler = ReportDataHandler.getInstance();

    private static final String TABLE_DETAIL_ERROR = "<%tableDetailError%>";

    private ErrorLog currentVehicle = null;
    private int vehicleCount = 0;

    public DefaultMailDetailReport(String[] receipts, Date from, Date to) {
        super(receipts, from, to);
        initContent();
    }

    private void initContent() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        setSubject("[" + name + "] " +
                "Statistic: AGVs Operation by time " +
                "from " + formatter.format(from) + " to " + formatter.format(to));

        File file = new File(getClass().getClassLoader().getResource("default-error-details-email-report-template.html").getFile());
        try {
            content.append(new String(Files.readAllBytes(file.toPath())));
            content.insert(content.indexOf(CUSTOMER_NAME), name);
            content.insert(content.indexOf(FROM), formatter.format(from));
            content.insert(content.indexOf(TO), formatter.format(to));
            content.insert(content.indexOf(TOTAL_ERROR), dataHandler.getErrorCount(from, to));
            content.insert(content.indexOf(TABLE_DETAIL_ERROR), writeTable());
            content = new StringBuilder(content.toString().replaceAll("<!--(.|\\n)*?-->|<%.+?%>", ""));
        } catch (IOException e)
        {
            e.printStackTrace();
            content = new StringBuilder();
        }
    }

    private String writeTable() {
        ArrayList<ErrorLog> errorList = dataHandler.getErrorLogList(from, to);
        errorList.sort((o1, o2) -> o1.getErrorVehicle().compareToIgnoreCase(o2.getErrorVehicle()));
        StringBuilder bigBuilder = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        for (ErrorLog error : errorList) {
            if (currentVehicle == null || !error.getErrorVehicle().equals(currentVehicle.getErrorVehicle())) {
                if (vehicleCount != 0) {
                    builder.insert(0,
                            "<tr height=\"30px\" style=\"border-bottom: 1px solid #a1a1a1;\">\n" +
                                    "                <th rowspan=\""+ vehicleCount + "<%byVehicle%>\" scope=\"row\" width=\"10%\" style=\"text-align: left; padding-left: 10px; vertical-align: top;\">" + currentVehicle.getErrorVehicle() + "</th>\n" +
                                    "                <td style=\"text-align: left;\" scope=\"col\">" + currentVehicle.getErrorCode() + "</td>\n" +
                                    "                <td style=\"text-align: left;\" scope=\"col\">" + currentVehicle.getErrorMessage() + "</td>\n" +
                                    "                <td style=\"text-align: right; padding-right: 10px;\" scope=\"col\">" + formatter.format(currentVehicle.getDatetimelog()) + "</td>\n" +
                                    "              </tr>");
                }
                currentVehicle = error;
                vehicleCount = 1;
                bigBuilder.append(builder.toString());
                builder = new StringBuilder();
            } else {
                builder.append("<tr height=\"30px\" style=\"border-bottom: 1px solid #a1a1a1;\">\n" +
                        "                <td style=\"text-align: left;\" scope=\"col\">" + error.getErrorCode() + "</td>\n" +
                        "                <td style=\"text-align: left;\" scope=\"col\">" + error.getErrorMessage() + "</td>\n" +
                        "                <td style=\"text-align: right; padding-right: 10px;\" scope=\"col\">" + formatter.format(error.getDatetimelog()) + "</td>\n" +
                        "              </tr>");
                vehicleCount++;
            }
        }
        //finally
        builder.insert(0,
                "<tr height=\"30px\" style=\"border-bottom: 1px solid #a1a1a1;\">\n" +
                        "                <th rowspan=\""+ vehicleCount + "<%byVehicle%>\" scope=\"row\" width=\"10%\" style=\"text-align: left; padding-left: 10px; vertical-align: top;\">" + currentVehicle.getErrorVehicle() + "</th>\n" +
                        "                <td style=\"text-align: left;\" scope=\"col\">" + currentVehicle.getErrorCode() + "</td>\n" +
                        "                <td style=\"text-align: left;\" scope=\"col\">" + currentVehicle.getErrorMessage() + "</td>\n" +
                        "                <td style=\"text-align: right; padding-right: 10px;\" scope=\"col\">" + formatter.format(currentVehicle.getDatetimelog()) + "</td>\n" +
                        "              </tr>");
        bigBuilder.append(builder.toString());
        return bigBuilder.toString();
    }
}