package org.opentcs.kernel.periodreport;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.ArrayList;
import java.sql.Date;
import java.util.Properties;

public class ReportMail {
    public static String name;

    private static final String USERNAME = "itteam.reporter.cfg@gmail.com";

    private static final String PASSWORD = "itteamwithluv"; //from it team with luv

    protected static final String EMAIL_REGEX = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    protected static final String TOTAL_ERROR = "<%total%>";
    protected static final String CUSTOMER_NAME = "<%customer%>";
    protected static final String FROM = "<%from%>";
    protected static final String TO = "<%to%>";

    protected String subject;
    protected StringBuilder content;
    protected String[] receipts;
    protected Date from;
    protected Date to;

    public ReportMail(String[] receipts, Date from, Date to) {
        this.receipts = receipts;
        this.from = from;
        this.to = to;
        subject = "Report Mail";
        content = new StringBuilder();
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void addLine(String html) {
        content.append(html).append("\n");
    }

    public void addLine() {
        content.append("<br>");
    }

    private InternetAddress[] getReceipts() {
        ArrayList<InternetAddress> arrayList = new ArrayList<>();
        for (String email : receipts) {
            if (email.matches(EMAIL_REGEX)) {
                try {
                    arrayList.add(new InternetAddress(email));
                } catch (AddressException e) {
                    e.printStackTrace();
                }
            }
        }
        return arrayList.toArray(new InternetAddress[0]);
    }

    public void send() throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        InternetAddress[] receipts = getReceipts();
        if (receipts.length == 0) {
            throw new MessagingException("No receipt to send");
        }
        message.setRecipients(Message.RecipientType.TO, getReceipts());
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(content.toString(), "text/html");
        multipart.addBodyPart(bodyPart);

        message.setContent(multipart);

        System.out.println("Sending mail...");

        Transport.send(message);

        System.out.println("Mail send successfully!");
    }
}
