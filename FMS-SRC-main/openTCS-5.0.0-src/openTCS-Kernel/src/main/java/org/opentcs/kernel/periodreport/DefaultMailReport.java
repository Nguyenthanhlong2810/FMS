package org.opentcs.kernel.periodreport;

import org.opentcs.database.entity.ErrorCount;
import org.opentcs.database.access.ReportDataHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;


public class DefaultMailReport extends ReportMail {
        private static final String TABLE_DETAIL_BY_ERROR = "<%tableDetailByError%>";
    private static final String TABLE_DETAIL_BY_VEHICLE = "<%tableDetailByVehicle%>";

    ReportDataHandler dataHandler = ReportDataHandler.getInstance();
    public DefaultMailReport(String[] receipts, Date from, Date to) {
        super(receipts, from, to);
        initContent();
    }

    private void initContent() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        setSubject("[" + name + "] " +
                "Statistic: AGVs Operation by time " +
                "from " + formatter.format(from) + " to " + formatter.format(to));

        File file = new File(getClass().getClassLoader().getResource("default-error-report-email-template.html").getFile());
        try {
            content.append(new String(Files.readAllBytes(file.toPath())));
            content.insert(content.indexOf(CUSTOMER_NAME), name);
            content.insert(content.indexOf(FROM), formatter.format(from));
            content.insert(content.indexOf(TO), formatter.format(to));
            content.insert(content.indexOf(TOTAL_ERROR), dataHandler.getErrorCount(from, to));
            content.insert(content.indexOf(TABLE_DETAIL_BY_ERROR), writeByError());
            content.insert(content.indexOf(TABLE_DETAIL_BY_VEHICLE), writeByVehicle());
            content = new StringBuilder(content.toString().replaceAll("<!--(.|\\n)*?-->|<%.+?%>", ""));
        } catch (IOException e)
        {
            e.printStackTrace();
            content = new StringBuilder();
            writeDefaultEmail();
        }
    }

    private String writeByError() {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (ErrorCount error : dataHandler.getErrorLogGroupByError(from, to)) {
            builder.append("<tr height=\"30px\" style=\"border-bottom: 1px solid #a1a1a1;\">\n" +
                    "              <th scope=\"row\" width=\"10%\" style=\"text-align: left; padding-left: 10px;\">" + index++ + "</th>\n" +
                    "              <td width=\"30%\" style=\"text-align: left;\" scope=\"col\">" + error.getErrorCode() + "</td>\n" +
                    "              <td width=\"40%\" style=\"text-align: left;\" scope=\"col\">" + error.getErrorMessage() + "</td>\n" +
                    "              <td width=\"20%\" style=\"text-align: right; padding-right: 10px;\" scope=\"col\">" + error.getCount() + " time(s)</td>\n" +
                    "            </tr>\n");
        }
        return builder.toString();
    }

    private String writeByVehicle() {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (ErrorCount error : dataHandler.getErrorLogGroupByVehicle(from, to)) {
            builder.append("<tr height=\"30px\" style=\"border-bottom: 1px solid #a1a1a1;\">\n" +
                    "              <th scope=\"row\" width=\"10%\" style=\"text-align: left; padding-left: 10px;\">" + index++ + "</th>\n" +
                    "              <td width=\"70%\" style=\"text-align: left;\" scope=\"col\">" + error.getErrorVehicle() + "</td>\n" +
                    "              <td width=\"20%\" style=\"text-align: right; padding-right: 10px;\" scope=\"col\">" + error.getCount() + " time(s)</td>\n" +
                    "            </tr>\n");
        }
        return builder.toString();
    }

    private void writeDefaultEmail() {
        addLine("<h2>Total error: " + dataHandler.getErrorCount(from, to) + " error</h2>");
        addLine();

        writeByErrorDefault();
        writeByVehicleDefault();
    }

    private void writeByErrorDefault() {
        ArrayList<ErrorCount> errorCounts = dataHandler.getErrorLogGroupByError(from, to);
        addLine("<p>Detail by errors: </p>");
        addLine("<ol style='list-style-type:1'>");
        for (ErrorCount error : errorCounts) {
            addLine("<li>Error " + error.getErrorCode() + " - " + error.getErrorMessage() + ": " + error.getCount() +  " time(s)</li>");
        }
        addLine("</ol>");
        addLine();
    }

    private void writeByVehicleDefault() {
        ArrayList<ErrorCount> errorCounts = dataHandler.getErrorLogGroupByVehicle(from, to);
        addLine("<p>Detail by vehicles: </p>");
        addLine("<ol style='list-style-type:1'>");
        for (ErrorCount error : errorCounts) {
            addLine("<li>" + error.getErrorVehicle() + ": " + error.getCount() +  " time(s)</li>");
        }
        addLine("</ol>");
    }
}
