package scratchduino.robot.commands.v1;

import java.util.*;
import scratchduino.robot.*;

public class DeviceList implements IDeviceList{
   
   private final Map<Integer, IDevice> mapDevices;
   public DeviceList(Map<Integer, IDevice> mapDevices){
      this.mapDevices = Collections.unmodifiableMap(new HashMap<Integer, IDevice>(mapDevices));
   }


   @Override
   public IDevice getDevice(int iDeviceID){
      return mapDevices.get(iDeviceID);
   }


   @Override
   public Collection<IDevice> getDevices(){
      return mapDevices.values();
   }


   @Override
   public Collection<Integer> getDeviceIDes(){
      return mapDevices.keySet();
   }
}
