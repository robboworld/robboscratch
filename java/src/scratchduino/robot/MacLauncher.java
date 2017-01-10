package scratchduino.robot;


public class MacLauncher{

   public static void main(String[] args) throws Throwable{
      String separator = System.getProperty("file.separator");
      String classpath = System.getProperty("java.class.path");
      String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
      ProcessBuilder processBuilder = new ProcessBuilder(path, "-cp", classpath, Main.class.getName());
      processBuilder.start();
   }
}
