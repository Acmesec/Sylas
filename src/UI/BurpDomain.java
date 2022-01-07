/*
 * Created by JFormDesigner on Sun Jan 02 20:49:48 CST 2022
 */

package UI;

import Domain.DomainProducer;
import Utils.Config;
import burp.BurpExtender;
import burp.IHttpRequestResponse;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.*;

import static java.lang.Thread.sleep;

/**
 * @author linchen
 */
public class BurpDomain extends JPanel {
    public BurpDomain() {
        initComponents();
        initBurpDomain();
    }

    public Map<String, String> runSearch() throws InterruptedException {
        IHttpRequestResponse[] messages = BurpExtender.getCallbacks().getSiteMap(null);
        BurpExtender.inputQueue.addAll(Arrays.asList(messages));
        for (int i = 0; i < 10; i++) {
            DomainProducer domainProducer = new DomainProducer();
            domainProducer.start();
        }
        while(!BurpExtender.inputQueue.isEmpty()){
            sleep(1000);
        }
        return null;
    }

    private void searchButtonActionPerformed(ActionEvent e) {
        SwingWorker<Map, Map> worker;
        worker = new SwingWorker<Map, Map>() {
            @Override
            protected Map doInBackground() throws Exception{
                searchButton.setEnabled(false);
                return runSearch();
            }
            @Override
            protected void done(){
                try{
                    get();
                    searchButton.setEnabled(true);
                }catch (Exception e){
                    BurpExtender.getStderr().println(e);
                    searchButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void connectDatabaseButtonActionPerformed(ActionEvent e) {
        // TODO add your code here
        ControlSwitch conSwitch = new ControlSwitch();
        int connect = JOptionPane.showConfirmDialog(null,conSwitch,"Connect Database",JOptionPane.OK_CANCEL_OPTION);
        if (connect==0){
            String host = conSwitch.hostTextField.getText();
            String port = conSwitch.portTextField.getText();
            String username = conSwitch.usernameTextField.getText();
            String passwd = conSwitch.passwdTextField.getText();
            String database = conSwitch.databaseTextField.getText();
            BurpExtender.db.setConn(host,port,username,passwd,database);
            if(BurpExtender.db.isConnect){
                connectDatabaseButton.setText("Status: Connected");
                connectDatabaseButton.setEnabled(false);
                closeConnectButton.setEnabled(true);
                BurpExtender.db.init(database);
                if (!Config.burpDomainConfig.exists()){
                    try {
                        Config.burpDomainConfig.createNewFile();
                    } catch (IOException e2) {
                        BurpExtender.getStderr().println(e2);
                    }
                }
                BurpExtender.config.put("host",host);
                BurpExtender.config.put("port",port);
                BurpExtender.config.put("username",username);
                BurpExtender.config.put("password",passwd);
                BurpExtender.config.put("database",database);
                Config.writeJson(BurpExtender.config);
                projectDoneAction(BurpExtender.config.get("currentProject"));
            }else{
                connectDatabaseButton.setText("Status: Connect Failed");
            }
        }

    }

    private void projectSettingActionPerformed(ActionEvent e) {
        // TODO add your code here
        NewProject nP = new NewProject(NewProject.PROJECT_MODE);
        int project = JOptionPane.showConfirmDialog(null,nP,"",JOptionPane.OK_CANCEL_OPTION);
        if(project==0 && BurpExtender.db.isConnect){
            String currentProject = nP.itemList.getSelectedValue();
            if(nP.list.contains(currentProject)){
                BurpExtender.config.put("currentProject", currentProject);
                Config.writeJson(BurpExtender.config);
                projectDoneAction(currentProject);
            }else{
                JOptionPane.showMessageDialog(null,"Must select a project!","No Project",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rootDomainSettingActionPerformed(ActionEvent e) {
        // TODO add your code here
        NewProject nP = new NewProject(NewProject.DOMAIN_MODE);
        int project = JOptionPane.showConfirmDialog(null,nP,"",JOptionPane.OK_CANCEL_OPTION);
        if(project==0){
            BurpExtender.currentRootDomainSet = BurpExtender.db.getRootDomainSet(BurpExtender.config.get("currentProject"));
        }
        if(BurpExtender.currentRootDomainSet.size() > 0){
            searchButton.setEnabled(true);
        }
    }

    private void closeConnect(ActionEvent e) {
        // TODO add your code here
        BurpExtender.db.closeConn();
        connectDatabaseButton.setEnabled(true);
        connectDatabaseButton.setText("Connect");
        closeConnectButton.setEnabled(false);
        rootDomainSetting.setEnabled(false);
        projectSetting.setEnabled(false);
        searchButton.setEnabled(false);
        label2.setText("------");
        connectDatabaseButton.setText("Connect Database");
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel2 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        label8 = new JLabel();
        searchButton = new JButton();
        connectDatabaseButton = new JButton();
        closeConnectButton = new JButton();
        label3 = new JLabel();
        projectSetting = new JButton();
        rootDomainSetting = new JButton();
        label5 = new JLabel();
        copyAllUrlsButton = new JButton();
        copyAllDomainsButton = new JButton();
        panel1 = new JPanel();
        label6 = new JLabel();
        label7 = new JLabel();
        scrollPane2 = new JScrollPane();
        subDomainTable = new JTable();
        scrollPane3 = new JScrollPane();
        urlTable = new JTable();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {834, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

        //======== panel2 ========
        {
            panel2.setLayout(new GridBagLayout());
            ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

            //---- label1 ----
            label1.setText("Project Name:");
            panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- label2 ----
            label2.setText("------");
            panel2.add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- label8 ----
            label8.setText("|");
            panel2.add(label8, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- searchButton ----
            searchButton.setText("Grep Domain&Url");
            searchButton.setEnabled(false);
            searchButton.addActionListener(e -> searchButtonActionPerformed(e));
            panel2.add(searchButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- connectDatabaseButton ----
            connectDatabaseButton.setText("Connect Database");
            connectDatabaseButton.addActionListener(e -> connectDatabaseButtonActionPerformed(e));
            panel2.add(connectDatabaseButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- closeConnectButton ----
            closeConnectButton.setText("Close Connect");
            closeConnectButton.setEnabled(false);
            closeConnectButton.addActionListener(e -> closeConnect(e));
            panel2.add(closeConnectButton, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- label3 ----
            label3.setText("|");
            panel2.add(label3, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- projectSetting ----
            projectSetting.setText("Project Setting");
            projectSetting.addActionListener(e -> projectSettingActionPerformed(e));
            panel2.add(projectSetting, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- rootDomainSetting ----
            rootDomainSetting.setText("RootDomain Setting");
            rootDomainSetting.setEnabled(false);
            rootDomainSetting.addActionListener(e -> rootDomainSettingActionPerformed(e));
            panel2.add(rootDomainSetting, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- label5 ----
            label5.setText("|");
            panel2.add(label5, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- copyAllUrlsButton ----
            copyAllUrlsButton.setText("Copy all Urls");
            panel2.add(copyAllUrlsButton, new GridBagConstraints(10, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- copyAllDomainsButton ----
            copyAllDomainsButton.setText("Copy all Domains");
            panel2.add(copyAllDomainsButton, new GridBagConstraints(11, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(10, 0, 5, 0), 0, 0));

        //======== panel1 ========
        {
            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
            ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.35, 0.65, 1.0E-4};
            ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

            //---- label6 ----
            label6.setText("Sub Domians");
            panel1.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- label7 ----
            label7.setText("Urls");
            panel1.add(label7, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== scrollPane2 ========
            {

                //---- subDomainTable ----
                subDomainTable.setModel(new DefaultTableModel());
                subDomainTable.setSurrendersFocusOnKeystroke(true);
                subDomainTable.setAutoCreateRowSorter(true);
                subDomainTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                scrollPane2.setViewportView(subDomainTable);
            }
            panel1.add(scrollPane2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //======== scrollPane3 ========
            {

                //---- urlTable ----
                urlTable.setModel(new DefaultTableModel());
                urlTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                urlTable.setSurrendersFocusOnKeystroke(true);
                scrollPane3.setViewportView(urlTable);
            }
            panel1.add(scrollPane3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
        String[] subDomainColumnNames = {"#", "Domain", "IP", "Time"};
        subDomainModel = new DefaultTableModel(null, subDomainColumnNames){
            @Override
            public Class<?> getColumnClass(int column) { return getValueAt(0,column).getClass();}
        };
        subDomainTable.setModel(subDomainModel);
        subDomainTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        subDomainTable.getColumnModel().getColumn(1).setPreferredWidth(385);
        subDomainTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        subDomainTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        String[] urlColumnNames = {"#", "URL", "Time"};
        urlModel = new DefaultTableModel(null, urlColumnNames){
            @Override
            public Class<?> getColumnClass(int column) { return getValueAt(0,column).getClass();}
        };
        urlTable.setModel(urlModel);
        urlTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        urlTable.getColumnModel().getColumn(1).setPreferredWidth(690);
        urlTable.getColumnModel().getColumn(2).setPreferredWidth(160);
    }
    public static void addSubDomainToUI(String domain, String ip, String time){
        subDomainModel.addRow(new Object[]{BurpExtender.subDomainCount, domain, ip, time});
    }

    public static void addURLToUI(String url, String time){
        urlModel.addRow(new Object[]{BurpExtender.urlCount, url, time});
    }

    public void clearUI(){
        BurpExtender.subDomainCount = 0;
        subDomainModel.setRowCount(0);
        BurpExtender.urlCount = 0;
        urlModel.setRowCount(0);
    }
    //自适应宽度
    public void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            // 最小宽度
            int width = 15;
            if (column == 0){
                columnModel.getColumn(column).setPreferredWidth(1);
            }else{
                for (int row = 0; row < table.getRowCount(); row++) {
                    TableCellRenderer renderer = table.getCellRenderer(row, column);
                    Component comp = table.prepareRenderer(renderer, row, column);
                    width = Math.max(comp.getPreferredSize().width +1 , width);
                }
                if(width > 400) {
                    width=400;
                }
                columnModel.getColumn(column).setPreferredWidth(width);
            }

        }
    }
    public boolean autoConnectDatabaseByConfig(){
        if(Config.isBuild()){
            try{
                if(!"".equals(BurpExtender.config.get("host"))){
                    String host = BurpExtender.config.get("host");
                    String port = BurpExtender.config.get("port");
                    String username = BurpExtender.config.get("username");
                    String password = BurpExtender.config.get("password");
                    String database = BurpExtender.config.get("database");
                    BurpExtender.db.setConn(host, port, username, password, database);
                    if(BurpExtender.db.isConnect){
                        connectDatabaseButton.setEnabled(false);
                        connectDatabaseButton.setText("Status: Connected");
                        closeConnectButton.setEnabled(true);
                        BurpExtender.db.init(database);
                        return true;
                    }
                }
            }catch (Exception e){
                BurpExtender.getStderr().println(e);
            }
        }
        return false;
    }

    public void projectDoneAction(String currentProject){
        projectSetting.setEnabled(true);
        if(!"".equals(currentProject)){
            clearUI();
            try{
                sleep(1000);
            }catch (InterruptedException e){
                BurpExtender.getStderr().println(e);
            }
            label2.setText(currentProject);
            rootDomainSetting.setEnabled(true);
            BurpExtender.currentRootDomainSet = BurpExtender.db.getRootDomainSet(currentProject);
            if(BurpExtender.currentRootDomainSet.size() > 0){
                searchButton.setEnabled(true);
                BurpExtender.subDomainMap = BurpExtender.db.getSubDomainMap(currentProject);
                BurpExtender.urlMap = BurpExtender.db.getUrlMap(currentProject);
                for(Map.Entry<String, String> entry: BurpExtender.urlMap.entrySet()){
                    BurpExtender.urlCount += 1;
                    String createTime = entry.getValue();
                    addURLToUI(entry.getKey(), createTime);
                }
                for(Map.Entry<String, HashMap<String, String>> entry: BurpExtender.subDomainMap.entrySet()){
                    BurpExtender.subDomainCount += 1;
                    HashMap<String, String> value = entry.getValue();
                    addSubDomainToUI(entry.getKey(), value.get("ipAddress"), value.get("createTime"));
                }
            }
        }
    }
    public void initBurpDomain(){
        if(autoConnectDatabaseByConfig()){
            String currentProject = BurpExtender.config.get("currentProject");
            projectDoneAction(currentProject);
        }
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel2;
    private JLabel label1;
    private JLabel label2;
    private JLabel label8;
    private JButton searchButton;
    private JButton connectDatabaseButton;
    private JButton closeConnectButton;
    private JLabel label3;
    private JButton projectSetting;
    private JButton rootDomainSetting;
    private JLabel label5;
    private JButton copyAllUrlsButton;
    private JButton copyAllDomainsButton;
    private JPanel panel1;
    private JLabel label6;
    private JLabel label7;
    private JScrollPane scrollPane2;
    private JTable subDomainTable;
    private JScrollPane scrollPane3;
    private JTable urlTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    private static DefaultTableModel subDomainModel;
    private static DefaultTableModel urlModel;
}
