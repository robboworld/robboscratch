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

   private IPort.STATUS status = IPort.STATUS.INIT;
   private IConnectedDevice device = null;

   private final PortChecker portChecker;
   public SerialPort serialPort = null;
   private ISerialPortMode serialPortMode;
   
//   private volatile PortCommandReader reader;


   public Port(IDeviceLocator locator, IDeviceList deviceList, String sPortName, IConfiguration config, int internalTestID){
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
   public STATUS getStatus(){
      synchronized(this){
         return status;
      }
   }


   public IConnectedDevice getDevice(){
      synchronized(this){
         return device;
      }
   }



   public void close(){
      log.info(LOG + "Close()");

      synchronized(this){
         Port.this.status = IPort.STATUS.TERMINATING;

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

         Port.this.status = IPort.STATUS.TERMINATED;
      }

      log.info(LOG + "ok, closed.");
   }


   
   
   
   
   

   private class PortChecker extends Thread{

//      private Thread writer;

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
modes:
            for(Iterator<ISerialPortMode> it = set.iterator(); it.hasNext(); ){
               ISerialPortMode serialPortMode = it.next(); 
               
               // Let's open
               serialPort.openPort();
               log.debug(LOG + Port.this.portName + " opened.");
               log.debug(LOG + Port.this.portName + " setting params...");
               
   
               // Something standart
               serialPort.setParams(serialPortMode.getSpeed(),
                                    8,
                                    1,
                                    SerialPort.PARITY_NONE);
               
               Port.this.serialPortMode = serialPortMode;

               
               synchronized(Port.this){
                  Port.this.status = IPort.STATUS.OPENNED;
               }
               
   
               // Hardware Overflow
               //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
               //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);
               
               if(serialPortMode.getFlowControl() == ISerialPortMode.PORT_FLOW_CONTROL.RTS_CTS){
                  serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
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
               synchronized(Port.this){
                  Port.this.status = IPort.STATUS.TEST_DATA;
               }
   
               
               DetectTimer timer = new DetectTimer(it.hasNext() ? IPort.STATUS.TERMINATING : IPort.STATUS.TIME_OUT);
               timer.start();
   
               log.debug(LOG + Port.this.portName + " detect timer started.");
   
               //We send a set of "32" now
               //to avoid false detection
               for(int f = 0; f < internalTestID + 1; f++) {
                  serialPort.writeByte((byte) 32);
                  log.debug(LOG + Port.this.portName + " 0x32 sent");
               }
   
               log.debug(LOG + Port.this.portName + " Test data sent.");
   
               long lMaxDetecTime = System.currentTimeMillis() + config.getDeviceDetectionTime(); 
   
               
               
               StringBuilder sb = new StringBuilder();
               do{
                  String sData = Port.this.serialPort.readString();
                  
                  log.debug(LOG + "read=" + sData);
                  
                  if(sData == null) {
                     Thread.sleep(100);
                  }
                  else {
                     sb.append(sData);
   
                     //Let's clean the rubbish
                     while(sb.length() > 0 && sb.charAt(0) != 'R'){
                        sb.delete(0, 1);
                     }
                     
                     log.debug(LOG + Port.this.portName + "=" + sData);
                     
                     if(sb.length() == (52 * (Port.this.internalTestID + 1))){
                        //we need THE EXACT acount of bytes
                        //due to false alarms
   
                        log.info(LOG + "RAW ID=" + sb + " [" + sb.length() + "]");
   
   
                        int iDeviceType   = Integer.parseInt(sb.substring(6,11));
                        int iFirmware     = Integer.parseInt(sb.substring(12,17));
                        String sSerialNumber = sb.substring(18, 52);
   
   
                        log.info(LOG + "DEVICE=" + iDeviceType + " VERSION=" + iFirmware + " SERIAL=" + sSerialNumber);
   
                        Port.this.device = new ConnectedDevice(iDeviceType, iFirmware, sSerialNumber);
   
                        if(iFirmware >= locator.getDevices().getDevice(iDeviceType).getFirmware()){
                           synchronized (Port.this){
   //                           reader = new PortCommandReader();                         
   //                           serialPort.removeEventListener();
   //                           serialPort.addEventListener(reader, SerialPort.MASK_RXCHAR);
                              
                              Port.this.status = IPort.STATUS.ROBOT_DETECTED;
                              timer.interrupt();
                              break modes;
                           }
                        }
                        else{
                           synchronized (Port.this){
                              Port.this.status = IPort.STATUS.WRONG_VERSION;
                              timer.interrupt();
                              break modes;
                           }
                        }
                     }
                     else{
                        synchronized (Port.this){
                           Port.this.status = IPort.STATUS.RESPONSE;
                        }
                     }
                     
                  }
   
               }
               while(lMaxDetecTime > System.currentTimeMillis());
               
               log.info(LOG + Port.this.portName + " time is out.");
               
               serialPort.closePort();
            }
         }
         catch (Throwable e){
            log.error(LOG, e);
            Port.this.status = IPort.STATUS.ERROR;
         }

         synchronized(Port.this){
            if(Port.this.status == IPort.STATUS.ROBOT_DETECTED || Port.this.status == IPort.STATUS.WRONG_VERSION){
               //ok, we gonna use it
            }
            else {
               //Let's close
               //no worth to keep it opened
               close();
            }

            if(Port.this.status == IPort.STATUS.ROBOT_DETECTED ||
               Port.this.status == IPort.STATUS.ERROR  ||
               Port.this.status == IPort.STATUS.UNKNOWN_DEVICE ||
               Port.this.status == IPort.STATUS.WRONG_VERSION){
            }
            else if(Port.this.status == IPort.STATUS.RESPONSE){
               Port.this.status = IPort.STATUS.UNKNOWN_DEVICE;
            }
            else{
               Port.this.status = IPort.STATUS.TIME_OUT;
            }
         }
      }
   }


   @Override
   public byte[] write(long CID, byte[] data, final int iLength){
      //Let's clean incoming buffer

      ByteBuffer bbuf = ByteBuffer.allocate(iLength);

      try{
         synchronized(Port.this){
//            serialPort.removeEventListener();
//            serialPort.purgePort(255);
//            serialPort.readBytes(serialPort.getInputBufferBytesCount());
//            serialPort.addEventListener(new PortCommandReader(CID, bbuf), SerialPort.MASK_RXCHAR);

//            reader.read(CID, bbuf);

//            Timer timer = new Timer(CID);
//            timer.start();
            serialPort.writeBytes(data);
            
            bbuf.put(serialPort.readBytes(iLength, COMMAND_TIMEOUT - 10));
            
//            timer.interrupt();
            
            if(bbuf.capacity() == iLength) {
               log.debug(LOG + "ok, read " + iLength + " done");
            }
            else{
               if(locator.getStatus() == IDeviceLocator.STATUS.READY) {
                  log.fatal(LOG + "Port=" + Port.this.portName + " timeout!");
                  Port.this.status = IPort.STATUS.ERROR;

                  IControlPanel cp = Context.ctx.getBean("ui", IControlPanel.class);
                  cp.popUp();
                  cp.reconnect();
               }
               else{
                  //ok, the whole interface it reconfiguring
                  log.error(LOG + " The interface is reconfiguring");
               }
            }
            

//            //Let's wait for data
//            this.wait(COMMAND_TIMEOUT);

//            timer.interrupt();
         }
      }
//      catch (SerialPortException e){
//         log.error(LOG, e);
//      }
//      catch (InterruptedException e){
//         log.error(LOG, e);
//      }
      catch (Exception e){
         log.error(LOG, e);
         
         if(locator.getStatus() == IDeviceLocator.STATUS.READY) {
            log.fatal(LOG + "Port=" + Port.this.portName + " timeout!");
            Port.this.status = IPort.STATUS.ERROR;

            IControlPanel cp = Context.ctx.getBean("ui", IControlPanel.class);
            cp.popUp();
            cp.reconnect();
         }
         else{
            //ok, the whole interface it reconfiguring
            log.error(LOG + " The interface is reconfiguring");
         }
         
      }

      return bbuf.array();
   }

   

   
   private class DetectTimer extends Thread{
      final IPort.STATUS status;
      
      public DetectTimer(STATUS status){
         this.status = status;
      }


      public void run(){      
         
         synchronized(Port.this){
            try{
               Port.this.wait(config.getDeviceDetectionTime() - 10);
               
               Port.this.status = status;
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
   
   
   

/*
   private class Timer extends Thread{
      
      private final long CID;
      
      public Timer(long CID){
         this.CID = CID;
      }




      public void run(){
         Thread.currentThread().setName("Timer: " + Port.this.portName + " CID=" + CID);
         
         synchronized(this){
            try{
               this.wait(COMMAND_TIMEOUT - 10);
               //Time is out!
               //Seems the Robot died.
               //RIP

//               serialPort.purgePort(255);
//               serialPort.removeEventListener();
//               serialPort.closePort();
//               serialPort = null;

               if(locator.getStatus() == IDeviceLocator.STATUS.READY) {
                  log.fatal(LOG + "Port=" + Port.this.portName + " timeout!");
                  Port.this.status = IPort.STATUS.ERROR;

                  IControlPanel cp = Context.ctx.getBean("ui", IControlPanel.class);
                  cp.popUp();
                  cp.reconnect();
               }
               else{
                  //ok, the whole interface it reconfiguring
                  log.error(LOG + " The interface is reconfiguring");
               }
            }
            catch (InterruptedException e){
               log.debug(LOG + "Port=" + Port.this.portName + " The Command completed.");
            }
         }
      }
   }
*/


/*
   private class PortCommandReader implements SerialPortEventListener{
      private ByteBuffer bbuf;
      private long CID;
      
      public PortCommandReader(){
      }
      
      
      public void read(long CID, ByteBuffer bbuf){
         this.CID = CID;
         this.bbuf = bbuf;
      }


      public void serialEvent(SerialPortEvent event){
         Thread.currentThread().setName("Reader: " + Port.this.portName + " CID=" + CID);
         
         if(event.isRXCHAR() && event.getEventValue() > 0){
            try{
               byte[] arrbyteRead = serialPort.readBytes();

               //System.out.println(arrbyteRead.length);

               bbuf.put(arrbyteRead);

               if(bbuf.hasRemaining()){
               }
               else{
                  log.trace(LOG + "Buffer is full.");

                  synchronized(Port.this){
                     Port.this.notify();
                  }
               }
            }
            catch(Exception e){
            }
         }
      }
   }
*/   
}



