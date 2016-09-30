package scratchduino.robot.commands.v1;

import java.util.*;
import scratchduino.robot.*;

public class Device implements IDevice{
   public final int firmware;
   
   public final Map<String, ICommand> mapCommands;
   

   public Device(int firmware, Map<String, ICommand> mapCommands){
      this.firmware = firmware;
      this.mapCommands = Collections.unmodifiableMap(mapCommands);
   }
   
   
   public int getFirmware(){
      return this.firmware;
   }
   


   @Override
   public ICommand getCommand( String sCommand){
      return mapCommands.get(sCommand);
   }
}
