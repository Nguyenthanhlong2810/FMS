package org.opentcs.guing.components.dialogs;

import org.opentcs.database.access.EmailReceiptsDal;
import org.opentcs.guing.application.OpenTCSView;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.sql.Date;

public class EmailAddressForm extends JDialog {

    private EmailReceiptsDal dal = new EmailReceiptsDal();
    private final int mode;

    private EmailTableModel tableModel;
    private JTable emailList;

    private JPopupMenu menu = new JPopupMenu();
    private JMenuItem itemRemove = new JMenuItem("Remove");

    private JPanel buttonsPanel = new JPanel();
    private JButton btnOk = new JButton("OK");
    private JButton btnCancel = new JButton("Cancel");

    private OpenTCSView view;
    private Date from;
    private Date to;

    public EmailAddressForm() {
        super((Frame) null, "Email list", true);
        this.mode = 1;
        initComponents();
    }

    public EmailAddressForm(Date from, Date to, OpenTCSView view) {
        super((Dialog) null, "Email list", true);
        this.mode = 2;
        this.view = view;
        this.from = from;
        this.to = to;
        initComponents();
    }

    private void initComponents() {
        this.add(new JLabel(" This email list used for sending mail reporting vehicle error"), BorderLayout.NORTH);
        emailList = new JTable();
        emailList.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));
        initTable();
        menu.add(itemRemove);
        this.setLayout(new BorderLayout());
        this.add(emailList, BorderLayout.CENTER);

        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.add(btnOk);
        buttonsPanel.add(btnCancel);
        initButtonsEvent();
        this.add(buttonsPanel, BorderLayout.SOUTH);

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(300, 300);
        this.setLocationRelativeTo(null);
    }

    private void initTable() {
        tableModel = new EmailTableModel(dal.getAll(), mode);
        emailList.setModel(tableModel);
        tableModel.addTableModelListener(e -> {
            if ((e.getLastRow() + 1) == tableModel.getRowCount()) {
                if (!tableModel.getValueAt(tableModel.getRowCount() - 1, 0).equals("")) {
                    tableModel.addEmail("");
                }
            }
        });

        emailList.setRowHeight(20);

        emailList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }

    private void initButtonsEvent() {
        btnOk.addActionListener(e -> {
            String[] emails = new String[tableModel.getRowCount() - 1];
            for (int i = 0; i < emails.length; i++) {
                emails[i] = (String) tableModel.getValueAt(i, 0);
            }
            dal.addNewEmailList(emails);

            if (mode == 2) {
                boolean sent = view.sendErrorLogReport(tableModel.getChoosedEmail(), from, to);
                JOptionPane.showMessageDialog(null, sent ? "Send mail successfully" : "Can't send mail, please try again");
            }

            EmailAddressForm.this.dispose();
        });

        btnCancel.addActionListener(e -> {
            EmailAddressForm.this.dispose();
        });

        itemRemove.addActionListener(e -> {
            tableModel.removeEmail(emailList.getSelectedRow());
        });
    }

    public static class EmailTableModel extends AbstractTableModel {
        private final int mode;
        private final ArrayList<String> emailList;
        private final ArrayList<Boolean> checklist;

        public EmailTableModel(ArrayList<String> emailList, int mode) {
            this.mode = mode;
            this.emailList = emailList;
            checklist = new ArrayList<>();
            for (String ignored : emailList) {
                checklist.add(true);
            }
            addEmail("");
        }

        @Override
        public int getRowCount() {
            return emailList.size();
        }

        @Override
        public int getColumnCount() {
            return mode;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return Boolean.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Email";
                case 1:
                    return "Choose";
                default:
                    return "???";
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return emailList.get(rowIndex);
                case 1:
                    return checklist.get(rowIndex);
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    emailList.set(rowIndex, (String) aValue);
                    fireTableRowsUpdated(rowIndex, rowIndex);
                    break;
                case 1:
                    checklist.set(rowIndex, (Boolean) aValue);
                    break;
            }
        }

        public void addEmail(String email) {
            emailList.add(email);
            checklist.add(false);

            fireTableDataChanged();
        }

        public void removeEmail(int index) {
            emailList.remove(index);
            checklist.remove(index);

            fireTableDataChanged();
        }

        public String[] getChoosedEmail() {
            ArrayList<String> choosedEmails = new ArrayList<>();
            for (int i = 0; i < emailList.size() - 1; i++) {
                if (checklist.get(i)) {
                    choosedEmails.add(emailList.get(i));
                }
            }
            return choosedEmails.toArray(new String[0]);
        }
    }

}
