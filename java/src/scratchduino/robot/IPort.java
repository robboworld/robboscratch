package scratchduino.robot;

public interface IPort{
   
   public static enum PROGRESS{INIT,
                               CLOSING,
                               CLOSED,
                             
                               TIME_OUT,
                               ERROR,
                             
                               OPENNED,
                               TEST_DATA,
                               RESPONSE,
                               NO_RESPONSE,
                               
                               ROBOT_DETECTED,
                               WRONG_VERSION,
                               UNKNOWN_DEVICE};
                               
                               
   public static enum STATE{DETECTION,
                            TIME_OUT,
                            ERROR,
                            ROBOT_DETECTED,
                            WRONG_VERSION,
                            UNKNOWN_DEVICE
                           };                               
   
   String getPortName();
   PROGRESS getProgress();
   STATE    getState();
   IConnectedDevice getDevice();
   void close();
   byte[] write(long CID, byte[] data, int iLength);
   int getSpeed();
}
