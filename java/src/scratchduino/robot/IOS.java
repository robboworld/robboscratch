package scratchduino.robot;

public interface IOS{
   
   public enum TYPE{UNKNOWN, WINDOWS, LINUX, MAC}
   
   TYPE getType();

}
