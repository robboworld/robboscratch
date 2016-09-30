package scratchduino.robot.ui.v1;

import java.awt.*;
import javax.swing.*;
import org.apache.commons.logging.*;

public final class InterfaceHelper{
   private static Log log = LogFactory.getLog(InterfaceHelper.class);
   private static final String LOG = "[InterfaceHelper] ";


   private static final ImageIcon iconWating;
   private static JDialog dlgWaiting = null;


   static{
      iconWating = new ImageIcon(JFrame.class.getResource("/loaderB32.gif"));
   }







   public static void setLookAndFeel(){
      // try {
      // for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      // if ("tiny".contains(info.getName().toLowerCase())) {
      // UIManager.setLookAndFeel(info.getClassName());
      // break;
      // }
      // }
      // } catch (Exception e) {
      // // If Nimbus is not available, you can set the GUI to another look and
      // feel.
      // }

      Toolkit.getDefaultToolkit().setDynamicLayout(true);
      System.setProperty("sun.awt.noerasebackground", "true");
      JFrame.setDefaultLookAndFeelDecorated(true);
      JDialog.setDefaultLookAndFeelDecorated(true);

      //FrameLucy.setDefaultLookAndFeelDecorated(true);

      try{
         UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
      }
      catch (Exception e){
         log.error(LOG, e);
      }
   }




   public static void showSecondInstance() {
      SwingUtilities.invokeLater(new Runnable(){
         public void run(){
            JOptionPane.showMessageDialog(null,
                     "Launched a second instance of the programm.",
                     "Error",
                     JOptionPane.ERROR_MESSAGE);         }
      });
   }









   public static void showWaiting(final String sMessage){
      if(SwingUtilities.isEventDispatchThread()){
         _showWating(sMessage);
      }
      else{
         SwingUtilities.invokeLater(new Runnable(){
            public void run(){
               _showWating(sMessage);
            }
         });
      }
   }
   private static void _showWating(String sMessage){
      dlgWaiting = new JDialog();

      JPanel panel = new JPanel();

      // panel.setBackground(new java.awt.Color(230, 230, 255));

      JLabel jLabel = new JLabel(sMessage);
      jLabel.setIcon(iconWating);
      panel.add(jLabel);
      dlgWaiting.add(panel);
      dlgWaiting.setModalExclusionType(JDialog.ModalExclusionType.TOOLKIT_EXCLUDE);

      dlgWaiting.setUndecorated(true);
      dlgWaiting.getRootPane().setWindowDecorationStyle(JRootPane.NONE);

      dlgWaiting.setSize(130, 45);
      dlgWaiting.setAlwaysOnTop(true);
      dlgWaiting.setLocationRelativeTo(null);
      dlgWaiting.setResizable(false);

      panel.setBorder(BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100)));
      dlgWaiting.setVisible(true);
   }










   public static void hideWaiting(){
      if(SwingUtilities.isEventDispatchThread()){
         _hideWaiting();
      }
      else{
         SwingUtilities.invokeLater(new Runnable(){
            public void run(){
               _hideWaiting();
            }
         });
      }
   }
   private static void _hideWaiting(){
      if(dlgWaiting == null){
      }
      else{
         dlgWaiting.setVisible(false);
         dlgWaiting = null;
      }
   }
}
