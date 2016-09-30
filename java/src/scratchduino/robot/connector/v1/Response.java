package scratchduino.robot.connector.v1;

import java.util.*;
import scratchduino.robot.*;



public class Response implements IResponse{

   private final byte[] rawData;
   private final Map<String, Object> parsedValues;
   
   

   public Response(byte[] rawData, Map<String, Object> parsedValues){
      this.rawData = rawData;
      this.parsedValues = parsedValues;
   }
   

   @Override
   public byte[] getRawData(){
      return Arrays.copyOf(rawData, rawData.length);
   }

   @Override
   public Map<String, Object> getParsedValues(){
      return parsedValues;
   }
}
