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
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + ((flowControl == null) ? 0 : flowControl.hashCode());
      result = prime * result + speed;
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if(this == obj)
         return true;
      if(obj == null)
         return false;
      if(getClass() != obj.getClass())
         return false;
      SerialPortMode other = (SerialPortMode) obj;
      if(flowControl != other.flowControl)
         return false;
      if(speed != other.speed)
         return false;
      return true;
   }

   @Override
   public String toString(){
      return "SerialPortMode [speed=" + speed + ", flowControl=" + flowControl + "]";
   }
}
