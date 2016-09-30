package scratchduino.robot;

import javax.xml.bind.*;

public class C{

   /**
    * @param args
    */
   public static void main(String[] args){
      byte[] val = DatatypeConverter.parseHexBinary("6103");
      System.out.print(val);   
      
   }

}
