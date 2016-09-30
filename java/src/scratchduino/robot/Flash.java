package scratchduino.robot;

import java.io.*;
import org.apache.commons.logging.*;

public class Flash implements IFlash{
   private static final String LOG = "[FLASH] ";
   private static Log log = LogFactory.getLog(Flash.class);
   
   private final IOS os;
   
   
   public Flash(IOS os){
      this.os = os;
   }




   @Override
   public void run(){
      
      String sBinaryName = "flash";
      
      try {
         switch(os.getType()) {
            case WINDOWS:{
               String sNameOS = sBinaryName + ".exe";
               
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
               
               String sNameOS = sBinaryName;               
   
               Process p = Runtime.getRuntime().exec("ps ax");
               BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
   
               while ((line = input.readLine()) != null){
                  pidInfo += line;
               }
               input.close();
   
               if(pidInfo.contains(sNameOS)){
               }
               else{
                  //not started
                  p = Runtime.getRuntime().exec("./" + sNameOS);
                  CloseConnector cc = new CloseConnector(p);
                  cc.start();
               }
               
               Runtime.getRuntime().exec("wmctrl -a \"Flash\"");
               
               break;
            }
            
            case MAC:{
               String sNameOS = sBinaryName + ".app";
               
               Runtime.getRuntime().exec("open " + sNameOS).waitFor();
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
