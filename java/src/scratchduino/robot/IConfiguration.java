package scratchduino.robot;

import java.util.*;


public interface IConfiguration{
   
   
   IOS getIOS();
   
//   List<ISerialPortMode> getSerialPortModes();   
   Set<String> excludePorts();
   int getPortInitDelay();
   int getPortCloseDelay();
   public int getDeviceDetectionTime();
   
   String getManifest();

   
   
   String i18n(String sKey);

   String getVersion();

   String getUpdateURL();
   
//   String getFirmwareCommandLine();

   String getDefaultMotorSpeed();
   
   boolean isAutoSave();
   
   String getRootFolder();
}
