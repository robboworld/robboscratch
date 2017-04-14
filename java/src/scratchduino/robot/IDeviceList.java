package scratchduino.robot;

import java.util.*;



public interface IDeviceList{

   public Collection<IDevice> getDevices();
   public Collection<Integer> getDeviceIDes();
   public IDevice getDevice(int iDeviceID);
}
