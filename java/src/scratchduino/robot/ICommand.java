package scratchduino.robot;

import java.util.*;


public interface ICommand{
   
   public IResponse run(long CID, IPort port, List<String> listParams) throws Exception;

}
