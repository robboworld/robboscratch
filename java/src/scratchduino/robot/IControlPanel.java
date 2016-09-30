package scratchduino.robot;

public interface IControlPanel{
   
   void popUp();
   
   
   void reconnect();
   
   
   void lock(String sMessage);
   void unlock();
   
   void dialogLoadReset();
   String dialogLoadCheck();
   byte[] dialogLoadData();
   void dialogSave(String sName, byte[] data);
}
