package scratchduino.robot;


public interface IConfiguration{
   
   IOS getIOS();
   
   int getPortSpeed();
   int getPortInitDelay();
   int getPortCloseDelay();
   public int getDeviceDetectionTime();
   
   String getManifest();

   
   
   String i18n(String sKey);

   String getVersion();

   String getUpdateURL();
   
   String getFirmwareCommandLine();

   String getDefaultMotorSpeed();
}
