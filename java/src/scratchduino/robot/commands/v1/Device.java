package scratchduino.robot.commands.v1;

import java.util.*;
import scratchduino.robot.*;
import scratchduino.robot.configuration.v1.*;

public class Device implements IDevice{
   
   public final ISerialPortMode serialPortMode;
   public final int firmware;
   public final String avrdude;
   
   public final Map<String, ICommand> mapCommands;
   

   public Device(String sPortSpeed, int firmware, String avrdude, Map<String, ICommand> mapCommands){
      String[] arrstrComportParams = sPortSpeed.split(",");
      this.serialPortMode = new SerialPortMode(Integer.parseInt(arrstrComportParams[0]),
                                               ISerialPortMode.PORT_FLOW_CONTROL.valueOf(arrstrComportParams[1]));
      this.firmware = firmware;
      this.avrdude = avrdude;
      this.mapCommands = Collections.unmodifiableMap(mapCommands);
   }
   
   
   public int getFirmware(){
      return this.firmware;
   }
   


   @Override
   public ICommand getCommand( String sCommand){
      return mapCommands.get(sCommand);
   }


   @Override
   public String getAVRDudeCommand(){
      return avrdude;
   }


   @Override
   public ISerialPortMode getPortMode(){
      // TODO Auto-generated method stub
      return this.serialPortMode;
   }
}
