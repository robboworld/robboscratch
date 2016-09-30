package scratchduino.robot;

import java.util.*;
import org.apache.commons.logging.*;


public class D{
   private static Log log = LogFactory.getLog(D.class);
   private static final String LOG = "[TestPortWin8] ";

   /**
    * @param args
    * @throws InterruptedException 
    */
   public static void main(String[] args) throws InterruptedException{
      
      //final IConfiguration config = ctx.getBean("config", IConfiguration.class);
      
      final IDeviceLocator locator = Context.ctx.getBean("device_locator", IDeviceLocator.class);


      locator.start();
      while(true) {
         for(IPort port : locator.getPortList()){
            System.out.print(port.getPortName() + " : " + port.getStatus());
            if(port.getDevice() == null){
            }
            else{
               System.out.print(" " + port.getDevice().getFirmwareVersion());
            }
            
            System.out.println();
         }
         
         
         try{
            Thread.sleep(500);
         }
         catch (InterruptedException e){
            break;
         }
         
         System.out.println(locator.getStatus());
         if(locator.getStatus() == IDeviceLocator.STATUS.READY){
            break;
         }
      }

      System.out.println("----------------------------------------------");

      String sPortRobot = null;
      String sPortLab = null;
      for(IPort port : locator.getPortList()){
         if(port.getStatus() == IPort.STATUS.ROBOT_DETECTED && port.getDevice().getType() == 0){
            sPortRobot = port.getPortName();
         }
         if(port.getStatus() == IPort.STATUS.ROBOT_DETECTED && port.getDevice().getType() == 1){
            sPortLab = port.getPortName();
         }
      }
      
      
      
      ICommand commandForward  = Context.ctx.getBean("commands", IDeviceList.class).getCommand(2, "forward");
      ICommand commandBackward = Context.ctx.getBean("commands", IDeviceList.class).getCommand(2, "forward");

      long CID = 0;
      while(true){
         try{
            if(sPortRobot == null){
            }
            else{
               commandForward.run(CID, sPortRobot, Collections.<String>emptyList());
               CID++;
               commandBackward.run(CID, sPortRobot, Collections.<String>emptyList());
               CID++;
            }
            
            if(sPortLab == null){
            }
            else {
               for(int f = 0; f < 8; f++){
                  ICommand commandLed_On  = Context.ctx.getBean("commands", IDeviceList.class).getCommand(3, "led");
                  ICommand commandLed_Off  = Context.ctx.getBean("commands", IDeviceList.class).getCommand(3, "led");
                  commandLed_On.run(CID, sPortLab, Collections.<String>emptyList());
                  CID++;
                  commandLed_Off.run(CID, sPortLab, Collections.<String>emptyList());
                  CID++;
               }
            }                  
         }
         catch (Exception e){
            log.error(LOG, e);
         }
      }
   }
}
