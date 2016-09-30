package scratchduino.robot;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class TestPortWin8 {

    private static SerialPort serialPort;

    public static void main(String[] args) {
        //Передаём в конструктор имя порта
        serialPort = new SerialPort(args[0]);
        try {
            //Открываем порт
            serialPort.openPort();
            //Выставляем параметры
            serialPort.setParams(SerialPort.BAUDRATE_115200,
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);
            //Включаем аппаратное управление потоком
//            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | 
//                                          SerialPort.FLOWCONTROL_RTSCTS_OUT);
            //Устанавливаем ивент лисенер и маску
            //serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
            //Отправляем запрос устройству
            
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            
            while(true) {
                  byte[] moveForward = {0x63, 48, 48, 36};
                  byte[] moveBackward = {0x63, 122, 122, 36};
                  serialPort.writeBytes(moveForward);
                  System.out.println(serialPort.readBytes(15));
                  serialPort.writeBytes(moveBackward);
                  System.out.println(serialPort.readBytes(15));
            }
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    private static class PortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    String data = serialPort.readString(event.getEventValue());
                    //И снова отправляем запрос
                    System.out.println(data);
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}