package scratchduino.robot;

import org.springframework.beans.*;



public class TestFirmware{

   /**
    * @param args
    * @throws Exception 
    * @throws BeansException 
    */
   public static void main(String[] args) throws Exception{
      Context.ctx.getBean("firmware", IFirmware.class).uploadFirmware("COM3");
   }

}
