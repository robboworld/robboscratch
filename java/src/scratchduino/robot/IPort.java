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
   
   public String  getPortName();
   public STATUS  getStatus();
   public IConnectedDevice getDevice();
   public void    close();
   byte[] write(long CID, byte[] data, int iLength);
}
