package scratchduino.robot;

import java.util.*;

public interface IDeviceLocator{
   
   public static enum STATUS{READY, IN_PROGRESS, STOPPED};
   
   public void start();
   void stop();
   public IDeviceLocator.STATUS getStatus();
   public List<IPort> getPortList();
   public Map<String, IPort> getPortByName();
   
   public IDeviceList getDevices();
}
