package scratchduino.robot.firmware.v1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import jssc.*;
import org.apache.commons.logging.*;
import org.springframework.beans.*;
import scratchduino.robot.*;
import scratchduino.robot.ui.v1.*;

public class FirmwareV1 extends JDialog implements WindowListener, IFirmware{
   private static final long serialVersionUID = -4884511825645086816L;
   
   private static Log log = LogFactory.getLog(FirmwareV1.class);
   private static final String LOG = "[COM] ";
   
   private final IConfiguration config;
   private final IDeviceList listDevices;
   
   private final JLabel lblAvrDudePath;
   private final JLabel lblAvrDudeVersionValue;
   private final JTextArea taFirmwareProgress;
   
   static{
      InterfaceHelper.setLookAndFeel();     
   }

   public FirmwareV1(IConfiguration config, IDeviceList listDevices){
      this.config = config;
      this.listDevices = listDevices; 
      
      
      
      this.setVisible(false);
      
      this.setSize(800, 300);
      this.setLocationRelativeTo(null);     
      
      this.setResizable(false);
      this.setAlwaysOnTop(true);
      
      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      
      this.setLayout(null);
      
      JPanel pnlAvrDudePath = new JPanel(new FlowLayout(FlowLayout.LEFT));
      JLabel lblAvrDudeName = new JLabel(config.i18n("dialog_firmare_avrdude"));
      lblAvrDudeName.setFont(new Font("Arial", Font.CENTER_BASELINE, 13));
      pnlAvrDudePath.add(lblAvrDudeName);
      
      lblAvrDudePath = new JLabel("");
      lblAvrDudePath.setFont(new Font("Arial", Font.PLAIN, 12));
      pnlAvrDudePath.add(lblAvrDudePath);
      
      pnlAvrDudePath.setBounds(10, 10, 700, 20);
      this.add(pnlAvrDudePath);


      JPanel pnlAvrDudeVersion = new JPanel(new FlowLayout(FlowLayout.LEFT));
      JLabel lblAvrDudeVersionName = new JLabel(config.i18n("dialog_firmare_avrdude_version"));
      lblAvrDudeVersionName.setFont(new Font("Arial", Font.CENTER_BASELINE, 13));
      pnlAvrDudeVersion.add(lblAvrDudeVersionName);
      
      lblAvrDudeVersionValue = new JLabel("");
      lblAvrDudeVersionValue.setFont(new Font("Arial", Font.PLAIN, 12));
      pnlAvrDudeVersion.add(lblAvrDudeVersionValue);
      
      pnlAvrDudeVersion.setBounds(10, 35, 700, 20);      
      this.add(pnlAvrDudeVersion);
      
      taFirmwareProgress = new JTextArea();
      taFirmwareProgress.setLineWrap(true);
      taFirmwareProgress.setWrapStyleWord(true);
      taFirmwareProgress.setBorder(new LineBorder(new Color(150, 150, 150)));
      taFirmwareProgress.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
      
      JScrollPane scroll = new JScrollPane(taFirmwareProgress);      
//      scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
       scroll.setBounds(20, 70, 750, 180);
      this.add(scroll);
      addWindowListener(this);
   }



   private Process p;
   private Timer timer;
   
   
   
   @Override
   public void uploadFirmware(final String sPortName) throws Exception{
      SwingUtilities.invokeAndWait(new Runnable(){
         public void run(){
            FirmwareV1.this.taFirmwareProgress.setText("");
            FirmwareV1.this.setVisible(true);
            // ControlPanel.this.setState(Frame.ICONIFIED);
         }
      });   
      
      
      final int iErrorCode = upload(sPortName, "diagnostics.hex");
      
      
      
      SerialPort serialPort = new SerialPort(sPortName);
      serialPort.openPort();
      
      // Something standart
      serialPort.setParams(config.getPortSpeed(),
                           SerialPort.DATABITS_8,
                           SerialPort.STOPBITS_1,
                           SerialPort.PARITY_NONE);

      // Hardware Overflow
      //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
      //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);
      serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
      serialPort.purgePort(255);
      
      StringBuilder sbDeviceID = new StringBuilder(serialPort.readString(100, 10000));
      serialPort.closePort();
      
      
      //Let's clean the rubbish
      while(sbDeviceID.length() > 0 && sbDeviceID.charAt(0) != 'R'){
         sbDeviceID.delete(0, 1);
      }      
      

      int iDeviceID   = Integer.parseInt(sbDeviceID.substring(6,11));
      
      
      
      final int iErrorCode2 = upload(sPortName, "devices/" + iDeviceID + "/" + listDevices.getDevice(iDeviceID).getFirmware() + ".hex");

      
      SwingUtilities.invokeAndWait(new Runnable(){
         public void run(){
            if(iErrorCode == 0){
               JOptionPane.showMessageDialog(FirmwareV1.this, config.i18n("dialog_firmware_ok"));
               FirmwareV1.this.setVisible(false);
            }
            else{
               FirmwareV1.this.addWindowListener(new WindowAdapter(){
                  @Override
                  public void windowClosing(WindowEvent e) {
                     FirmwareV1.this.setVisible(false);
                     FirmwareV1.this.removeWindowListener(this);
                  }
              });
               
              JOptionPane.showMessageDialog(FirmwareV1.this, config.i18n("dialog_firmware_error"), "", JOptionPane.ERROR_MESSAGE);
            }
         }
      });
      
   }
   
   
   
   
   public int upload(final String sPortName, final String sFirmware) throws Exception{
   
   
      final String sAVRDudePath;
      final String sAVRDudeVersion;
      
      switch(config.getIOS().getType()){
         case WINDOWS:{
            sAVRDudePath = new File(".").getCanonicalPath().replaceAll("\\\\", "/") + "/firmware/win/avrdude.exe";
            break;
         }
         case LINUX:{
            //Let's find out where the AVRDude is...
            p = Runtime.getRuntime().exec("which avrdude");
            
            InputStream stdout = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            sAVRDudePath = reader.readLine();
            break;
         }
         case MAC:{
            sAVRDudePath = new File(".").getCanonicalPath().replaceAll("\\\\", "/") + "/firmware/mac/avrdude";
            break;
         }
         default:{
            sAVRDudePath = null;
            break;
         }
      }
      
      log.info(LOG + " AVRDudePath=" + sAVRDudePath);      


      p = Runtime.getRuntime().exec(sAVRDudePath);
      InputStream stderr = p.getErrorStream();
      //InputStream stdout = p.getInputStream();

      //BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
      BufferedReader error = new BufferedReader(new InputStreamReader(stderr));

      String sOut = null;
      final Integer iVersion;
      try{
         p.waitFor();
         
         while (error.ready()){
            sOut = error.readLine();
         }
      }
      catch (Exception e){
      }
      sAVRDudeVersion = sOut;
      
      log.info("[COM] AVRDudeVersion=" + sAVRDudeVersion);
      
      if(sAVRDudeVersion == null){
         avrDudeNotFound();
         return 2;
      }
      
      
      Pattern pattern = Pattern.compile("\\d+\\.\\d+");
      Matcher matcher = pattern.matcher(sAVRDudeVersion);
      if(!matcher.find()){
         avrDudeNotFound();
         return 2;
      }

      
      try{
         iVersion = Integer.valueOf(Integer.parseInt(matcher.group(0).split("\\.")[0]));
      }
      catch (Exception e1){
         avrDudeNotFound();
         return 2;
      }
      
      
      SwingUtilities.invokeAndWait(new Runnable(){
         public void run(){
            lblAvrDudePath.setText(sAVRDudePath);
            lblAvrDudeVersionValue.setText(sAVRDudeVersion);
         }
      });
      

      //Let's check the minimal version
      if(iVersion < 6){
         SwingUtilities.invokeAndWait(new Runnable(){
            public void run(){
                  JOptionPane.showMessageDialog(FirmwareV1.this, config.i18n("dialog_firmare_avrdude_bad_version"));
                  FirmwareV1.this.setVisible(false);
            }
         });
         
         return 3;
      }

      
      
      
      
      
      
      
      
   
      
      final int iErrorCode;
      Timer timer = null;      
      
      switch(config.getIOS().getType()){
         case WINDOWS:{
            String sFullFirmwareCommand = config.getFirmwareCommandLine().replaceAll("%avrdude%", sAVRDudePath).replaceAll("%port%", sPortName).replaceAll("%firmware%", sFirmware);
            log.info(LOG + sFullFirmwareCommand);
            p = Runtime.getRuntime().exec(sFullFirmwareCommand);
            
            stderr = p.getErrorStream();
            error = new BufferedReader(new InputStreamReader(stderr));
            
            timer = new Timer(error);
            timer.start();
            
            break;
         }
         
         
         
         case LINUX:{
            String sFullFirmwareCommand = config.getFirmwareCommandLine().replaceAll("%avrdude%", "avrdude").replaceAll("%port%", sPortName).replaceAll("%firmware%", sFirmware);
            log.info(LOG + sFullFirmwareCommand);
            p = Runtime.getRuntime().exec(sFullFirmwareCommand);
            
            stderr = p.getErrorStream();
            error = new BufferedReader(new InputStreamReader(stderr));
            
            timer = new Timer(error);
            timer.start();
            
            break;
         }
         
         
         
         case MAC:{
            String sFullFirmwareCommand = config.getFirmwareCommandLine().replaceAll("%avrdude%", "firmware/mac/avrdude").replaceAll("%port%", sPortName).replaceAll("%firmware%", sFirmware);
            log.info(LOG + sFullFirmwareCommand);
            p = Runtime.getRuntime().exec(sFullFirmwareCommand);
            
            stderr = p.getErrorStream();
            error = new BufferedReader(new InputStreamReader(stderr));
            
            timer = new Timer(error);
            timer.start();
            
            break;
         }
      }
  
      iErrorCode = p.waitFor();
      timer.interrupt();
      
      return iErrorCode;
   }
   

   
   
   
   

   
   
   
   
   
   
   

   
   
   private class Timer extends Thread{
      StringBuilder sb = new StringBuilder();
      
      private final BufferedReader reader;
      
      public Timer(BufferedReader reader){
         this.reader = reader;
      }



      public void run(){
         long lStartTime = System.currentTimeMillis();

         while(true){
            if(this.isInterrupted()){
               break;
            }
            
            try{
               if(lStartTime + 10000 > System.currentTimeMillis()){
                  String s;
                  while ((s = reader.readLine()) != null){
                     sb.append(s + "\n");
                     
                     SwingUtilities.invokeAndWait(new Runnable(){
                        public void run(){
                           taFirmwareProgress.setText(sb.toString());
                        }
                     });
                  }
               }
               else{
                  // err...
                  // Timeout?
                  p.destroy();
                  break;
               }
         
               Thread.sleep(100);

            }
            catch (InterruptedException e){
               // ok, flashed
               break;
            }
            catch (IOException e){
               break;
            }
            catch (InvocationTargetException e){
               break;
            }
         }
      }
   }


   
   
   
   
   
   private void avrDudeNotFound(){
      try{
         SwingUtilities.invokeAndWait(new Runnable(){
            public void run(){
               FirmwareV1.this.lblAvrDudePath.setText(FirmwareV1.this.config.i18n("dialog_firmare_avrdude_not_found"));
               JOptionPane.showMessageDialog(FirmwareV1.this, FirmwareV1.this.config.i18n("dialog_firmare_avrdude_not_found"), "", 0);
            }
         });
      }
      catch (InvocationTargetException e1){
         e1.printStackTrace();
      }
      catch (InterruptedException e1){
         e1.printStackTrace();
      }
   }
   
   
   
   
   

   /**
    * @param args
    * @throws Exception 
    * @throws BeansException 
    */
   public static void main(String[] args) throws Exception{
      Context.ctx.getBean("firmware", IFirmware.class).uploadFirmware("COM6");
   }







   @Override
   public void windowActivated(WindowEvent arg0){
      // TODO Auto-generated method stub
      
   }







   @Override
   public void windowClosed(WindowEvent arg0){
      // TODO Auto-generated method stub
      
   }







   @Override
   public void windowClosing(WindowEvent arg0){
      setVisible(false);
      this.p.destroy();
      this.timer.interrupt();
   }







   @Override
   public void windowDeactivated(WindowEvent arg0){
      // TODO Auto-generated method stub
      
   }







   @Override
   public void windowDeiconified(WindowEvent arg0){
      // TODO Auto-generated method stub
      
   }







   @Override
   public void windowIconified(WindowEvent arg0){
      // TODO Auto-generated method stub
      
   }







   @Override
   public void windowOpened(WindowEvent arg0){
      // TODO Auto-generated method stub
      
   }
}
