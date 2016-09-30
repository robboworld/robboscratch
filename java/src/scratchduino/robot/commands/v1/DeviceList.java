package scratchduino.robot.commands.v1;

import java.util.*;
import scratchduino.robot.*;

public class DeviceList implements IDeviceList{
   
   private final Map<Integer, Device> mapDevices;


   public DeviceList(Map<Integer, Device> mapDevices){
      this.mapDevices = mapDevices;
   }


   @Override
   public Device getDevice(int iDeviceID){
      return mapDevices.get(iDeviceID);
   }
}
