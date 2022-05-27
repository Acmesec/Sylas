/*
 * Created by JFormDesigner on Fri May 27 15:01:28 CST 2022
 */

package ui;

import java.awt.*;
import javax.swing.*;

/**
 * @author unknown
 */
public class CopyList extends JPanel {
    public CopyList() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        checkBox1 = new JCheckBox();
        button1 = new JButton();
        scrollPane1 = new JScrollPane();
        list1 = new JList<>();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 51, 0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

        //---- checkBox1 ----
        checkBox1.setText("select all");
        add(checkBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- button1 ----
        button1.setText("copy");
        add(button1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //======== scrollPane1 ========
        {

            //---- list1 ----
            list1.setModel(new AbstractListModel<String>() {
                String[] values = {
                    "1",
                    "2",
                    "3",
                    "4",
                    "5"
                };
                @Override
                public int getSize() { return values.length; }
                @Override
                public String getElementAt(int i) { return values[i]; }
            });
            scrollPane1.setViewportView(list1);
        }
        add(scrollPane1, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JCheckBox checkBox1;
    private JButton button1;
    private JScrollPane scrollPane1;
    private JList<String> list1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
