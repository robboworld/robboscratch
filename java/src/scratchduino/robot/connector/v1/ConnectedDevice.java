package scratchduino.robot.connector.v1;

import scratchduino.robot.*;

public class ConnectedDevice implements IConnectedDevice{
   
   private final int type;
   private final int version;
   private final int serial;
   
   
   public ConnectedDevice(int type, int version, int serial){
      this.type = type;
      this.version = version;
      this.serial = serial;
   }


   public int getType(){
      return type;
   }


   public int getFirmwareVersion(){
      return version;
   }


   public int getSerial(){
      return serial;
   }
}
