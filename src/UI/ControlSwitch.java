package UI;

import java.awt.event.*;
import Utils.Config;
import Utils.DBUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import javax.swing.*;

public class ControlSwitch extends JPanel {
    public static final String[] DB_SERVER = new String[]{"Mysql","Sqlite"};
    public ControlSwitch() {
        initComponents();
    }

    private void dbServer(ActionEvent e) {
        // TODO add your code here
        int dbServeRChose = dbServerComboBox.getSelectedIndex();
        switch (dbServeRChose){
            case DBUtil.MYSQL_DB:
                hostTextField.setEnabled(true);
                portTextField.setEnabled(true);
                usernameTextField.setEnabled(true);
                databaseTextField.setEnabled(true);
                passwdTextField.setEnabled(true);
                break;
            case DBUtil.SQLITE_DB:
                hostTextField.setEnabled(false);
                portTextField.setEnabled(false);
                usernameTextField.setEnabled(false);
                databaseTextField.setEnabled(false);
                passwdTextField.setEnabled(false);
                break;
            default:
                break;
        }

    }
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label6 = new JLabel();
        dbServerComboBox = new JComboBox<>();
        dbConfigPanel = new JPanel();
        label1 = new JLabel();
        hostTextField = new JTextField();
        label5 = new JLabel();
        portTextField = new JTextField();
        label2 = new JLabel();
        usernameTextField = new JTextField();
        label3 = new JLabel();
        passwdTextField = new JTextField();
        label4 = new JLabel();
        databaseTextField = new JTextField();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

        //---- label6 ----
        label6.setText("db_server:");
        add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- dbServerComboBox ----
        dbServerComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
            "Mysql",
            "Sqlite"
        }));
        dbServerComboBox.addActionListener(e -> dbServer(e));
        add(dbServerComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //======== dbConfigPanel ========
        {
            dbConfigPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)dbConfigPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)dbConfigPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
            ((GridBagLayout)dbConfigPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)dbConfigPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- label1 ----
            label1.setText("host:");
            dbConfigPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));
            dbConfigPanel.add(hostTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label5 ----
            label5.setText("port:");
            dbConfigPanel.add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));
            dbConfigPanel.add(portTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label2 ----
            label2.setText("username:");
            dbConfigPanel.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));
            dbConfigPanel.add(usernameTextField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label3 ----
            label3.setText("password:");
            dbConfigPanel.add(label3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));
            dbConfigPanel.add(passwdTextField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("database:");
            dbConfigPanel.add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));
            dbConfigPanel.add(databaseTextField, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(dbConfigPanel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
        if(Config.isBuild()){
            try{
                Map<String,String> json = Config.parseJson();
                dbServerComboBox.setSelectedIndex(Arrays.binarySearch(DB_SERVER,json.get("db_server")));
                hostTextField.setText(json.get("host"));
                portTextField.setText(json.get("port"));
                usernameTextField.setText(json.get("username"));
                passwdTextField.setText(json.get("password"));
                databaseTextField.setText(json.get("database"));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label6;
    public JComboBox<String> dbServerComboBox;
    private JPanel dbConfigPanel;
    private JLabel label1;
    public JTextField hostTextField;
    private JLabel label5;
    public JTextField portTextField;
    private JLabel label2;
    public JTextField usernameTextField;
    private JLabel label3;
    public JTextField passwdTextField;
    private JLabel label4;
    public JTextField databaseTextField;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
