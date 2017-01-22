package scratchduino.robot.drafts;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import scratchduino.robot.ui.v1.*;



public class TableTest extends JFrame{
   public TableTest(){
      ImageIcon aboutIcon = new ImageIcon(JFrame.class.getResource("/green.png"));
      ImageIcon addIcon   = new ImageIcon(JFrame.class.getResource("/green.png"));
      ImageIcon copyIcon  = new ImageIcon(JFrame.class.getResource("/green.png"));

      String[] columnNames = { "A", "B", "C" };
      Object[][] data = { { aboutIcon, "About", "a"},
                          { addIcon,   "Add",   "b"},
                          { copyIcon, "Copy",   "c"} };

      DefaultTableModel model = new DefaultTableModel(data, columnNames);
      JTable table = new JTable(model){
         // Returning the Class of each column will allow different
         // renderers to be used based on Class
         public Class getColumnClass(int column){
            return getValueAt(0, column).getClass();
         }
      };

      table.getColumn("C").setCellRenderer(new ButtonRenderer());
      table.setPreferredScrollableViewportSize(table.getPreferredSize());
      //table.setRowMargin(10);
      //table.setIntercellSpacing(new Dimension(10, 10));
      table.setRowHeight(0, 22);      
      table.setRowHeight(1, 22);      
      table.setRowHeight(2, 22);
      
      table.getColumnModel().getColumn(0).setPreferredWidth(0);      
      

      JScrollPane scrollPane = new JScrollPane(table);
      getContentPane().add(scrollPane);
      
      DefaultTableModel model2 = (DefaultTableModel) table.getModel();
      model2.addRow(new Object[]{aboutIcon, "About", "a"});      
   }
   
   
   
   
   
   
   class ButtonRenderer extends JButton implements TableCellRenderer {

      public ButtonRenderer() {
        setOpaque(true);
      }

      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(isSelected){
          setForeground(table.getSelectionForeground());
          setBackground(table.getSelectionBackground());
        }
        else{
          setForeground(table.getForeground());
          setBackground(UIManager.getColor("Button.background"));
        }
        setText((value == null) ? "" : value.toString());
        
        setBorder(new EmptyBorder(20, 20, 20, 20));
        this.setSize(5,5);
        return this;
      }
    }   


   



   public static void main(String[] args){
      InterfaceHelper.setLookAndFeel();
      
      TableTest frame = new TableTest();
      frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }

}