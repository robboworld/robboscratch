package scratchduino.robot.connector.v1;

import java.nio.*;
import java.util.*;
import jssc.*;
import org.apache.commons.logging.*;
import scratchduino.robot.*;



public class Port implements IPort{
   private static Log log = LogFactory.getLog(Port.class);
   private final String LOG;

   private static final int COMMAND_TIMEOUT = 2000;

   private final IDeviceLocator locator;
   private final IDeviceList deviceList;
   private final String portName;
   private final IConfiguration config;
   private final int internalTestID;

   private IPort.PROGRESS progress = IPort.PROGRESS.INIT;
   private IPort.STATE state = IPort.STATE.DETECTION;
   private IConnectedDevice device = null;

   private final PortChecker portChecker;
   public SerialPort serialPort = null;
   private ISerialPortMode serialPortMode;





   // private volatile PortCommandReader reader;

   public Port(IDeviceLocator locator, IDeviceList deviceList, String sPortName,
            IConfiguration config, int internalTestID){
      this.locator = locator;
      this.deviceList = deviceList;
      this.portName = sPortName;
      this.config = config;
      this.internalTestID = internalTestID;
      this.portChecker = new PortChecker();
      this.portChecker.start();

      LOG = "[" + sPortName + "] ";
   }





   @Override
   public String getPortName(){
      return portName;
   }





   @Override
   public PROGRESS getProgress(){
      synchronized (this){
         return progress;
      }
   }





   @Override
   public STATE getState(){
      return state;
   }





   public IConnectedDevice getDevice(){
      synchronized (this){
         return device;
      }
   }





   public void close(){
      log.info(LOG + "Close()");

      synchronized (this){
         Port.this.progress = IPort.PROGRESS.CLOSING;

         try{
            serialPort.removeEventListener();
         }
         catch (Throwable e){
            log.error(e);
         }

         try{
            serialPort.purgePort(255);
         }
         catch (Throwable e){
            log.error(e);
         }

         try{
            serialPort.closePort();
         }
         catch (Throwable e){
            log.error(e);
         }

         Port.this.progress = IPort.PROGRESS.CLOSED;
      }

      log.info(LOG + "ok, closed.");
   }

   private class PortChecker extends Thread{

      // private Thread writer;

      public PortChecker(){
         Port.this.serialPort = new SerialPort(Port.this.portName);
      }





      public void run(){
         log.info(LOG + Port.this.portName + " Starting...");

         final IConfiguration config = Context.ctx.getBean("config", IConfiguration.class);

         try{
            Thread.currentThread().setName("Test Data Writer " + Port.this.portName);

            LinkedHashSet<ISerialPortMode> set = new LinkedHashSet<ISerialPortMode>();
            for(IDevice device : deviceList.getDevices()){
               set.add(device.getPortMode());
            }
            modes: for(Iterator<ISerialPortMode> it = set.iterator(); it.hasNext();){
               ISerialPortMode serialPortMode = it.next();

               // Let's open
               
               //modified_by_Yaroslav
               
              
               while (serialPort.isOpened()) {
            	   
            	   Thread.sleep(1);
            	   
               }
               
              
            	   
               serialPort.openPort();
            	   
               
              
               log.debug(LOG + Port.this.portName + " opened.");
               log.debug(LOG + Port.this.portName + " setting params...");

               // Something standart
               serialPort.setParams(serialPortMode.getSpeed(), 8, 1, SerialPort.PARITY_NONE);

               Port.this.serialPortMode = serialPortMode;

               synchronized (Port.this){
                  Port.this.progress = IPort.PROGRESS.OPENNED;
               }

               // Hardware Overflow
               // serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
               // | SerialPort.FLOWCONTROL_RTSCTS_OUT);
               // serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN
               // | SerialPort.FLOWCONTROL_XONXOFF_OUT);

               if(serialPortMode.getFlowControl() == ISerialPortMode.PORT_FLOW_CONTROL.RTS_CTS){
                  serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
                           | SerialPort.FLOWCONTROL_RTSCTS_OUT);
               }
               else{
                  serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
               }
               serialPort.purgePort(255);

               log.debug(LOG + Port.this.portName + " ok, done.");

               log.debug(LOG + Port.this.portName + " init delay=" + config.getPortInitDelay());

               try{
                  Thread.sleep(config.getPortInitDelay());
               }
               catch (InterruptedException e){
                  throw new Error(e);
               }

               // Send "ID" command
               synchronized (Port.this){
                  Port.this.progress = IPort.PROGRESS.TEST_DATA;
               }

               DetectTimer timer = new DetectTimer(!it.hasNext(), serialPortMode);
               timer.start();

               log.debug(LOG + Port.this.portName + " detect timer started.");

               // We send a set of "32" now
               // to avoid false detection
               for(int f = 0; f < internalTestID + 1; f++){
                  serialPort.writeByte((byte) 32);

                  synchronized (Port.this){
                     if(Port.this.progress == IPort.PROGRESS.TIME_OUT
                              || Port.this.progress == IPort.PROGRESS.CLOSED){
                        continue modes;
                     }
                  }
                  log.debug(LOG + Port.this.portName + " 0x32 sent");
               }

               log.debug(LOG + Port.this.portName + " Test data sent.");

               long lMaxDetecTime = System.currentTimeMillis() + config.getDeviceDetectionTime();

               StringBuilder sb = new StringBuilder();
               do{
                  String sData = Port.this.serialPort.readString();

                  log.debug(LOG + "read=" + sData);

                  if(sData == null){
                     Thread.sleep(100);
                  }
                  else{
                     sb.append(sData);

                     // Let's clean the rubbish
                     while (sb.length() > 0 && sb.charAt(0) != 'R'){
                        sb.delete(0, 1);
                     }

                     log.debug(LOG + Port.this.portName + "=" + sData);
                     
                     log.debug(LOG + "SB_Length" + "=" + sb.length());
                     log.debug(LOG + "Port.this.internalTestID" + "=" + Port.this.internalTestID);
                     log.debug(LOG + "52 * (Port.this.internalTestID + 1)" + "=" + 52 * (Port.this.internalTestID + 1));
                     

                     if(sb.length() == (52 * (Port.this.internalTestID + 1))){
                        // we need THE EXACT acount of bytes
                        // due to false alarms

                        log.info(LOG + "RAW ID=" + sb + " [" + sb.length() + "]");

                        int iDeviceType = Integer.parseInt(sb.substring(6, 11));
                        int iFirmware = Integer.parseInt(sb.substring(12, 17));
                        String sSerialNumber = sb.substring(18, 52);

                        log.info(LOG + "DEVICE=" + iDeviceType + " VERSION=" + iFirmware
                                 + " SERIAL=" + sSerialNumber);

                        Port.this.device = new ConnectedDevice(iDeviceType, iFirmware,
                                 sSerialNumber);

                        if(iFirmware >= locator.getDevices().getDevice(iDeviceType).getFirmware()){
                           synchronized (Port.this){
                              // reader = new PortCommandReader();
                              // serialPort.removeEventListener();
                              // serialPort.addEventListener(reader,
                              // SerialPort.MASK_RXCHAR);

                              Port.this.progress = IPort.PROGRESS.ROBOT_DETECTED;
                              Port.this.state = IPort.STATE.ROBOT_DETECTED;
                              timer.interrupt();
                              break modes;
                           }
                        }
                        else{
                           synchronized (Port.this){
                              Port.this.progress = IPort.PROGRESS.WRONG_VERSION;
                              timer.interrupt();
                              Port.this.state = IPort.STATE.WRONG_VERSION;
                              break modes;
                           }
                        }
                     }
                     else{
                        synchronized (Port.this){
                           Port.this.progress = IPort.PROGRESS.RESPONSE;
                        }
                     }

                  }

               }
               while (lMaxDetecTime > System.currentTimeMillis());

               log.info(LOG + Port.this.portName + " time is out.");

               if(serialPort.isOpened()){
                  serialPort.closePort();
               }
            }
         }
         catch (Throwable e){
            if(progress == IPort.PROGRESS.TIME_OUT){
               // we've already lost the port
               // timeout
            }
            else{
               log.error(LOG, e);
               Port.this.progress = IPort.PROGRESS.ERROR;
               Port.this.state = IPort.STATE.ERROR;
            }
         }

         synchronized (Port.this){
            if(Port.this.progress == IPort.PROGRESS.ROBOT_DETECTED
                     || Port.this.progress == IPort.PROGRESS.WRONG_VERSION){
               // ok, we gonna use it
            }
            else{
               // Let's close
               // no worth to keep it opened
               close();
            }

            if(Port.this.progress == IPort.PROGRESS.RESPONSE){
               // Strange response
               Port.this.progress = IPort.PROGRESS.UNKNOWN_DEVICE;
               Port.this.state = IPort.STATE.UNKNOWN_DEVICE;
            }
         }
      }
   }





   @Override
   public byte[] write(long CID, byte[] data, final int iLength){
      // Let's clean incoming buffer

      ByteBuffer bbuf = ByteBuffer.allocate(iLength);

      try{
         synchronized (Port.this){
            // serialPort.removeEventListener();
            // serialPort.purgePort(255);
            // serialPort.readBytes(serialPort.getInputBufferBytesCount());
            // serialPort.addEventListener(new PortCommandReader(CID, bbuf),
            // SerialPort.MASK_RXCHAR);

            // reader.read(CID, bbuf);

            // Timer timer = new Timer(CID);
            // timer.start();
            serialPort.writeBytes(data);

            bbuf.put(serialPort.readBytes(iLength, COMMAND_TIMEOUT - 10));

            // timer.interrupt();

            if(bbuf.capacity() == iLength){
               log.debug(LOG + "ok, read " + iLength + " done");
            }
            else{
               if(locator.getStatus() == IDeviceLocator.STATUS.READY){
                  log.fatal(LOG + "Port=" + Port.this.portName + " timeout!");
                  Port.this.progress = IPort.PROGRESS.ERROR;

                  IControlPanel cp = Context.ctx.getBean("ui", IControlPanel.class);
                  cp.popUp();
                  cp.reconnect();
               }
               else{
                  // ok, the whole interface it reconfiguring
                  log.error(LOG + " The interface is reconfiguring");
               }
            }

            // //Let's wait for data
            // this.wait(COMMAND_TIMEOUT);

            // timer.interrupt();
         }
      }
      // catch (SerialPortException e){
      // log.error(LOG, e);
      // }
      // catch (InterruptedException e){
      // log.error(LOG, e);
      // }
      catch (Exception e){
         log.error(LOG, e);

         if(locator.getStatus() == IDeviceLocator.STATUS.READY){
            log.fatal(LOG + "Port=" + Port.this.portName + " timeout!");
            Port.this.progress = IPort.PROGRESS.ERROR;

            IControlPanel cp = Context.ctx.getBean("ui", IControlPanel.class);
            cp.popUp();
            cp.reconnect();
         }
         else{
            // ok, the whole interface it reconfiguring
            log.error(LOG + " The interface is reconfiguring");
         }

      }

      return bbuf.array();
   }

   private class DetectTimer extends Thread{

      private final boolean isLastAttempt;





      public DetectTimer(boolean isLastAttempt, ISerialPortMode serialPortMode){
         log.info(LOG + serialPortMode + " isLastAttempt=" + isLastAttempt);
         this.setName(Port.this.LOG + "Detect timer (" + serialPortMode.getSpeed() + " "  + serialPortMode.getFlowControl() + ")");
         this.isLastAttempt = isLastAttempt;
      }





      public void run(){
         synchronized (Port.this){
            try{
               Port.this.wait(config.getDeviceDetectionTime() - 10);

               log.error(LOG + "Detect Daemon timeout.");

               Port.this.progress = IPort.PROGRESS.TIME_OUT;

               Port.this.close();

               if(isLastAttempt){
                  log.info(LOG + "It was last attempt to try.");
                  Port.this.state = IPort.STATE.TIME_OUT;
               }
               else {
                  log.info(LOG + "There are other devices.");
               }
            }
            catch (InterruptedException e){
               log.info(LOG + "ok, detection has been finished.");
            }
         }
      }
   }





   @Override
   public int getSpeed(){
      // TODO Auto-generated method stub
      return serialPortMode.getSpeed();
   }
}
