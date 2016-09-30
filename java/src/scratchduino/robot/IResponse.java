package scratchduino.robot;

import java.util.*;



public interface IResponse{

   public abstract byte[] getRawData();
   public abstract Map<String, Object> getParsedValues();

}