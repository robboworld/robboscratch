package scratchduino.robot;

public interface IPort{
   
   public static enum STATUS{INIT,
                             TERMINATING,
                             TERMINATED,
                             
                             TIME_OUT,
                             ERROR,
                             
                             OPENNED,
                             TEST_DATA,
                             RESPONSE,
                             NO_RESPONSE,
                             
                             ROBOT_DETECTED,
                             WRONG_VERSION,
                             UNKNOWN_DEVICE};
   
   String getPortName();
   STATUS getStatus();
   IConnectedDevice getDevice();
   void close();
   byte[] write(long CID, byte[] data, int iLength);
   int getSpeed();
}
