package scratchduino.robot.ui.v1;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.lang.reflect.*;
import java.math.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import org.apache.commons.logging.*;
import scratchduino.robot.*;



public class ControlPanel extends JFrame implements IControlPanel{
   private static final long serialVersionUID = -8510057476499416417L;
   
   private static Log log = LogFactory.getLog(ControlPanel.class);
   private static final String LOG = "[ControlPanel] ";
   
   private static final ImageIcon iconWating;
   private static final ImageIcon iconLogo;
   
   private static final ImageIcon iconRed;
   private static final ImageIcon iconYellow;
   private static final ImageIcon iconGreen;
   
   private static final int FRAME_WIDTH  = 640;
   private static final int FRAME_HEIGHT = 400;
   
   
   
   private static final JDialog dlgWaiting = new JDialog();
   
   
   
   public static final TrayIcon trayIcon;
   public static final PopupMenu popup;
   
   
   protected final AtomicBoolean bFirstRun = new AtomicBoolean(true);
   
   
   private final String TEMP_FILE_NAME = "scratch_autosave_21.sb2";

   
   
   static{
      popup = new PopupMenu();
      Image image = Toolkit.getDefaultToolkit().getImage(JFrame.class.getResource("/loaderB32.gif"));
      
   
      trayIcon = new TrayIcon(image, "", popup);
   }

   
   
   static{
      iconWating = new ImageIcon(JFrame.class.getResource("/loaderB32.gif"));
      iconLogo   = new ImageIcon(JFrame.class.getResource("/robot.png"));
      
      iconRed    = new ImageIcon(JFrame.class.getResource("/red.png"));
      iconYellow = new ImageIcon(JFrame.class.getResource("/yellow.png"));
      iconGreen  = new ImageIcon(JFrame.class.getResource("/green.png"));
      
      
      JPanel panel = new JPanel();
      
      //panel.setBackground(new java.awt.Color(230, 230, 255));
      
      JLabel jLabel = new JLabel(" Please wait...");
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
   }
   
   static{
      // We need ".", not "," in numbers
      //Locale.setDefault(Locale.US);
      
      InterfaceHelper.setLookAndFeel();
   }
   

   private final IDeviceLocator locator;
   private final IConfiguration config;
   private final IFirmware firmware;
   
   private JTable tblComPortList;
   private JScrollPane scrollableList;   
   private JButton btnFind;
   private JCheckBox cbAutoFind;
   private JButton btnDiagnostic;
   private JButton btnExit;
   
   private String bestDeviceIcon = null;
   
   private enum STATE{NO_DEVICE, IN_PROGRESS, READY, WRONG_VERSION};
   
   private volatile STATE state = STATE.NO_DEVICE;
   
   private Map<String, JLabel>  mapStatuses = new HashMap<String, JLabel>();
   private Map<String, JButton> mapFirmwareButtons = new HashMap<String, JButton>();
   
   
   
   private volatile String loadFileName = null;
   private volatile String saveFileName = null;
   private volatile File   selectedFile = null;
   private volatile byte[] loadFileData = null;
   

   public ControlPanel(IConfiguration config, IDeviceLocator locator, IFirmware firmware) throws InvocationTargetException, InterruptedException{
      this.locator  = locator;
      this.config   = config;
      this.firmware = firmware;
      

      if(this.config.getIOS().getType() == IOS.TYPE.MAC){
         try{
            com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
            Image image = ImageIO.read(JFrame.class.getResource("/robbo-control-panel.png"));
            application.setDockIconImage(image);
         }
         catch (IOException e){
            throw new Error(e);
         }
      }      

      SwingUtilities.invokeAndWait(new Runnable(){
         public void run(){
            ControlPanel.this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
            ControlPanel.this.setResizable(false);
            ControlPanel.this.setLocationRelativeTo(null);
            ControlPanel.this.setAlwaysOnTop(true);
            
            ControlPanel.this.setLayout(null);
         
            if(ControlPanel.this.config.getIOS().getType() == IOS.TYPE.MAC){
               setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
               ControlPanel.this.addWindowListener(new WindowAdapter(){
                  public void windowClosing(WindowEvent e){
                     ControlPanel.this.setState(Frame.ICONIFIED);
                  }
               });
            }      
            
            
            
            JPanel panel = new JPanel();
            panel.setLayout(null);
            panel.setBackground(new Color(220, 220, 220));
            panel.setSize(640, 55);
            panel.setBounds(0,0, 640, 55);
      
            JLabel lbTitleIcon = new JLabel();
            lbTitleIcon.setIcon(iconLogo);
            lbTitleIcon.setBounds(15, 3, 640, 45);
            panel.add(lbTitleIcon);
            ControlPanel.this.add(panel);
            
            
            JLabel labelTitleText = new JLabel(ControlPanel.this.config.i18n("title").replaceAll("%v", ControlPanel.this.config.getVersion()));
            labelTitleText.setFont(new Font("Arial", Font.CENTER_BASELINE, 12));
            labelTitleText.setBounds(420, 18, 640, 45);
            panel.add(labelTitleText);
            
            
            
            JLabel lbDeviceList = new JLabel(ControlPanel.this.config.i18n("label_device_list"));
            lbDeviceList.setBounds(20, 60, 595, 20);
            ControlPanel.this.add(lbDeviceList);
            
      
            String[] columnNames = {" ", "  ", "   "};
            DefaultTableModel model = new DefaultTableModel(new Object[][]{}, columnNames){
               private static final long serialVersionUID = -8731419365530875480L;
      
               @Override
               public boolean isCellEditable(int row, int column) {
                  if(column == 2){
                     return true;
                  }
                  return false;
               }
           };      
            
            
            tblComPortList = new JTable(model){
               private static final long serialVersionUID = 9050870870092600975L;
      
               public Class<?> getColumnClass(int column){
                  return getValueAt(0, column).getClass();
               }
            };
            tblComPortList.getColumnModel().getColumn(0).setPreferredWidth(0);
            tblComPortList.getColumnModel().getColumn(1).setPreferredWidth(350);
            tblComPortList.getColumn(" ").setCellRenderer(new LabelRenderer());
            tblComPortList.getColumn("  ").setCellRenderer(new LabelRenderer());
            tblComPortList.getColumn("   ").setCellRenderer(new ButtonRenderer());
            
            tblComPortList.getColumnModel().getColumn(2).setCellEditor(new ClientsTableRenderer(new JCheckBox()));
            
            
            tblComPortList.setCellSelectionEnabled(false);
            tblComPortList.setFocusable(false);
            tblComPortList.setBackground(new Color(232, 233, 237));
            tblComPortList.setGridColor(new Color(232, 233, 237));
            
            
            scrollableList = new JScrollPane(tblComPortList);
            scrollableList.setBorder(new LineBorder(new Color(150, 150, 150)));
            scrollableList.setBounds(20, 80, 595, 185);
            
            ControlPanel.this.add(scrollableList);
            
            
            btnFind = new JButton(ControlPanel.this.config.i18n("button_search"));
            btnFind.setFont(new Font("Arial", Font.BOLD, 13));
            btnFind.setBorder(new TextBubbleBorder(new Color(150, 150, 150), 1, 8,0));
            btnFind.setBounds(30, 280, 520, 35);
            btnFind.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent paramActionEvent){
                  reconnect();
               }
            });
            ControlPanel.this.add(btnFind);
            
            
            cbAutoFind = new JCheckBox("auto");
            cbAutoFind.setBounds(560, 280, 100, 35);
            //cbAutoFind.setSelected(true);
            ControlPanel.this.add(cbAutoFind);
            
            
            btnDiagnostic = new JButton(ControlPanel.this.config.i18n("button_diagnostic"));
            btnDiagnostic.setFont(new Font("Arial", Font.BOLD, 12));
            btnDiagnostic.setBorder(new TextBubbleBorder(new Color(150, 150, 150), 1, 8,0));
            btnDiagnostic.setBounds(30, 322, 280, 28);
            btnDiagnostic.setEnabled(false);
            ControlPanel.this.add(btnDiagnostic);
      
            btnExit = new JButton(ControlPanel.this.config.i18n("button_exit"));
            btnExit.setFont(new Font("Arial", Font.BOLD, 12));
            btnExit.setBorder(new TextBubbleBorder(new Color(150, 150, 150), 1, 8,0));
            btnExit.setBounds(325, 322, 280, 28);
            btnExit.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent paramActionEvent){
                  //System.exit(0);
                  ControlPanel.this.setState(Frame.ICONIFIED);
               }
            });
            ControlPanel.this.add(btnExit);
            
            
            
      
            //Tray
            if(SystemTray.isSupported()){
      
               SystemTray tray = SystemTray.getSystemTray();
      
               MouseListener mouseListener = new MouseListener(){
      
                  public void mouseClicked(MouseEvent event){
                     // System.out.println("Tray Icon - Mouse clicked!");
                  }
      
                  public void mouseEntered(MouseEvent event){
                     // System.out.println("Tray Icon - Mouse entered!");
                  }
      
                  public void mouseExited(MouseEvent event){
                     // System.out.println("Tray Icon - Mouse exited!");
                  }
      
                  public void mousePressed(MouseEvent event){
                     // System.out.println("Tray Icon - Mouse pressed!");
                     ControlPanel.this.popUp();
                  }
      
                  public void mouseReleased(MouseEvent e){
                     // System.out.println("Tray Icon - Mouse released!");
                  }
               };
      
               ActionListener exitListener = new ActionListener(){
                  public void actionPerformed(ActionEvent e){
                     System.out.println("Exiting...");
                     System.exit(0);
                  }
               };
      
      
               ActionListener actionListener = new ActionListener(){
                  public void actionPerformed(ActionEvent e){
                     //trayIcon.displayMessage("Action Event", "An Action Event Has Been Performed!", TrayIcon.MessageType.INFO);
                  }
               };
               
               MenuItem defaultItem = new MenuItem(ControlPanel.this.config.i18n("tray_menu_exit"));
               defaultItem.addActionListener(exitListener);
               popup.add(defaultItem);
               
      
               trayIcon.setImageAutoSize(true);
               trayIcon.addActionListener(actionListener);
               trayIcon.addMouseListener(mouseListener);
      
               try{
                  tray.add(trayIcon);
               }
               catch (AWTException e){
                  System.err.println("TrayIcon could not be added.");
               }
      
            }
            else{
               // System Tray is not supported
            }
            
            ControlPanel.this.revalidate();
            ControlPanel.this.setVisible(true);
         }
      });
      

      AutoFinder af = new AutoFinder();
      af.start();
      
      FramePositionChecker fpc = new FramePositionChecker();
      fpc.start();
      
      checkNewVersion();
      
      
      
      loadFileData = dialogOpenTmp();
      Context.ctx.getBean("flash", IFlash.class).run();      
   }


   
   
      
   private class AutoFinder extends Thread{
      private boolean bFirstRun = true;
      
      public void run(){
         while(true){
            if(cbAutoFind.isSelected()){
               synchronized(ControlPanel.this) {
                  if(state == STATE.NO_DEVICE){
                     reconnect();
                  }
               }
            }
            else{
               if(bFirstRun){
                  synchronized(ControlPanel.this) {
                     if(state == STATE.NO_DEVICE){
                        reconnect();
                     }
                  }
                  bFirstRun = false;
               }
            }
            
            try{
               Thread.sleep(1000);
            }
            catch (InterruptedException e){
               break;
            }
         }
      }
   }
   
   
   
   
   private class Finder extends Thread{
      
      public void run(){
         synchronized(ControlPanel.this) {
            state = STATE.IN_PROGRESS;
         }
         
         
         
         try{
            SwingUtilities.invokeAndWait(new Runnable(){
               public void run(){
                  btnFind.setEnabled(false);
                  btnFind.setIcon(iconWating);
               }
            });
         }
         catch (InvocationTargetException e){
            log.error(LOG, e);
            return;
         }
         catch (InterruptedException e){
            log.error(LOG, e);
            return;
         }

         
         if(mapStatuses.size() > 0){
            locator.stop();
            
            for(final IPort port : locator.getPortList()){
               SwingUtilities.invokeLater(new Runnable(){
                  public void run(){
                     mapFirmwareButtons.get(port.getPortName()).setEnabled(false);
                     mapStatuses.get(port.getPortName()).setText(printStatus(port).getText());
                     mapStatuses.get(port.getPortName()).setIcon(printStatus(port).getIcon());
                  }
               });
            }
            try{
               Thread.sleep(config.getPortCloseDelay());
            }
            catch (InterruptedException e){
            }
         }

         mapStatuses.clear();
         mapFirmwareButtons.clear();
         
         DefaultTableModel model = (DefaultTableModel) tblComPortList.getModel();         
         model.setRowCount(0);

         try{
            SwingUtilities.invokeAndWait(new Runnable(){
               public void run(){
                  tblComPortList.revalidate();
                  tblComPortList.repaint();
               }
            });
         }
         catch (InvocationTargetException e){
            log.error(LOG, e);
            return;
         }
         catch (InterruptedException e){
            log.error(LOG, e);
            return;
         }
         
         
         
         
         // Let's build status map
         locator.start();            
         
         for(final IPort port : locator.getPortList()){
            final JLabel lbPortStatus = printStatus(port);
            mapStatuses.put(port.getPortName(), lbPortStatus);
         }
         

            
         try{
            SwingUtilities.invokeAndWait(new Runnable(){
               public void run(){
                  
                  int iRowCount = 0;
                  for(final IPort port : locator.getPortList()){
                     
                     JLabel lbPortName = new JLabel(port.getPortName() + ":");
                     lbPortName.setPreferredSize(new Dimension(90, 12));
                     lbPortName.setFont(new Font("Arial", Font.BOLD, 12));
                     lbPortName.setForeground(new Color(0, 0, 255));
                     
                     
                     JLabel lbPortStatus = mapStatuses.get(port.getPortName());
                     lbPortStatus.setPreferredSize(new Dimension(350, 16));
                     lbPortStatus.setFont(new Font("Arial", Font.PLAIN, 11));
                     lbPortStatus.setForeground(new Color(0, 0, 0));
                     
                     
                     final JButton btnUploadFirmware = new JButton(ControlPanel.this.config.i18n("button_upload_firmware"));
                     btnUploadFirmware.addActionListener(new ClickFirmwareUpload(port));
                     mapFirmwareButtons.put(port.getPortName(), btnUploadFirmware);
                     
                     //tblComPortList.getColumn("").setCellRenderer(new ButtonRenderer(btnUploadFirmware));      
                     
                     
/*                     
                     final JPanel pnlPort = new JPanel(new FlowLayout(FlowLayout.LEFT));
                     pnlPort.setSize(593, 28);
                     pnlPort.setPreferredSize(new Dimension(593, 28));
                     pnlPort.setFocusable(true);
                     // pnlPort.setBorder(new LineBorder(new Color(150, 150,
                     // 150)));

                     JLabel lbPortName = new JLabel(port.getPortName() + ":");
                     lbPortName.setPreferredSize(new Dimension(90, 12));
                     lbPortName.setFont(new Font("Arial", Font.BOLD, 12));
                     lbPortName.setForeground(new Color(0, 0, 255));
                     pnlPort.add(lbPortName);

                     JLabel lbPortStatus = mapStatuses.get(port.getPortName());
                     lbPortStatus.setPreferredSize(new Dimension(350, 16));
                     lbPortStatus.setFont(new Font("Arial", Font.PLAIN, 11));
                     lbPortStatus.setForeground(new Color(0, 0, 0));
                     pnlPort.add(lbPortStatus);

                     final JButton btnUploadFirmware = new JButton(ControlPanel.this.config.i18n("button_upload_firmware"));
                     btnUploadFirmware.addActionListener(new ClickFirmwareUpload(port));
                     mapFirmwareButtons.put(port.getPortName(), btnUploadFirmware);
                     
                     btnUploadFirmware.setEnabled(false);
                     pnlPort.add(btnUploadFirmware);

                     pnlPort.setBounds(0, iRowCount * 30, 595, 30);
                     pnlComPortList.add(pnlPort);
*/
                     
                     
                     DefaultTableModel model = (DefaultTableModel) tblComPortList.getModel();
                     
                     model.addRow(new Object[]{lbPortName,
                                               lbPortStatus,
                                               btnUploadFirmware});
                     
                     
                     tblComPortList.setRowHeight(iRowCount, 22);                   
                     
                     
                     
                     iRowCount++;
                  }
               }
            });
         }
         catch (InvocationTargetException e){
            log.error(LOG, e);
            return;
         }
         catch (InterruptedException e){
            log.error(LOG, e);
            return;
         }
         
         while (ControlPanel.this.locator.getStatus() != IDeviceLocator.STATUS.READY){
            
            int iPortNumber = 0;
            for(final IPort port : locator.getPortList()){
               
               final int iPortNumber_ = iPortNumber; 
               
               SwingUtilities.invokeLater(new Runnable(){
                  public void run(){
                     if(port.getStatus() == IPort.STATUS.ROBOT_DETECTED /*&& bFirstRun.get()*/){
                        //ControlPanel.this.setVisible(false);
                        bFirstRun.getAndSet(false);
                     }
                     
                     
                     if(port.getStatus() == IPort.STATUS.WRONG_VERSION ||
                        port.getStatus() == IPort.STATUS.UNKNOWN_DEVICE ||
                        port.getStatus() == IPort.STATUS.TIME_OUT){
                        
                        mapFirmwareButtons.get(port.getPortName()).setEnabled(true);
                     }
                     
                     mapStatuses.get(port.getPortName()).setText(printStatus(port).getText());
                     mapStatuses.get(port.getPortName()).setIcon(printStatus(port).getIcon());
                     
                     tblComPortList.getModel().setValueAt(mapStatuses.get(port.getPortName()), iPortNumber_, 1);
                     tblComPortList.revalidate();
                     tblComPortList.repaint();                     
                  }
               });
               
               iPortNumber++;
            }
            
            
            try{
               Thread.sleep(100);
            }
            catch (InterruptedException e){
               break;
            }
            if(locator.getStatus() == IDeviceLocator.STATUS.READY){
               break;
            }
         }
         
         int iPortNumber = 0;
         for(final IPort port : locator.getPortList()){
            
            final int iPortNumber_ = iPortNumber; 
            
            SwingUtilities.invokeLater(new Runnable(){
               public void run(){
                  if(port.getStatus() == IPort.STATUS.ROBOT_DETECTED /*&& bFirstRun.get()*/){
                     //ControlPanel.this.setVisible(false);
                     bFirstRun.getAndSet(false);

//                     Context.ctx.getBean("flash", IFlash.class).run(port.getDevice().getType());
                  }
                  
                  if(port.getStatus() == IPort.STATUS.WRONG_VERSION ||
                     port.getStatus() == IPort.STATUS.UNKNOWN_DEVICE ||
                     port.getStatus() == IPort.STATUS.TIME_OUT ||
                     port.getStatus() == IPort.STATUS.ROBOT_DETECTED){
                     mapFirmwareButtons.get(port.getPortName()).setEnabled(true);
                  }
                  
                  mapStatuses.get(port.getPortName()).setText(printStatus(port).getText());
                  mapStatuses.get(port.getPortName()).setIcon(printStatus(port).getIcon());
                  tblComPortList.getModel().setValueAt(mapStatuses.get(port.getPortName()), iPortNumber_, 1);
                  
                  tblComPortList.revalidate();
                  tblComPortList.repaint();                    
               }
            });
            
            iPortNumber++;
         }

         SwingUtilities.invokeLater(new Runnable(){
            public void run(){
               btnFind.setEnabled(true);
               btnFind.setIcon(null);
            }
         });
         

         synchronized(ControlPanel.this){
            if(state == STATE.READY || state == STATE.WRONG_VERSION){
            }
            else{
               state = STATE.NO_DEVICE;
            }
         }
      }
   }
   
   
   protected JLabel printStatus(IPort port){      
      switch (port.getStatus()){
         case INIT:{
            JLabel lb = new JLabel(ControlPanel.this.config.i18n("port_state_init"));
            lb.setIcon(iconYellow);
            updateIcon(iconYellow);
            return lb;
         }
         case TIME_OUT:{
            JLabel lb = new JLabel(ControlPanel.this.config.i18n("port_state_timeout"));
            lb.setIcon(iconRed);
            updateIcon(iconRed);
            return lb;
         }
         case ERROR:{
            JLabel lb = new JLabel(ControlPanel.this.config.i18n("port_state_error"));
            lb.setIcon(iconRed);
            updateIcon(iconRed);
            return lb;
         }
         case OPENNED:{
            JLabel lb = new JLabel(ControlPanel.this.config.i18n("port_state_opened") + " " + port.getSpeed());
            lb.setIcon(iconYellow);
            updateIcon(iconYellow);
            return lb;
         }
         case TEST_DATA:{
            JLabel lb = new JLabel(ControlPanel.this.config.i18n("port_state_data_sent") + " " + port.getSpeed());
            lb.setIcon(iconYellow);
            updateIcon(iconYellow);
            return lb;
         }
         case RESPONSE:{
            JLabel lb = new JLabel(ControlPanel.this.config.i18n("port_state_analizing_response"));
            lb.setIcon(iconYellow);
            updateIcon(iconYellow);
            return lb;
         }
         case NO_RESPONSE:{
            break;
         }
         case ROBOT_DETECTED:{
            JLabel lb = new JLabel(format_id_string(ControlPanel.this.config.i18n("port_state_robot_ok"), port.getDevice()));
            lb.setIcon(iconGreen);
            updateIcon(iconGreen);
            
            state = STATE.READY;
            return lb;
         }
         case WRONG_VERSION:{
            JLabel lb = new JLabel(format_id_string(ControlPanel.this.config.i18n("port_state_wrong_version"), port.getDevice()));
            lb.setIcon(iconYellow);
            updateIcon(iconYellow);
            
            state = STATE.WRONG_VERSION;
            return lb;
         }
         case UNKNOWN_DEVICE:{
            JLabel lb = new JLabel(ControlPanel.this.config.i18n("port_state_unknown_device"));
            lb.setIcon(iconRed);
            updateIcon(iconRed);
            return lb;
         }
         case TERMINATING:{
            JLabel lb = new JLabel(ControlPanel.this.config.i18n("port_state_closing"));
            lb.setIcon(iconRed);
            updateIcon(iconRed);
            return lb;
         }
         case TERMINATED:{
            JLabel lb = new JLabel(ControlPanel.this.config.i18n("port_state_closing"));
            lb.setIcon(iconRed);
            updateIcon(iconRed);
            return lb;
         }
         
      }
      
      JLabel lb = new JLabel("---");
      lb.setIcon(iconYellow);
      trayIcon.setImage(iconYellow.getImage());

      return lb;
   }
   
   
   
   
   private String format_id_string(String str, IConnectedDevice device){
      try{
         return str.replaceAll("%s", "" + device.getSerialCompacted())
                   .replaceAll("%f", String.format("%04d", device.getFirmwareVersion()));
      }
      catch (Exception e){
         return "unknown device type=" + device.getType();
      }
   }
   
   
   
   
   private void updateIcon(ImageIcon icon){
      if(bestDeviceIcon == null){
         //no status yet
         trayIcon.setImage(icon.getImage());
      }
      else if(bestDeviceIcon.equals(iconGreen.toString())){
         //At least one robot is ok!
         //We do not need to set it yellow or red
      }
      else if(bestDeviceIcon.equals(iconYellow.toString())){
         if(icon.toString().equals(iconGreen.toString())){
            //ok, one is red-> yellow or red-> green now
            trayIcon.setImage(icon.getImage());
         }
         else{
         }
      }
      else if(bestDeviceIcon.equals(iconRed.toString())){
         if(icon.toString().equals(iconGreen.toString()) || icon.getImage().equals(iconGreen.toString())){
            //ok, one is red-> yellow or red-> green now
            trayIcon.setImage(icon.getImage());
         }
      }
      
      bestDeviceIcon = icon.toString();
   }
   
   

   
   public void popUp(){
      SwingUtilities.invokeLater(new Runnable(){
         public void run(){
            ControlPanel.this.setVisible(true);
            int state = getExtendedState();
            state &= ~JFrame.ICONIFIED;
            ControlPanel.this.setExtendedState(state);
            ControlPanel.this.setAlwaysOnTop(true);
            ControlPanel.this.toFront();
            ControlPanel.this.requestFocus();
         }
      });
   }   




   class ClickFirmwareUpload  implements ActionListener{
      private final IPort port;
      

      public ClickFirmwareUpload(IPort port){
         this.port = port;
      }




      @Override
      public void actionPerformed(ActionEvent arg0){
         FirmwareUploader uploader = new FirmwareUploader(port);
         uploader.start(); 
      }
   
   }
   


   
   
   class FirmwareUploader extends Thread{
      private final IPort port;
        

      public FirmwareUploader(IPort port){
         this.port = port;
      }




      @Override
      public void run(){      
         try{
            SwingUtilities.invokeAndWait(new Runnable(){
               public void run(){
                  ControlPanel.this.setVisible(false);
                  //ControlPanel.this.setState(Frame.ICONIFIED);               
               }
            });
         }
         catch (InvocationTargetException e1){
         }
         catch (InterruptedException e1){
         }
         

         
         String sFirmwareQuestion = ControlPanel.this.config.i18n("dialog_confirm_update_firmware")
                                                            .replaceAll("%p", "" + port.getPortName()); 
         
         IConnectedDevice device = port.getDevice();
         
         if(device == null){
            sFirmwareQuestion = sFirmwareQuestion.replaceAll("%f", config.getVersion()); 
         }
         else{
            sFirmwareQuestion = sFirmwareQuestion.replaceAll("%f", String.format("%04d", locator.getDevices().getDevice(port.getDevice().getType()).getFirmware())); 
         }
         
      
         if(JOptionPane.showOptionDialog(null,
                  sFirmwareQuestion,
                  ControlPanel.this.config.i18n("are_you_sure"), 
                  JOptionPane.YES_NO_OPTION, 
                  JOptionPane.INFORMATION_MESSAGE, 
                  null, 
                  new String[]{ControlPanel.this.config.i18n("yes"), ControlPanel.this.config.i18n("no")}, // this is the array
                  "default") == JOptionPane.YES_OPTION){
         }
         else{
            try{
               SwingUtilities.invokeAndWait(new Runnable(){
                  public void run(){
                     ControlPanel.this.setVisible(true);
                     //ControlPanel.this.setState(Frame.ICONIFIED);               
                  }
               });
            }
            catch (InvocationTargetException e1){
            }
            catch (InterruptedException e1){
            }
            
            return;
         }
         
//         SwingUtilities.invokeLater(new Runnable(){
//            public void run(){
//               ControlPanel.this.setState(Frame.NORMAL);               
//               //ControlPanel.this.setVisible(true);
//               btnUpload.setEnabled(false);
//            }
//         });
         
         
//         lock(ControlPanel.this.config.i18n("message_firmwaring"));
         
         try{
            port.close();
            
            ControlPanel.this.firmware.uploadFirmware(port.getPortName());
         }
         catch (Throwable e){
            log.fatal(LOG, e);
         }

//         unlock();         
         ControlPanel.this.popUp();
         ControlPanel.this.reconnect();
      }
   }

   
   
   
   
   
   
   
   class TextBubbleBorder extends AbstractBorder {
      private static final long serialVersionUID = 5448483592139168866L;
      
      
      private Color color;
      private int thickness = 4;
      private int radii = 8;
      private int pointerSize = 7;
      private Insets insets = null;
      private BasicStroke stroke = null;
      private int strokePad;
      private int pointerPad = 4;
      RenderingHints hints;

      TextBubbleBorder(Color color){
          new TextBubbleBorder(color, 4, 8, 7);
      }

      TextBubbleBorder(
              Color color, int thickness, int radii, int pointerSize) {
          this.thickness = thickness;
          this.radii = radii;
          this.pointerSize = pointerSize;
          this.color = color;

          stroke = new BasicStroke(thickness);
          strokePad = thickness / 2;

          hints = new RenderingHints(
                  RenderingHints.KEY_ANTIALIASING,
                  RenderingHints.VALUE_ANTIALIAS_ON);

          int pad = radii + strokePad;
          int bottomPad = pad + pointerSize + strokePad;
          insets = new Insets(pad, pad, bottomPad, pad);
      }

      @Override
      public Insets getBorderInsets(Component c) {
          return insets;
      }

      @Override
      public Insets getBorderInsets(Component c, Insets insets) {
          return getBorderInsets(c);
      }

      @Override
      public void paintBorder(
              Component c,
              Graphics g,
              int x, int y,
              int width, int height) {

          Graphics2D g2 = (Graphics2D) g;

          int bottomLineY = height - thickness - pointerSize;

          RoundRectangle2D.Double bubble = new RoundRectangle2D.Double(
                  0 + strokePad,
                  0 + strokePad,
                  width - thickness,
                  bottomLineY,
                  radii,
                  radii);

          Polygon pointer = new Polygon();

          // left point
          pointer.addPoint(
                  strokePad + radii + pointerPad,
                  bottomLineY);
          // right point
          pointer.addPoint(
                  strokePad + radii + pointerPad + pointerSize,
                  bottomLineY);
          // bottom point
          pointer.addPoint(
                  strokePad + radii + pointerPad + (pointerSize / 2),
                  height - strokePad);

          Area area = new Area(bubble);
          area.add(new Area(pointer));

          g2.setRenderingHints(hints);

          Area spareSpace = new Area(new Rectangle(0, 0, width, height));
          spareSpace.subtract(area);
          g2.setClip(spareSpace);
          g2.clearRect(0, 0, width, height);
          g2.setClip(null);

          g2.setColor(color);
          g2.setStroke(stroke);
          g2.draw(area);
      }
   }


   @Override
   public void reconnect(){
      synchronized(this) {
         if(state == STATE.IN_PROGRESS) {
         }
         else{
            //Let's start
            Finder finder = new Finder();
            finder.start();
         }
      }
   }




   @Override
   public void lock(final String sMessage){
      SwingUtilities.invokeLater(new Runnable(){
         public void run(){
            btnFind.setEnabled(false);
            btnDiagnostic.setEnabled(false);
            btnExit.setEnabled(false);
            btnFind.setText(sMessage);
            btnFind.setIcon(iconWating);
            
            for(Component component : tblComPortList.getComponents()){
               component.setEnabled(false);
               
               if(component instanceof JPanel){
                  JPanel panel = (JPanel) component;
                  
                  for(Component component2 : panel.getComponents()){
                     component2.setEnabled(false);
                  }
               }
            }
         }
      });
   }
   @Override
   public void unlock(){
      SwingUtilities.invokeLater(new Runnable(){
         public void run(){
            btnFind.setEnabled(true);
            btnDiagnostic.setEnabled(true);
            btnExit.setEnabled(true);
            btnFind.setText(config.i18n("button_search"));
            btnFind.setIcon(null);
            
            for(Component component : tblComPortList.getComponents()){
               component.setEnabled(true);
               
               if(component instanceof JPanel){
                  JPanel panel = (JPanel) component;
                  
                  for(Component component2 : panel.getComponents()){
                     component2.setEnabled(true);
                  }
               }
            }
            
            btnDiagnostic.setEnabled(false);
         }
      });
   }
   
   
   private class FramePositionChecker extends Thread{
      private long lastTime = 0;
      private int iInterval = 250;
      
      private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      
      public void run(){
         while(true){
            if(System.currentTimeMillis() -  iInterval > lastTime){
               lastTime = System.currentTimeMillis();
               
               int iCurrentX = ControlPanel.this.getLocation().x;
               int iCurrentY = ControlPanel.this.getLocation().y;
               
               
               if(iCurrentX < 0) {
                  iCurrentX = 0;
               }
               if(iCurrentY < 0) {
                  switch(config.getIOS().getType()){
                     case MAC:{
                        iCurrentY = 50;
                        break;
                     }
                     default:{
                        iCurrentY = 0;
                        break;
                     }
                  }
               }
               if(iCurrentX > screenSize.width - FRAME_WIDTH) {
                  iCurrentX = screenSize.width - FRAME_WIDTH;
               }
               if(iCurrentY > screenSize.height - FRAME_HEIGHT) {
                  iCurrentY = screenSize.height - FRAME_HEIGHT;
               }
               
               ControlPanel.this.setLocation(iCurrentX, iCurrentY);
            }
            
            synchronized(this){
               try{
                  this.wait(iInterval);
               }
               catch (InterruptedException e){
                  return;
               }
            }
         }
      }
   }
   
   
   private void checkNewVersion(){
      try{
         log.info(LOG + "Check new version...");
         
         String sUpdateURL = config.getUpdateURL() + "/" + config.getVersion() + ".txt";
         log.trace(LOG + "UpdateURL=" + sUpdateURL);
         
         URL url = new URL(sUpdateURL);

         HttpURLConnection con = (HttpURLConnection) url.openConnection();
         int iResponseCode = con.getResponseCode();

         StringBuilder sb = new StringBuilder();
         if(iResponseCode == 200){
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null){
               sb.append(inputLine);
            }
            in.close();
         
            log.trace(LOG + sb.toString());
         
         
            if(JOptionPane.showOptionDialog(null, 
                     ControlPanel.this.config.i18n("dialog_update_open_browser"), 
                     ControlPanel.this.config.i18n("dialog_update_new_version_available"), 
                     JOptionPane.YES_NO_OPTION, 
                     JOptionPane.INFORMATION_MESSAGE, 
                     null, 
                     new String[]{ControlPanel.this.config.i18n("yes"), ControlPanel.this.config.i18n("no")}, // this is the array
                     "default") == JOptionPane.YES_OPTION){
            
               Desktop.getDesktop().browse(new URI(sb.toString()));            
            }
         }
      }
      
      catch (Throwable e){
         log.error(LOG, e);
      }
      
      log.info(LOG + "No update or server unavailble");
      return;
   }




   @Override
   public void dialogLoadReset(){
      SwingUtilities.invokeLater(new Thread(){
         public void run(){
            loadFileName = null;
            loadFileData = null;
            
            UIManager.put("FileChooser.openDialogTitleText",  ControlPanel.this.config.i18n("dialog_open_title"));
            UIManager.put("FileChooser.openButtonText",       ControlPanel.this.config.i18n("dialog_open_button_open"));
            UIManager.put("FileChooser.cancelButtonText",     ControlPanel.this.config.i18n("dialog_open_button_cancel"));
            UIManager.put("FileChooser.lookInLabelText",      ControlPanel.this.config.i18n("dialog_open_button_look_in"));
            UIManager.put("FileChooser.filesOfTypeLabelText", ControlPanel.this.config.i18n("dialog_open_button_file_type"));
            UIManager.put("FileChooser.fileNameLabelText",    ControlPanel.this.config.i18n("dialog_open_button_file_name"));
            
            
            
            try{
               MyFileChooser fileChooser = new MyFileChooser();
               fileChooser.setAcceptAllFileFilterUsed(false);
               fileChooser.setFileFilter(new FileNameExtensionFilter(".sb2", "sb2"));
               
               
               int iResult;
               while (true){
                  iResult = fileChooser.showOpenDialog(null);
                  if(iResult == JFileChooser.APPROVE_OPTION && !fileChooser.getSelectedFile().exists()){
                     JOptionPane.showMessageDialog(ControlPanel.this, ControlPanel.this.config.i18n("dialog_open_error_no_file"));
                  }
                  else{
                     break;
                  }
               }
               
               if(iResult == JFileChooser.APPROVE_OPTION){
                  selectedFile = fileChooser.getSelectedFile();
                  loadFileName = selectedFile.getName();
                  loadFileData = Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath()));
               }
            }
            catch (Exception e){
               log.error(LOG, e);
            }
         }
      });
   }
   
   
   
   @Override
   public String dialogLoadCheck(){
      return loadFileName;
   }
   


   @Override
   public byte[] dialogLoadData(){
      return loadFileData;
   }
   


   
   
   @Override
   public void dialogSaveReset(){
      selectedFile = null;
      saveFileName = null;
   }
   @Override
   public String dialogSaveCheck(){
      return saveFileName;
   }   
   @Override
   public void dialogSave(final byte[] data){
      log.trace(LOG + "save");
      
      if(selectedFile == null){
         dialogSaveAs("project", data);        
      }
      else{
         try{
            FileOutputStream stream = new FileOutputStream(selectedFile.getAbsolutePath());
            stream.write(data);
            stream.close();
         }
         catch (Exception e){
            log.error(LOG, e);
         }
      }
      
      dialogSaveTmp(data);
   }
   

   
   
   
   private File getTmpFile(){
      String sTmpFolder = System.getProperty("java.io.tmpdir");
      if(sTmpFolder.endsWith("/")){
         //Windows adds this                  
      }
      else{
         sTmpFolder += "/";
      }
      
      try{
         MessageDigest m = MessageDigest.getInstance("MD5");
         m.reset();
         m.update(System.getProperty("user.name").getBytes());
         byte[] digest = m.digest();
         BigInteger bigInt = new BigInteger(1, digest);
         String hashtext = bigInt.toString(16);
         // Now we need to zero pad it if you actually want the full 32 chars.
         while (hashtext.length() < 32){
            hashtext = "0" + hashtext;
         }
         return new File(sTmpFolder + "/" + hashtext + "_" + TEMP_FILE_NAME);
      }
      catch (Exception e){
         throw new Error();
      }
   }


   
   
   
   public byte[] dialogOpenTmp(){
      log.trace(LOG + "openTmp()");

      if(config.isAutoSave()){
         try{
            File tmpFile = getTmpFile();
            byte fileContent[] = new byte[(int) tmpFile.length()];
            FileInputStream stream = new FileInputStream(tmpFile);
            stream.read(fileContent);
            stream.close();

            return fileContent;
         }
         catch (Throwable e){
            log.info("Can not read the autosave file.");
         }
      }
      
      return null;
   }
   
   
   
   
   
   @Override
   public void dialogSaveTmp(final byte[] data){
      log.trace(LOG + "saveTmp()");
      
      
      synchronized(this){      
         if(config.isAutoSave()){
            try{
               FileOutputStream stream = new FileOutputStream(getTmpFile());
               stream.write(data);
               stream.close();
            }
            catch (Throwable e){
               log.error(LOG, e);
            }
         }
      }
   }
   

   
   
   
   
   @Override
   public void dialogSaveAs(final String sName, final byte[] data){
      log.trace(LOG + "save as");
      
      saveFileName = null;
            
      SwingUtilities.invokeLater(new Thread(){
         public void run(){                        
            UIManager.put("FileChooser.saveDialogTitleText",  ControlPanel.this.config.i18n("dialog_save_title"));
            UIManager.put("FileChooser.saveButtonText",       ControlPanel.this.config.i18n("dialog_save_button_save"));
            UIManager.put("FileChooser.cancelButtonText",     ControlPanel.this.config.i18n("dialog_save_button_cancel"));
            UIManager.put("FileChooser.lookInLabelText",      ControlPanel.this.config.i18n("dialog_save_button_look_in"));
            UIManager.put("FileChooser.filesOfTypeLabelText", ControlPanel.this.config.i18n("dialog_save_button_file_type"));
            UIManager.put("FileChooser.fileNameLabelText",    ControlPanel.this.config.i18n("dialog_save_button_file_name"));
            
      
            MyFileChooser fileChooser = new MyFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter(".sb2", "sb2"));               
            fileChooser.setSelectedFile(new File(sName));
            
            
            ControlPanel.this.setAlwaysOnTop(false);
               
            
            while(true){
               int iResult = fileChooser.showSaveDialog(null);
                  
               if(iResult == JFileChooser.APPROVE_OPTION){
                  selectedFile = fileChooser.getSelectedFile();
                  if(selectedFile.getAbsolutePath().endsWith(".sb2")){
                  }
                  else{
                     selectedFile = new File(selectedFile.getAbsolutePath() + ".sb2");
                  }
                  
                  if(selectedFile.exists()){
                     
                     UIManager.put("OptionPane.yesButtonText", ControlPanel.this.config.i18n("yes"));
                     UIManager.put("OptionPane.noButtonText",  ControlPanel.this.config.i18n("no"));                     
                     int dialogResult = JOptionPane.showConfirmDialog(null,
                                                                      ControlPanel.this.config.i18n("dialog_save_confirm_file_exists"),
                                                                      "",
                                                                      JOptionPane.YES_NO_OPTION);
                     if(dialogResult == JOptionPane.NO_OPTION){
                        continue;
                     }
                  }
                  
                  saveFileName = selectedFile.getName();
                  
                  
                  try{
                     FileOutputStream stream = new FileOutputStream(selectedFile.getAbsolutePath());
                     stream.write(data);
                     stream.close();
                     break;
                  }
                  catch (Exception e){
                     log.error(LOG, e);
                  }
               }
               else{
                  saveFileName = "---"; 
                  break;
               }
            }
            
            ControlPanel.this.setAlwaysOnTop(true);
         }
      });
   }
   

   
   
   class MyFileChooser extends JFileChooser{
      private static final long serialVersionUID = 7051835330389929338L;

      protected JDialog createDialog(Component parent) throws HeadlessException{
         JDialog dialog = super.createDialog(parent);
         dialog.setAlwaysOnTop(true);
         return dialog;
      }
   }

   
   
   
   class LabelRenderer implements TableCellRenderer{

      public LabelRenderer(){
      }


      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
         return (Component) value;
      }
   }
   
   
   
   
   
   class ButtonRenderer implements TableCellRenderer{
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
         return (Component) value;
      }
   }
   
   
   
   
   
   
   
   public class ClientsTableRenderer extends DefaultCellEditor{
      private static final long serialVersionUID = 1675293203153976084L;
      
      private JButton button;
      private boolean clicked;
      @SuppressWarnings("unused")
      private int row, col;
      @SuppressWarnings("unused")
      private JTable table;





      public ClientsTableRenderer(JCheckBox checkBox){
         super(checkBox);
         button = new JButton();
         button.setOpaque(true);
         button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
               fireEditingStopped();
            }
         });
      }





      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
         this.table = table;
         this.row = row;
         this.col = column;

         button.setForeground(Color.black);
         button.setBackground(UIManager.getColor("Button.background"));
         button.setText(ControlPanel.this.config.i18n("button_upload_firmware"));
         clicked = true;
         return button;
      }





      public Object getCellEditorValue(){
         if(clicked){
            //JOptionPane.showMessageDialog(button, row);
            
            FirmwareUploader uploader = new FirmwareUploader(locator.getPortList().get(row));
            uploader.start(); 
         }
         clicked = false;
         
         return new JButton(ControlPanel.this.config.i18n("button_upload_firmware"));
      }





      public boolean stopCellEditing(){
         clicked = false;
         return super.stopCellEditing();
      }





      protected void fireEditingStopped(){
         super.fireEditingStopped();
      }
   }
}
