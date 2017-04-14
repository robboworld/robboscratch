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


   
   
   
   @Override
   public Set<String> excludePorts(){
      String KEY = "exclude";
      String sValue = properties.getProperty(KEY);

      try{
         Set<String> setPorts = new HashSet<String>();
         for(String sPort : Arrays.asList(sValue.split(","))){
            setPorts.add(sPort.trim().toLowerCase());
         }
         return setPorts;
      }
      catch (Exception e){
         return Collections.emptySet();
      }
   }










   @Override
   public String getManifest(){
      // TODO Auto-generated method stub
      return null;
   }





   @Override
   public int getPortInitDelay(){
      return getIntValue("initDelay");      
   }
   @Override
   public int getPortCloseDelay(){
      return getIntValue("closeDelay");      
   }
   @Override
   public int getDeviceDetectionTime(){
      return getIntValue("detectionTime");      
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
      return getStringValue("version");
   }




   @Override
   public String getUpdateURL(){
      return getStringValue("update_url");
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
      return getBooleanValue("autosave");      
   }





   @Override
   public String getRootFolder(){
      return this.ROOT_PATH;
   }
   
   
   
   
   
   
   private String getStringValue(String sKey){
      String sValue = properties.getProperty(sKey);

      if(sValue == null || "".equals(sValue)){
         throw new Error("'" + sKey + "' is missed in the server.properties");
      }
      else{
         return sValue;
      }
   }
   
   private int getIntValue(String sKey){
      String sValue = getStringValue(sKey);
      
      try{
         return Integer.parseInt(sValue);
      }
      catch (NumberFormatException e){
         throw new Error("'" + sKey + "' has to be integer");
      }
   }
   
   
   private boolean getBooleanValue(String sKey){
      String sValue = getStringValue(sKey);
      
      return Boolean.parseBoolean(sValue);
   }
}
