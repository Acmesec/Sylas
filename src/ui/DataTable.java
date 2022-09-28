/*
 * Created by JFormDesigner on Tue Sep 27 17:00:18 CST 2022
 */

package ui;

import burp.BurpExtender;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * @author 0chencc
 */
public class DataTable extends JTable {
    final static String EXCEL_SUFFIX_NAME = ".xls";
    String tableTittle;
    public DataTable(String tableTittle) {
        initComponents();
        this.tableTittle = tableTittle;
    }

    private void thisMouseClicked(MouseEvent e) {
        // TODO add your code here
        if (e.getButton() == MouseEvent.BUTTON3){
            popupMenu1.show(e.getComponent(),e.getX(),e.getY());
        }
    }

    private void exportData(ActionEvent e) {
        // TODO add your code here
        DefaultTableModel defaultTableModel = (DefaultTableModel) this.getModel();
        JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jFileChooser.setSelectedFile(new File(tableTittle+"SylasExportFile.xls"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel (.xls)","xls");
        jFileChooser.setFileFilter(filter);
        int s = jFileChooser.showDialog(null,"Save");
        if (s == JFileChooser.APPROVE_OPTION){
            try(HSSFWorkbook hssfWorkbook = new HSSFWorkbook()){
                HSSFSheet sheet = hssfWorkbook.createSheet("Sylas");
                int[] selectRows = this.getSelectedRows();
                int columns = this.getColumnCount();
                int rows = selectRows.length;
                for (int row=0;row<rows;row++){
                    HSSFRow row_tmp = sheet.createRow(row);
                    for (int col=0;col<columns;col++){
                        row_tmp.createCell(col).setCellValue(defaultTableModel.getValueAt(selectRows[row],col).toString());
                    }
                }
                String xlsSavePath = jFileChooser.getSelectedFile().toString();
                if (!xlsSavePath.endsWith(EXCEL_SUFFIX_NAME)){
                    xlsSavePath = xlsSavePath+EXCEL_SUFFIX_NAME;
                }
                hssfWorkbook.write(new File(xlsSavePath));
                BurpExtender.getStdout().println(xlsSavePath);
            }catch (Exception e1){
                BurpExtender.getStderr().println(e1);
            }
        }
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        popupMenu1 = new JPopupMenu();
        export = new JMenuItem();

        //---- this ----
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                thisMouseClicked(e);
            }
        });

        //======== popupMenu1 ========
        {

            //---- export ----
            export.setText("Export data to ..");
            export.addActionListener(e -> exportData(e));
            popupMenu1.add(export);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPopupMenu popupMenu1;
    private JMenuItem export;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}