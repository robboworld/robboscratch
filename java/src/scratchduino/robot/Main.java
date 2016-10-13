package scratchduino.robot;

import java.io.*;
import java.net.*;
import org.apache.commons.logging.*;
import org.springframework.beans.*;

public class Main{
   private static Log log = LogFactory.getLog(Main.class);
   private static final String LOG = "[Main] ";

   private static final int PORT_NUMER = 34794;
   private static ServerSocket socket = null;


   /**
    * @param args
    * @throws Exception 
    * @throws BeansException 
    */
   public static void main(String[] args) throws Exception{
      log.info(LOG + "ok, let's start");
      
      Context.ctx.getBean("rest", IRest.class);     

      final IControlPanel main = Context.ctx.getBean("ui", IControlPanel.class);



      //Only one instance
      try{
         socket = new ServerSocket(PORT_NUMER);
         socket.isBound();

         Thread thread = new Thread(){
            public void run(){
               while(true){
                  try{
                     socket.accept();
                     main.popUp();
                  }
                  catch (IOException e){
                  }
               }
            }
         };
         thread.start();

      }
      catch (Exception e){
         new Socket("127.0.0.1", PORT_NUMER);
         System.exit(0);
      }


      main.popUp();


//      final RoboConfig roboConfig = ctx.getBean("robo_config", RoboConfig.class);


   }
}
