package org.opentcs.kernel.periodreport;

import org.opentcs.database.access.ConfigurationDal;
import org.opentcs.database.access.EmailReceiptsDal;
import org.opentcs.database.entity.AubotConfiguration;

import javax.mail.*;
import java.sql.Date;

public class MailingTask extends PeriodTask {

    private static final String EMAIL_REGEX = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    public MailingTask(ResetableSchedulerTask instance, Period period) {
        super(instance.getExecutor(), instance, period);
    }


    @Override
    public void executeTask() {
        Date from = new Date(period.getLastTimeInMilis(System.currentTimeMillis()));
        Date to = new Date(System.currentTimeMillis());
        String[] receipts = new EmailReceiptsDal().getAll().toArray(new String[0]);

        try {
            new DefaultMailReport(receipts, from, to).send();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static Period getPeriodFromDb() throws Exception {
        int periodNumber;
        ConfigurationDal dal = new ConfigurationDal();
        String periodConfig = dal.getConfiguration("datelog");
        if (periodConfig == null) {
            dal.add(new AubotConfiguration("datelog",
                    String.valueOf(Period.WEEKLY.getRepresent()),
                    "This configuration for sending mail periodically: 0 - none; 1 - daily; 7 - weekly; 30 - monthly; 365 - yearly"));
            periodNumber = Period.WEEKLY.getRepresent();
        } else {
            periodNumber = Integer.parseInt(periodConfig);
        }
        return Period.getPeriod(periodNumber);
    }
}
