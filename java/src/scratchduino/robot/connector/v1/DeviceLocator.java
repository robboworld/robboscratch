 package scratchduino.robot.connector.v1;

import java.util.*;
import jssc.*;
import scratchduino.robot.*;



public class DeviceLocator implements IDeviceLocator{

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

         listPorts.clear();
         mapPorts.clear();
         
         int iCounter = 0;
         for(String sPortName : arrPorts){
            Port port = new Port(this, sPortName, config, iCounter); 
            listPorts.add(port);
            mapPorts.put(sPortName, port);
            iCounter++;
         }
      }
   }
   @Override
   public void stop(){
      synchronized(this){
         for(final IPort port : listPorts){
            port.close();
         }
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
