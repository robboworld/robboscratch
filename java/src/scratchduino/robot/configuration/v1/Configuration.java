package scratchduino.robot.configuration.v1;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import scratchduino.robot.*;



public class Configuration implements IConfiguration{

   private static final String CONFIG_FILE = "config.ini";

   private final Properties properties;
   private final Properties i18n;






   private Properties i18n() throws Exception{
      Properties i18n;

      try{
         i18n = loadFile("i18n." + Locale.getDefault().getISO3Language());
      }
      catch (Throwable e){
         // English if no file
         i18n = loadFile("i18n.eng");
      }

      return i18n;
   }





   private Properties loadFile(String sPath) throws Exception{
      InputStream isFileProperties = null;

      Properties properties = new Properties();
      try{
         String sFullPath = this.getRootFolder() + "/" + sPath;

         System.out.println("Loading the property server file at the path " + sFullPath);

         isFileProperties = null;

         isFileProperties = new FileInputStream(sFullPath);
         Reader reader = new InputStreamReader(isFileProperties, "UTF-8");
         properties.load(reader);

         System.out.println("ok, loaded.");
      }
      catch (Exception e){
         throw new Exception("Can not open the properties file: " + isFileProperties);
      }

      return properties;
   }

   private final IOS os;



   private final String ROOT_PATH;




   public Configuration(IOS os){
      this.os = os;


      try{
         Path path = Paths.get(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();

         if(new File(path.toFile() + File.separator + CONFIG_FILE).exists()){
            //IDE
         }
         else{
            path = path.getParent();
         }

         ROOT_PATH = path.toString().replaceAll("\\\\", "/");
      }
      catch (URISyntaxException e){
         throw new Error(e);
      }



      try{
         properties = loadFile(CONFIG_FILE);
      }
      catch (Throwable e){
         throw new Error(e);
      }

      try{
         i18n = i18n();
      }
      catch (Throwable e){
         throw new Error(e);
      }

   }





   // public static String getMongoURL(){
   // String sMongoURL = properties.getProperty("database_ip");
   //
   // if(sMongoURL == null){
   // throw new ServiceConfigurationException(
   // "'database_ip' is missed in the server.properties"
   // );
   // }
   // else{
   // return sMongoURL;
   // }
   // }

   @Override
   public int getPortSpeed(){
      String sPortSpeed = properties.getProperty("speed");

      if(sPortSpeed == null || "".equals(sPortSpeed)){
         throw new Error("'Speed' is missed in the server.properties");
      }
      else{
         try{
            return Integer.parseInt(sPortSpeed);
         }
         catch (NumberFormatException e){
            throw new Error("'Speed' must be int type");
         }
      }
   }










   @Override
   public String getManifest(){
      // TODO Auto-generated method stub
      return null;
   }





   @Override
   public int getPortInitDelay(){
      // TODO Auto-generated method stub
      return 4000;
   }





   @Override
   public int getPortCloseDelay(){
      // TODO Auto-generated method stub
      return 500;
   }





   @Override
   public int getDeviceDetectionTime(){
      // TODO Auto-generated method stub
      return 8000;
   }
















   @Override
   public String i18n(String sKey){
      return i18n.getProperty(sKey);
   }





   @Override
   public IOS getIOS(){
      return os;
   }



   @Override
   public String getVersion(){
      String sVersion = properties.getProperty("version");

      if(sVersion == null || "".equals(sVersion)){
         throw new Error("'version' is missed in the server.properties");
      }
      else{
         return sVersion;
      }
   }




   @Override
   public String getUpdateURL(){
      String sUpdateURL = properties.getProperty("update_url");

      if(sUpdateURL == null || "".equals(sUpdateURL)){
         throw new Error("update_url' is missed in the server.properties");
      }
      else{
         return sUpdateURL;
      }
   }





   @Override
   public String getFirmwareCommandLine(){
      String sFirmwareCommandLine = properties.getProperty("avrdude_command_line");

      if(sFirmwareCommandLine == null || "".equals(sFirmwareCommandLine)){
         throw new Error("avrdude_command_line' is missed in the server.properties");
      }
      else{
         return sFirmwareCommandLine;
      }
   }


   @Override
   public String getDefaultMotorSpeed(){
      final String KEY = "default";
      String sValue = properties.getProperty(KEY);

      if(sValue == null || "".equals(KEY)){
         throw new Error("'" + KEY + "' is missed in the server.properties");
      }
      else{
         return sValue;
      }
   }



   @Override
   public boolean isAutoSave(){
      final String KEY = "autosave";
      String sValue = properties.getProperty(KEY);

      if(sValue == null || "".equals(KEY)){
         throw new Error("'" + KEY + "' is missed in the server.properties");
      }
      else{
         return Boolean.parseBoolean(sValue);
      }
   }





   @Override
   public String getRootFolder(){
      return this.ROOT_PATH;
   }
}
