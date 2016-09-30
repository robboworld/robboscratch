package scratchduino.robot;

public interface IDevice{
   
   int getFirmware();
   
   ICommand getCommand(String sCommand);
}
