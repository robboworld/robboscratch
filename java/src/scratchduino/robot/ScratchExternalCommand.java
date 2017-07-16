package scratchduino.robot;

import java.io.*;
import org.apache.commons.logging.*;

public class ScratchExternalCommand implements IScratch{
   private static final String LOG = "[SCRATCH] ";
   private static Log log = LogFactory.getLog(ScratchExternalCommand.class);

   private final IConfiguration config;
   private final IOS os;
   private final String sCommand;


   public ScratchExternalCommand(IConfiguration config, IOS os, String sCommand){
      log.trace(LOG + "Scratch bean has been instanced. External command=" + sCommand);
      this.config = config;
      this.os = os;
      this.sCommand = sCommand;
   }




   @Override
   public void run(){

      log.info(LOG + sCommand);

      try{
         switch(os.getType()) {
            case WINDOWS:{
               String sNameOS = sCommand + ".exe";

               try{
                  String line;
                  String pidInfo = "";

                  Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
                  BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

                  while ((line = input.readLine()) != null){
                     pidInfo += line;
                  }
                  input.close();

                  if(pidInfo.contains(sNameOS)){
                  }
                  else{
                     //not started
                     ProcessBuilder pb = new ProcessBuilder();
                     pb.directory(new File(System.getProperty("user.home")));
                     pb.command(sNameOS);
                     p = pb.start();

                     CloseConnector cc = new CloseConnector(p);
                     cc.start();
                  }

                  //Runtime.getRuntime().exec("cmdow.exe \"Adobe Flash Player 10\" /ACT /RES");
                  //Runtime.getRuntime().exec("wscript activate_flash_window.vbs");
               }
               catch (Throwable e){
                  log.error(LOG, e);
               }

               break;
            }

            case LINUX:{
               String line;
               String pidInfo = "";

               String sNameOS = sCommand;

               Process p = Runtime.getRuntime().exec("ps ax");
               BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

               while ((line = input.readLine()) != null){
                  pidInfo += line;
               }
               input.close();

               if(pidInfo.contains(sNameOS)){
                  log.info(LOG + "Seems already running");
               }
               else{
                  log.info(LOG + "Not runing yet, let's start.");
                  //not started
                  p = Runtime.getRuntime().exec("./" + sNameOS);

                  log.info(LOG + "ok, started");
                  CloseConnector cc = new CloseConnector(p);
                  cc.start();
               }

               Runtime.getRuntime().exec("wmctrl -a \"Flash\"");

               break;
            }

            case MAC:{
               String sNameOS = sCommand + ".app";
               String sCommand = "open " + config.getRootFolder() + "/" + sNameOS;
               log.debug(LOG + "We are going to run: " + sCommand);

               Runtime.getRuntime().exec(sCommand).waitFor();
               CloseConnectorPS cc = new CloseConnectorPS(sNameOS);
               cc.start();

               break;
            }
         }
      }
      catch(Throwable e){
         log.fatal(LOG, e);
      }
   }


   private class CloseConnector extends Thread{

      private final Process p;

      public CloseConnector(Process p){
         this.p = p;
      }

      public void run(){
         try{
            p.waitFor();
         }
         catch (InterruptedException e){
         }

         System.exit(0);
      }
   }


   private class CloseConnectorPS extends Thread{

      private final String sBinaryName;

      public CloseConnectorPS(String sNameOS){
         this.sBinaryName = sNameOS;
      }

      public void run(){
         while(true){
            try{
               StringBuilder pidInfo = new StringBuilder();

               Process p = Runtime.getRuntime().exec("ps ax");
               BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
               p.waitFor();

               while (input.ready()){
                  pidInfo.append(input.readLine());
               }
               input.close();

               if(pidInfo.toString().contains(sBinaryName)){
               }
               else{
                  System.exit(0);
               }

               try{
                  Thread.sleep(100);
               }
               catch (InterruptedException e){
                  break;
               }
            }
            catch (IOException e){
            }
            catch (InterruptedException e){
               break;
            }
         }
      }
   }

}
