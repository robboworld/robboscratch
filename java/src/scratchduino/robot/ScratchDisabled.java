package scratchduino.robot;

import org.apache.commons.logging.*;

public class ScratchDisabled implements IScratch{
   private static final String LOG = "[SCRATCH] ";
   private static Log log = LogFactory.getLog(ScratchDisabled.class);
   
   
   
   public ScratchDisabled(IConfiguration config, IOS os){
      log.info(LOG + "No Scratch UI");
   }




   @Override
   public void run(){
   }  
}
