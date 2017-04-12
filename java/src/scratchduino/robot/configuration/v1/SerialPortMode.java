package scratchduino.robot.configuration.v1;

import scratchduino.robot.*;

public class SerialPortMode implements ISerialPortMode{
   public final int speed;
   public final PORT_FLOW_CONTROL flowControl;
   
   public SerialPortMode(int speed, PORT_FLOW_CONTROL flowControl){
      this.speed = speed;
      this.flowControl = flowControl;
   }

   public int getSpeed(){
      return speed;
   }

   public PORT_FLOW_CONTROL getFlowControl(){
      return flowControl;
   }

   @Override
   public String toString(){
      return "SerialPortMode [speed=" + speed + ", flowControl=" + flowControl + "]";
   }
}
