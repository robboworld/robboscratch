package scratchduino.robot;

public interface IConnectedDevice{
   public int getType();
   public int getFirmwareVersion();
   public int getSerial();
}
