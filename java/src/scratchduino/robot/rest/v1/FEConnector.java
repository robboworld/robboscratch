package scratchduino.robot.rest.v1;

import java.util.*;
import java.util.concurrent.atomic.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Context;
import org.apache.commons.logging.*;
import scratchduino.robot.*;



@Path("/")
public class FEConnector{
   private static Log log = LogFactory.getLog(FEConnector.class);
   private static final String LOG = "[Connector] ";
   

   @Context
   ServletContext context;

   
   private static final Map<String,String > locks = new HashMap<String, String>();
   
   
   private static final AtomicLong atomlongCID = new AtomicLong(0);



   public FEConnector(@Context HttpServletRequest hsr){
      log.info(LOG + "\n------------------------------------\n" + hsr.getRequestURI());

   }


   
   @GET
   @Path("/crossdomain.xml")
   @Produces("text/plain; charset=UTF-8")
   public String crossdomain() throws Exception{      
      return "<?xml version=\"1.0\"?><cross-domain-policy><site-control permitted-cross-domain-policies=\"master-only\"/><allow-access-from domain=\"*\" /></cross-domain-policy>";
   }
   @POST
   @Path("/crossdomain.xml")
   @Produces("text/plain; charset=UTF-8")
   public String crossdomainPOST() throws Exception{      
      log.trace(LOG + "POST");
      return crossdomain();
   }


   
   
   
   @GET
   @Path("/settings")
   @Produces("text/plain; charset=UTF-8")
   public String settings() throws Exception{      
      log.trace(LOG + "settings()");

      final IConfiguration config = scratchduino.robot.Context.ctx.getBean("config", IConfiguration.class);

      return "default_motor_speed=" + config.getDefaultMotorSpeed();
   }
   
   
   
   
   
   
   @GET
   @Path("/dialog/load/reset")
   @Produces("text/plain; charset=UTF-8")
   public String dialogLoadReset() throws Exception{      
      log.trace(LOG + "loadReset()");

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      main.dialogLoadReset();

      return "";
   }
   
   

   
   @GET
   @Path("/dialog/load/check")
   @Produces("text/plain; charset=UTF-8")
   public String dialogLoadCheck() throws Exception{      
      log.trace(LOG + "loadCheck()");

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      return main.dialogLoadCheck() == null ? "" : main.dialogLoadCheck();  
   }
   
   
   
   @GET
   @Path("/dialog/load/data")
   @Produces(MediaType.APPLICATION_OCTET_STREAM)   
   public byte[] dialogLoadData() throws Exception{      
      log.trace(LOG + "loadData()");

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      return main.dialogLoadData();  
   }
   
   
   
   
   
   @POST
   @Path("/dialog/save/{NAME}")
   @Produces("text/plain; charset=UTF-8")
   public String saveScratch(@PathParam("NAME") String sName,
                             byte[] data) throws Exception{      
      log.trace(LOG + "SAVE name=" + sName + ", length=" + data.length);

      final IControlPanel main = scratchduino.robot.Context.ctx.getBean("ui", IControlPanel.class);
      main.dialogSave(sName, data);
      
      return "";
   }
   
   
   

   
   @GET
   @Path("/default/{DEVICE}/{paths:.+}")
   @Produces("text/plain; charset=UTF-8")
   public String defaultPort(@PathParam("PORT")   String sPortName,
                             @PathParam("DEVICE") int iDeviceID,
                             @PathParam("paths")  List<PathSegment> uglyPath,
                             @Context HttpServletResponse response) throws Exception{
      resetCache(response);      
      
      IDeviceLocator locator = ((IDeviceLocator) context.getAttribute("locator"));
      
      log.debug(LOG + "Status=" + locator.getStatus());
      
      if(locator.getStatus() == IDeviceLocator.STATUS.READY){
         for(IPort port : locator.getPortList()){
            if(port.getStatus() == IPort.STATUS.ROBOT_DETECTED && port.getDevice().getType() == iDeviceID){
               return servicePort(port.getPortName(), iDeviceID, uglyPath, response);
            }
         }
      }
      
      return "error=1";
   }
   @POST
   @Path("/default/{DEVICE}/{paths:.+}")
   @Produces("text/plain; charset=UTF-8")
   public String defaultPortPOST(@PathParam("PORT")    String sPortName,
                                 @PathParam("DEVICE")  int iDeviceID,
                                 @PathParam("paths") List<PathSegment> uglyPath,
                                 @Context HttpServletResponse response) throws Exception{
      log.trace(LOG + "POST");
      
      resetCache(response);      
      
      return defaultPort(sPortName, iDeviceID, uglyPath, response);
   }
   
   
   
   
   


   @GET
   @Path("/port/{PORT}/{DEVICE}/{paths:.+}")
   @Produces("text/plain; charset=UTF-8")
   public String servicePort(@PathParam("PORT")  String sPortName,
                             @PathParam("DEVICE")  int iDeviceID,
                             @PathParam("paths") List<PathSegment> uglyPath,
                             @Context HttpServletResponse response) throws Exception{
      
      resetCache(response);      

      List<String> listParameters = new ArrayList<String>();

      for(PathSegment pathSegment : uglyPath){
         listParameters.add(pathSegment.getPath());
      }
      
      String sCommand = listParameters.remove(0);
      
      
      synchronized(FEConnector.class){
         if(locks.get(sPortName) == null){
            locks.put(sPortName, sPortName);
         }
      }
      
      synchronized(locks.get(sPortName)){      
      
         IDeviceLocator locator = ((IDeviceLocator) context.getAttribute("locator"));
         
         if(locator.getStatus() == IDeviceLocator.STATUS.READY){      
            if("crossdomain.xml".equals(sCommand)){
               return "<?xml version=\"1.0\"?><cross-domain-policy><site-control permitted-cross-domain-policies=\"master-only\"/><allow-access-from domain=\"*\" /></cross-domain-policy>";
            }
            else{
               try{
                  
                  if(locator.getPortByName().get(sPortName).getDevice().getType() == iDeviceID) { 
                     IDeviceList listDevices = (IDeviceList) context.getAttribute("devices");
                     ICommand command = listDevices.getDevice(iDeviceID).getCommand(sCommand);
                     
                     if(command == null){
                        return "error=" + IRest.UNKNOWN_COMMAND;
                     }                  
                     
                     IResponse reponse = command.run(atomlongCID.getAndIncrement(), locator.getPortByName().get(sPortName), listParameters);
   
                     StringBuilder sb = new StringBuilder();
                     ArrayList<String> arliKeys = new ArrayList<String>(reponse.getParsedValues().keySet());
                     
//                        Collections.sort(arliKeys);
   
                     for(String sKey : arliKeys){
                        //sb.append(sKey + "=" + reponse.getParsedValues().get(sKey) + "\n");
                        sb.append(reponse.getParsedValues().get(sKey) + "\n");
                     }
   
                     return sb.toString();
                  }
                  else{
                     return "error=" + IRest.DEVICE_NOT_SUPPORTED;
                  }
                  }
               catch (Exception e){
                  return "error=" + IRest.UNKNOWN_ERROR;
               }
            }
         }
         else{
            return "error=1";
         }
      }
   }
   

   
   private void resetCache(HttpServletResponse response){
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
   }
}
