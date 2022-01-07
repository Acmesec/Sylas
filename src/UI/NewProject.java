/*
 * Created by JFormDesigner on Mon Jan 03 16:50:09 CST 2022
 */

package UI;

import burp.BurpExtender;

import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import javax.swing.*;

/**
 * @author linchen
 */
public class NewProject extends JPanel {
    public static final int PROJECT_MODE = 0;
    public static final int DOMAIN_MODE = 1;
    public NewProject(int mode) {
        this.mode = mode;
        initComponents();
    }
    public void loadData(){
        if(BurpExtender.db.isConnect){
            HashSet<String> set;
            switch (mode){
                case PROJECT_MODE:
                    set = BurpExtender.db.getProjectSet();
                    break;
                case DOMAIN_MODE:
                    set = BurpExtender.db.getRootDomainSet(BurpExtender.config.get("currentProject"));
                    break;
                default:
                    set = new HashSet<>();
                    break;
            }
            for (String s : set) {
                list.addElement(s);
            }
        }
    }
    private void addActionPerformed(ActionEvent e) {
        // TODO add your code here
        String item = inputTextFiled.getText().trim();
        switch (mode){
            case PROJECT_MODE:
                HashSet<String> projectSet = BurpExtender.db.getProjectSet();
                if(!"".equals(item) && !projectSet.contains(item) && !list.contains(item)){
                    list.addElement(item);
                    BurpExtender.db.addProject(item);
                }
                break;
            case DOMAIN_MODE:
                if(!"".equals(item) && !BurpExtender.currentRootDomainSet.contains(item) && !list.contains(item)){
                    list.addElement(item);
                    BurpExtender.currentRootDomainSet.add(item);
                    BurpExtender.db.addRootDomain(BurpExtender.config.get("currentProject"), item);
                }
                break;
            default:
                break;
        }
        inputTextFiled.setText("");
    }

    private void remove(ActionEvent e) {
        // TODO add your code here
        String item = itemList.getSelectedValue();
        switch(mode){
            case PROJECT_MODE:
                HashSet<String> projectSet = BurpExtender.db.getProjectSet();
                if(!"".equals(item) && projectSet.contains(item) && list.contains(item)){
                    list.removeElement(item);
                    BurpExtender.db.removeProject(item);
                }
                break;
            case DOMAIN_MODE:
                if(!"".equals(item) && BurpExtender.currentRootDomainSet.contains(item) && list.contains(item)){
                    list.removeElement(item);
                    BurpExtender.currentRootDomainSet.remove(item);
                    BurpExtender.db.removeRootDomain(BurpExtender.config.get("currentProject"), item);
                }
                break;
            default:
                break;
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        scrollPane1 = new JScrollPane();
        itemList = new JList<>();
        inputTextFiled = new JTextField();
        remove = new JButton();
        add = new JButton();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 1.0E-4};

        //---- label1 ----
        label1.setText("NewProject Name");
        add(label1, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 5, 0), 0, 0));

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(itemList);
        }
        add(scrollPane1, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));
        add(inputTextFiled, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- remove ----
        remove.setText("Remove");
        remove.addActionListener(e -> remove(e));
        add(remove, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- add ----
        add.setText("Add");
        add.addActionListener(e -> addActionPerformed(e));
        add(add, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
        label1.setText(mode==PROJECT_MODE?"Project Name":"RootDomain Name");
        itemList.setModel(list);
        loadData();
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JScrollPane scrollPane1;
    public JList<String> itemList;
    public JTextField inputTextFiled;
    private JButton remove;
    public JButton add;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public DefaultListModel<String> list = new DefaultListModel<String>();
    private int mode;

}
