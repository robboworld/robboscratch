package scratchduino.robot;

import org.apache.commons.logging.*;

public class FlashNoGUI implements IFlash{
   private static final String LOG = "[FLASH] ";
   private static Log log = LogFactory.getLog(FlashNoGUI.class);
   
   
   
   public FlashNoGUI(IConfiguration config, IOS os){
      log.info(LOG + "No Flash UI");
   }




   @Override
   public void run(){
   }  
}
