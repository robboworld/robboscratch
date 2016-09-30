package scratchduino.robot;

import java.util.regex.*;

public class B
{
  public static void main(String[] args)
  {
    String sAVRDudeVersion = "avrdude version 6.0.1, URL: <http://savannah.nongnu.org/projects/avrdude/>";
    Pattern p = Pattern.compile("\\d+\\.\\d+\\.\\d+");
    
    Matcher matcher = p.matcher(sAVRDudeVersion);
    if (matcher.find()) {
      System.out.println(matcher.group(0));
    }
  }
}
