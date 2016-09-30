package scratchduino.robot;

import java.io.*;
import java.util.*;
import org.apache.commons.logging.*;

public class OS implements IOS{
   private static final String LOG = "[OSDetector] ";
   private static Log log = LogFactory.getLog(OS.class);
   
   private static IOS.TYPE cached; 

   @Override
   public IOS.TYPE getType(){
      if(cached == null){      
         String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
         log.info(LOG + OS);
         log.info(LOG + "PATH=" + new File(".").getAbsolutePath());
         
         if((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)){
            cached = IOS.TYPE.MAC;
         }
         else if(OS.indexOf("win") >= 0){
            cached = IOS.TYPE.WINDOWS;
         }
         else if(OS.indexOf("nux") >= 0){
            cached = IOS.TYPE.LINUX;
         }
         else{
            cached = IOS.TYPE.UNKNOWN;
         }
      }
      return cached;
   }

}
