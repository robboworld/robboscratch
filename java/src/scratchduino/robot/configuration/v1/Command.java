package scratchduino.robot.configuration.v1;

import java.io.*;
import java.nio.*;
import java.util.*;
import javax.xml.bind.*;
import org.apache.commons.logging.*;
import scratchduino.robot.*;
import scratchduino.robot.connector.v1.*;



public class Command implements ICommand{
   private static Log log = LogFactory.getLog(Main.class);


   private final XCommand xCommand;
   protected final Map<String, Object> mapResponse = new LinkedHashMap<String, Object>();


   private final String sFileParameters;
   
   
   private final static String EMPTY_CRC  = "24"; //every command starts with $
   private final static byte RESP_MARK = 0x23; //every response starts with #


   public Command(String sFileParameters) throws Exception{
      final String LOG = "[Command] ";

      this.sFileParameters = sFileParameters;

      JAXBContext jc = JAXBContext.newInstance(XCommand.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();

      InputStream in = this.getClass().getClassLoader().getResourceAsStream(sFileParameters);
      xCommand = (XCommand) JAXBIntrospector.getValue(unmarshaller.unmarshal(in));


      log.debug(LOG + xCommand);
   }




   public synchronized Response run(final long CID, IPort port, List<String> listParams) throws Exception{
      Thread.currentThread().setName("Command: " + port.getPortName() + " CID=" + CID);

      final String LOG = "[" + port.getPortName() + "] ";
      log.trace(LOG + "params=" + sFileParameters);



      if(port.getStatus() == IPort.STATUS.ROBOT_DETECTED){
         //ok, let's find out how much space we need
         
         
         int iLength = 1;
         for(XCommand.XResponseValue responseValue : xCommand.response.listValues){
            if("unsigned int".equals(responseValue.type)) {
               iLength += 2;
            }
            else if("unsigned byte".equals(responseValue.type)) {
               iLength += 1;
            }
            else{
               throw new Exception("Unknown data type=" + responseValue.type);
            }
         }
         log.trace(LOG + "We need " + iLength + " bytes.");


         StringBuilder sbOut = new StringBuilder(xCommand.code);
         for(String sParam : listParams){
            if(sParam.length() < 2){
               sbOut.append("0");
            }
            sbOut.append(sParam);
         }
         sbOut.append(EMPTY_CRC);
         
         byte[] arrbyteRequest = DatatypeConverter.parseHexBinary(sbOut.toString());
         log.trace(LOG + "OUT=" + byteArrayToHex(arrbyteRequest));

         
         byte[] arrbyteResponse = port.write(CID, arrbyteRequest , iLength);


         ByteBuffer bbuf = ByteBuffer.wrap(arrbyteResponse);
         
         if(bbuf.get() == RESP_MARK){
            //Seems ok            
         }
         else{
            throw new Exception("Synchronization lost");
         }
         

         for(XCommand.XResponseValue responseValue : xCommand.response.listValues){
            if("unsigned int".equals(responseValue.type)) {
               int byteunsignedHigh = bbuf.get() & 0xFF;
               int byteunsignedLow = bbuf.get() & 0xFF;
               mapResponse.put(responseValue.name, new Integer(byteunsignedHigh) * 256 + byteunsignedLow);
            }
            else if("unsigned byte".equals(responseValue.type)) {
               byte byteValue = bbuf.get();
               mapResponse.put(responseValue.name, new Integer(byteValue));
            }
            else{
               throw new Exception("Unknown data type=" + responseValue.type);
            }
         }


         LinkedHashMap<String, Object> parsedValues = new LinkedHashMap<String, Object>(mapResponse);
         Response response = new Response(arrbyteResponse, parsedValues);

         return response;
      }
      else{
         throw new Exception("Port is closed");
      }
   }


   



   public static String byteArrayToHex(byte[] a){
      StringBuilder sb = new StringBuilder(a.length * 2);
      for(byte b : a){
         if(sb.length() > 0){
            sb.append(",");
         }
         sb.append("0x" + String.format("%02x", b & 0xff));
      }
      return sb.toString();
   }
}
