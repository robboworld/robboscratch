 package scratchduino.robot.connector.v1;

import java.util.*;
import org.apache.commons.logging.*;
import jssc.*;
import scratchduino.robot.*;
import scratchduino.robot.logs.*;
import scratchduino.robot.ui.v1.*;



public class DeviceLocator implements IDeviceLocator{
   
   private static Log log = LogFactory.getLog(ControlPanel.class);
   private static final String LOG = "[DeviceLocator] ";
   
   

   private final IConfiguration config;
   private final IDeviceList deviceList;
   private List<IPort> listPorts = new ArrayList<IPort>();   
   private Map<String, IPort> mapPorts = new HashMap<String, IPort>();   

   
   public DeviceLocator(IConfiguration config, IDeviceList deviceList){
      this.config = config;
      this.deviceList = deviceList;
   }
   
   
   @Override
   public void start(){
      synchronized(this){
         log.info(LOG + "Let's set up the  ports...");
         
//         switch(getStatus()){
//            case IN_PROGRESS:{
//               return;
//            }
//            case READY:{
//               break;
//            }
//            case STOPPED:{
//               break;
//            }
//         }
         
         String[] arrPorts = SerialPortList.getPortNames();

         log.info(LOG + "We got from OS: " + arrPorts);


         listPorts.clear();
         mapPorts.clear();
         
         LogTableFormatter ltf = new LogTableFormatter(1, true);
         int iCounter = 0;
         for(String sPortName : arrPorts){
            if(config.excludePorts().contains(sPortName.toLowerCase())) continue;
            
            Port port = new Port(this, sPortName, config, iCounter); 
            
            ltf.addCell(iCounter);
            ltf.addCell(sPortName);
            ltf.br();
            
            listPorts.add(port);
            mapPorts.put(sPortName, port);
            iCounter++;
         }
         
         log.info(LOG + "ok, done\n" + ltf.toString());
      }
   }
   @Override
   public void stop(){
      synchronized(this){
         log.info(LOG + "Closing all ports...");
         
         for(final IPort port : listPorts){
            log.trace(LOG + "closing=" + port.getPortName());
            port.close();
            log.trace(LOG + "ok, done");
         }
         
         log.info(LOG + "ok, all ports closed.");
      }
   }


   
   
   
   @Override
   public DeviceLocator.STATUS getStatus(){
      DeviceLocator.STATUS statusTemp = DeviceLocator.STATUS.READY; 
      
      synchronized(this){
         for(IPort port : listPorts){
            switch(port.getStatus()){
               case TIME_OUT:{
                  break;
               }
               case ERROR:{
                  break;
               }
               case INIT:{
                  return DeviceLocator.STATUS.IN_PROGRESS;
               }
               case OPENNED:{
                  return DeviceLocator.STATUS.IN_PROGRESS;
               }
               case TEST_DATA:{
                  return DeviceLocator.STATUS.IN_PROGRESS;
               }
               case RESPONSE:{
                  return DeviceLocator.STATUS.IN_PROGRESS;
               }
               case NO_RESPONSE:{
                  break;
               }
               case ROBOT_DETECTED:{
                  break;
               }
               case UNKNOWN_DEVICE:{
                  break;
               }
               case TERMINATING:{
                  return DeviceLocator.STATUS.IN_PROGRESS;
               }
               case TERMINATED:{
                  return DeviceLocator.STATUS.IN_PROGRESS;
               }
            }
         }
      }
      
      return statusTemp;
   }

   
   
   
   
   
   @Override
   public List<IPort> getPortList(){
      return Collections.unmodifiableList(listPorts);
   }

   @Override
   public Map<String, IPort> getPortByName(){
      return Collections.unmodifiableMap(mapPorts);
   }


   @Override
   public IDeviceList getDevices(){
      return deviceList;
   }
}
