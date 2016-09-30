package scratchduino.robot;


public class Test{

   /**
    * @param args
    * @throws InterruptedException 
    */
   public static void main(String[] args) throws InterruptedException{
//    ApplicationContext ctx = new AnnotationConfigApplicationContext(Configs.class);
      
      
      //final IConfiguration config = ctx.getBean("config", IConfiguration.class);
      
      final IDeviceLocator locator = Context.ctx.getBean("device_locator", IDeviceLocator.class);


      for(int f = 0; f < 10; f++){
         locator.start();
         while(true) {
            for(IPort port : locator.getPortList()){
               System.out.print(port.getPortName() + " : " + port.getStatus());
               if(port.getDevice() == null){
               }
               else{
                  System.out.print(" " + port.getDevice().getFirmwareVersion());
               }
               
               System.out.println();
            }
            
            
            try{
               Thread.sleep(500);
            }
            catch (InterruptedException e){
               break;
            }
            
            System.out.println(locator.getStatus());
            if(locator.getStatus() == IDeviceLocator.STATUS.READY){
               break;
            }
         }

         System.out.println("----------------------------------------------");
         
         
         for(IPort port : locator.getPortList()){
            System.out.print(port.getPortName() + " : " + port.getStatus());
            if(port.getDevice() == null){
            }
            else{
               System.out.print(" " + port.getDevice().getFirmwareVersion());
            }
            System.out.println();
         }
         
         
         Thread.sleep(20000);
         
         locator.stop();
         
         Thread.sleep(2000);
         
      }
   }

}
