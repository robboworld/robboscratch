package scratchduino.robot;

public interface IControlPanel{
   
   void popUp();
   
   
   void reconnect();
   
   
   void lock(String sMessage);
   void unlock();
   
   void dialogLoadReset();
   void dialogSaveReset();
   String dialogLoadCheck();
   String dialogSaveCheck();
   byte[] dialogLoadData();
   void dialogSave(byte[] data);
   void dialogSaveAs(String sName, byte[] data);
   void dialogSaveTmp(byte[] data);
}
