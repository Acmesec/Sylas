/*
 * Created by JFormDesigner on Sun Jan 02 20:49:48 CST 2022
 */

package ui;

import domain.DomainProducer;
import utils.Config;
import burp.BurpExtender;
import burp.IHttpRequestResponse;
import utils.DBUtil;

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

    /**
     * 创建10个线程进行域名的提取
     * @throws InterruptedException
     */
    public void runSearch() throws InterruptedException {
        IHttpRequestResponse[] messages = BurpExtender.getCallbacks().getSiteMap(null);
        BurpExtender.inputQueue.addAll(Arrays.asList(messages));
        for (int i = 0; i < 10; i++) {
            DomainProducer domainProducer = new DomainProducer();
            domainProducer.start();
        }
        while(!BurpExtender.inputQueue.isEmpty()){
            sleep(1000);
        }
    }

    private void searchButtonActionPerformed(ActionEvent e) {
        SwingWorker<Map, Map> worker;
        worker = new SwingWorker<Map, Map>() {
            @Override
            protected Map doInBackground() throws Exception{
                searchButton.setEnabled(false);
                runSearch();
                return null;
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
            int chose = conSwitch.dbServerComboBox.getSelectedIndex();
            String dbServer = ControlSwitch.DB_SERVER[chose];
            String host = conSwitch.hostTextField.getText();
            String port = conSwitch.portTextField.getText();
            String username = conSwitch.usernameTextField.getText();
            String passwd = conSwitch.passwdTextField.getText();
            String database = conSwitch.databaseTextField.getText();
            switchDatabaseServer(chose,host,port,username,passwd,database);
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
                BurpExtender.config.put("db_server",dbServer);
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
        tabbedPane1 = new JTabbedPane();
        panel1 = new JPanel();
        label6 = new JLabel();
        label7 = new JLabel();
        scrollPane2 = new JScrollPane();
        subDomainTable = new JTable();
        scrollPane3 = new JScrollPane();
        urlTable = new JTable();
        panel3 = new JPanel();
        label4 = new JLabel();
        label9 = new JLabel();
        scrollPane1 = new JScrollPane();
        similarSubDomainTable = new JTable();
        scrollPane4 = new JScrollPane();
        similarUrlsTable = new JTable();

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

        //======== tabbedPane1 ========
        {

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
                    subDomainModel = new DefaultTableModel(null, subDomainColumnNames){
                        @Override
                        public Class<?> getColumnClass(int column) { return getValueAt(0,column).getClass();}
                    };
                    subDomainTable.setModel(subDomainModel);
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
                    urlModel = new DefaultTableModel(null, urlColumnNames){
                        @Override
                        public Class<?> getColumnClass(int column) { return getValueAt(0,column).getClass();}
                    };
                    urlTable.setModel(urlModel);
                    urlTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                    urlTable.setSurrendersFocusOnKeystroke(true);
                    urlTable.setAutoCreateRowSorter(true);
                    scrollPane3.setViewportView(urlTable);
                }
                panel1.add(scrollPane3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            tabbedPane1.addTab("Exact Match", panel1);

            //======== panel3 ========
            {
                panel3.setLayout(new GridBagLayout());
                ((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.35, 0.65, 1.0E-4};
                ((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

                //---- label4 ----
                label4.setText("Similar Sub Domain");
                panel3.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- label9 ----
                label9.setText("Similar Urls");
                panel3.add(label9, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                    new Insets(0, 0, 5, 0), 0, 0));

                //======== scrollPane1 ========
                {

                    //---- similarSubDomainTable ----
                    similarSubDomainModel = new DefaultTableModel(null, subDomainColumnNames){
                        @Override
                        public Class<?> getColumnClass(int column) { return getValueAt(0,column).getClass();}
                    };
                    similarSubDomainTable.setModel(similarSubDomainModel);
                    similarSubDomainTable.setSurrendersFocusOnKeystroke(true);
                    similarSubDomainTable.setAutoCreateRowSorter(true);
                    similarSubDomainTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                    scrollPane1.setViewportView(similarSubDomainTable);
                }
                panel3.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //======== scrollPane4 ========
                {

                    //---- similarUrlsTable ----
                    similarUrlModel = new DefaultTableModel(null, urlColumnNames){
                        @Override
                        public Class<?> getColumnClass(int column) { return getValueAt(0,column).getClass();}
                    };
                    similarUrlsTable.setModel(similarUrlModel);
                    similarUrlsTable.setSurrendersFocusOnKeystroke(true);
                    similarUrlsTable.setAutoCreateRowSorter(true);
                    similarUrlsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
                    scrollPane4.setViewportView(similarUrlsTable);
                }
                panel3.add(scrollPane4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            tabbedPane1.addTab("Similar Matching", panel3);
        }
        add(tabbedPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
        resizeColumnWidth(subDomainTable,urlTable);
        resizeColumnWidth(similarSubDomainTable,similarUrlsTable);
    }

    public static void addSubDomainToUI(String domain, String ip, String time){
        subDomainModel.addRow(new Object[]{BurpExtender.subDomainCount, domain, ip, time});
    }

    public static void addURLToUI(String url, String time){
        urlModel.addRow(new Object[]{BurpExtender.urlCount, url, time});
    }
    public static void addSimilarSubDomainToUI(String domain, String ip, String time){
        similarSubDomainModel.addRow(new Object[]{BurpExtender.similarSubDomainCount, domain, ip, time});
    }
    public static void addSimilarUrlToUI(String url, String time){

        similarUrlModel.addRow(new Object[]{BurpExtender.similarUrlCount, url, time});
    }

    // 2022年01月10日21:02:25 修复了排序数据后切换项目插件UI崩溃的情况
    public void clearUI(){
        if(BurpExtender.subDomainCount > 0){
            BurpExtender.subDomainCount = 0;
            subDomainModel.setColumnIdentifiers(subDomainColumnNames);
            subDomainModel.setRowCount(0);
        }
        if(BurpExtender.urlCount > 0){
            BurpExtender.urlCount = 0;
            urlModel.setColumnIdentifiers(urlColumnNames);
            urlModel.setRowCount(0);
        }
        if (BurpExtender.similarSubDomainCount > 0){
            BurpExtender.similarSubDomainCount = 0;
            similarSubDomainModel.setColumnIdentifiers(subDomainColumnNames);
            similarSubDomainModel.setRowCount(0);
        }
        if (BurpExtender.similarUrlCount > 0){
            BurpExtender.similarUrlCount = 0;
            similarUrlModel.setColumnIdentifiers(urlColumnNames);
            similarUrlModel.setRowCount(0);
        }
        resizeColumnWidth(subDomainTable,urlTable);
        resizeColumnWidth(similarSubDomainTable,similarUrlsTable);
    }

    /**
     * 自适应宽度
     */
    private void resizeColumnWidth(JTable domainTable,JTable urlTable){
        domainTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        domainTable.getColumnModel().getColumn(1).setPreferredWidth(385);
        domainTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        domainTable.getColumnModel().getColumn(3).setPreferredWidth(160);

        urlTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        urlTable.getColumnModel().getColumn(1).setPreferredWidth(650);
        urlTable.getColumnModel().getColumn(2).setPreferredWidth(160);
    }

    /**
     * 切换数据库服务
     * @param chose 选择
     * @param arg 数据库配置信息
     */
    private void switchDatabaseServer(int chose, String... arg){
        BurpExtender.db.switchConn(chose);
        switch (chose){
            case DBUtil.MYSQL_DB:
                BurpExtender.db.setConn(arg[0], arg[1], arg[2], arg[3], arg[4]);
                break;
            case DBUtil.SQLITE_DB:
                BurpExtender.db.setConn();
                break;
            default:
                break;
        }
    }
    public boolean autoConnectDatabaseByConfig(){
        if(Config.isBuild()){
            try{
                if(!"".equals(BurpExtender.config.get("db_server"))){
                    int chose = Arrays.binarySearch(ControlSwitch.DB_SERVER,BurpExtender.config.get("db_server"));
                    String host = BurpExtender.config.get("host");
                    String port = BurpExtender.config.get("port");
                    String username = BurpExtender.config.get("username");
                    String password = BurpExtender.config.get("password");
                    String database = BurpExtender.config.get("database");
                    switchDatabaseServer(chose,host,port,username,password,database);
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
            if(BurpExtender.db.projectExist(currentProject)){
                clearUI();
                label2.setText(currentProject);
                rootDomainSetting.setEnabled(true);
                BurpExtender.currentRootDomainSet = BurpExtender.db.getRootDomainSet(currentProject);
                if(BurpExtender.currentRootDomainSet.size() > 0){
                    searchButton.setEnabled(true);
                    BurpExtender.subDomainMap = BurpExtender.db.getSubDomainMap(currentProject);
                    BurpExtender.urlMap = BurpExtender.db.getUrlMap(currentProject);
                    BurpExtender.similarSubDomainMap = BurpExtender.db.getSimilarSubDomainMap(currentProject);
                    BurpExtender.similarUrlMap = BurpExtender.db.getSimilarUrlMap(currentProject);
                    for(Map.Entry<String, String> entry: BurpExtender.urlMap.entrySet()){
                        BurpExtender.urlCount += 1;
                        String createTime = entry.getValue();
                        addURLToUI(entry.getKey(), createTime);
                    }
                    for(Map.Entry<String, String> entry: BurpExtender.similarUrlMap.entrySet()){
                        BurpExtender.similarUrlCount += 1;
                        String createTime = entry.getValue();
                        addSimilarUrlToUI(entry.getKey(), createTime);
                    }
                    for(Map.Entry<String, HashMap<String, String>> entry: BurpExtender.subDomainMap.entrySet()){
                        BurpExtender.subDomainCount += 1;
                        HashMap<String, String> value = entry.getValue();
                        addSubDomainToUI(entry.getKey(), value.get("ipAddress"), value.get("createTime"));
                    }
                    for(Map.Entry<String, HashMap<String, String>> entry: BurpExtender.similarSubDomainMap.entrySet()){
                        BurpExtender.similarSubDomainCount += 1;
                        HashMap<String, String> value = entry.getValue();
                        addSimilarSubDomainToUI(entry.getKey(), value.get("ipAddress"), value.get("createTime"));
                    }
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
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JLabel label6;
    private JLabel label7;
    private JScrollPane scrollPane2;
    private JTable subDomainTable;
    private JScrollPane scrollPane3;
    private JTable urlTable;
    private JPanel panel3;
    private JLabel label4;
    private JLabel label9;
    private JScrollPane scrollPane1;
    private JTable similarSubDomainTable;
    private JScrollPane scrollPane4;
    private JTable similarUrlsTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    private static DefaultTableModel subDomainModel;
    private static DefaultTableModel urlModel;
    private static DefaultTableModel similarSubDomainModel;
    private static DefaultTableModel similarUrlModel;
    private static final String[] subDomainColumnNames = {"#", "domain", "IP", "Time"};
    private static final String[] urlColumnNames = {"#", "URL", "Time"};

}