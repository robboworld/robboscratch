package scratchduino.robot.rest.v1;


import java.io.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;
import scratchduino.robot.*;

public class Jersey implements IRest{

   private final IDeviceList deviceList;
   private final IDeviceLocator locator;




   public Jersey(IDeviceLocator locator, IDeviceList commandList){
      this.deviceList = commandList;
      this.locator = locator;
      
      RunnerREST runner = new RunnerREST();
      runner.start();
      
      RunnerWEB runnerWEB = new RunnerWEB();
      runnerWEB.start();
   }




   private class RunnerREST extends Thread{

      public void run(){
         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.setContextPath("/");
         context.setAttribute("devices", deviceList);
         context.setAttribute("locator",  locator);

         Server jettyServer = new Server(9876);
         jettyServer.setHandler(context);

         ServletHolder jerseyServlet = context.addServlet(
                  com.sun.jersey.spi.container.servlet.ServletContainer.class, "/*");
         jerseyServlet.setInitOrder(0);

         // Tells the Jersey Servlet which REST service/class to load.
         jerseyServlet.setInitParameter("com.sun.jersey.config.property.packages",
                  "scratchduino.robot.rest.v1");

         try{
            jettyServer.start();
            jettyServer.join();
         }
         catch(Throwable e){
            throw new Error(e);
         }
         finally{
            jettyServer.destroy();
         }
      }
   }
   
   
   
   
   
   
   
   
   
   
   
   
   
   private class RunnerWEB extends Thread{

      public void run(){
         //File server
         Server jettyServer = new Server(9877);
         
         ResourceHandler resource_handler = new ResourceHandler();
         resource_handler.setDirectoriesListed(true);
         resource_handler.setWelcomeFiles(new String[]{ "index.html" });
         
         resource_handler.setResourceBase(new File("media").getAbsolutePath());

         HandlerList handlers = new HandlerList();
         handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
         jettyServer.setHandler(handlers);
         
         
         try{
            jettyServer.start();
            jettyServer.join();
         }
         catch(Throwable e){
            throw new Error(e);
         }
         finally{
            jettyServer.destroy();
         }
      }
   }
}
