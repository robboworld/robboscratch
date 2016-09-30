package scratchduino.robot.configuration.v1;

import java.util.*;
import javax.xml.bind.annotation.*;



@XmlRootElement(name = "command")
public class XCommand{

   public String code;
   public XResponse response = new XResponse();

   public static class XResponse{
      @XmlElement(name = "value")
      public List<XResponseValue> listValues = new ArrayList<XResponseValue>();





      @Override
      public String toString(){
         return "XResponse [listValues=" + listValues + "]";
      }
   }

   public static class XResponseValue{
      public String name;
      public String type;
      public int length;

      @Override
      public String toString(){
         return "Value [name=" + name + ", type=" + type + ", length=" + length + "]";
      }
   }





   @Override
   public String toString(){
      return "XCommand [code=" + code + ", response=" + response + "]";
   }
}
