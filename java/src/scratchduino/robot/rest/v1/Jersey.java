package scratchduino.robot.rest.v1;


import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.*;
import scratchduino.robot.*;

public class Jersey implements IRest{

   private final IDeviceList deviceList;
   private final IDeviceLocator locator;




   public Jersey(IDeviceLocator locator, IDeviceList commandList){
      this.deviceList = commandList;
      this.locator = locator;
      
      Runner runner = new Runner();
      runner.start();
   }




   private class Runner extends Thread{

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
}
