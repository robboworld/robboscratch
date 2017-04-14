package scratchduino.robot;

public interface IDevice{
   
   int getFirmware();
   
   String getAVRDudeCommand();
   
   ISerialPortMode getPortMode();
   
   ICommand getCommand(String sCommand);
}
