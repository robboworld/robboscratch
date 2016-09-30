package scratchduino.robot;

import org.springframework.context.*;
import org.springframework.context.support.*;

public class Context{
   public static final ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
}
