package scratchduino.robot.connector.v1;

import scratchduino.robot.*;

public class ConnectedDevice implements IConnectedDevice{
   
   private final int type;
   private final int version;
   private final String serial;
   private final String serialCompacted;
   
   
   public ConnectedDevice(int type, int version, String serial){
      this.type = type;
      this.version = version;
      this.serial = serial;
      
      StringBuilder sb = new StringBuilder(); 
      for(String section : serial.split("-")){
         if(sb.length() > 0) sb.append('-'); 
         try{
            sb.append(Long.parseLong(section));
         }
         catch (NumberFormatException e){
            sb.append(section);
         }
      }
      
      this.serialCompacted = sb.toString();
   }


   public int getType(){
      return type;
   }


   public int getFirmwareVersion(){
      return version;
   }


   public String getSerial(){
      return serial;
   }
   
   
   public String getSerialCompacted(){
      return this.serialCompacted;
   }
}
