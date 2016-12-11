package scratchduino.robot;

public interface IConnectedDevice{
   public int getType();
   public int getFirmwareVersion();
   public String getSerial();
   public String getSerialCompacted();
}
