package scratchduino.robot;

public interface ISerialPortMode{
   enum PORT_FLOW_CONTROL {
      NONE, RTS_CTS
   };


   public int getSpeed();
   public PORT_FLOW_CONTROL getFlowControl();
}
