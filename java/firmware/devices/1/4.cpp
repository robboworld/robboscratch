#define FIRMWARE_VERSION "00004"

#include <EEPROM.h>
#include "Arduino.h"

#define SERIAL_SPEED 115200
#define SERIAL_ADDRESS 0


#define data 2
#define clock 4




char chararrSerialRaw[50];
char chararrModel[21];
char chararrVersion[21];
char chararrPart[21];
char chararrSerial[21];
int MODEL_ID;





int notes[] = {262, 277, 294, 311, 330, 349, 370, 392, 415, 440, 466, 494, 523, 554, 587, 622, 659, 698, 740, 784, 831, 880, 932, 988, 1047};


byte commandState;
const byte COMMAND_STATE_WAITING_COMMAND=0;
const byte COMMAND_STATE_WAITING_COMMAND_TYPE=1;
const byte COMMAND_STATE_WAITING_DATA=2;
const byte COMMAND_STATE_WAITING_CRC=3;
const byte COMMAND_STATE_EXECUTING=4;




unsigned long startMillis= millis();  // Start of sample window
unsigned int soundVolume = 0;   // peak-to-peak level

unsigned int signalMax = 0;
unsigned int signalMin = 1024;


const int samplingWindow = 50; // Sample window width in mS (50 mS = 20Hz)
unsigned int sample;
const int micPin = A3;




void parseSerialNumber(){
    EEPROM.get(SERIAL_ADDRESS, chararrSerialRaw);

    int iPointer = 0;
    while(chararrSerialRaw[iPointer] != '-'){
      iPointer++;
    }
    iPointer++;

    int iModelOffset = 0;
    while(chararrSerialRaw[iPointer] != '-'){
      chararrModel[iModelOffset] = chararrSerialRaw[iPointer];
      iModelOffset++;
      iPointer++;
    }
    iPointer++;

    int iVersionOffset = 0;
    while(chararrSerialRaw[iPointer] != '-'){
      chararrVersion[iVersionOffset] = chararrSerialRaw[iPointer];
      iVersionOffset++;
      iPointer++;
    }

    iPointer++;

    int iPartOffset = 0;
    while(chararrSerialRaw[iPointer] != '-'){
      chararrPart[iPartOffset] = chararrSerialRaw[iPointer];
      iPartOffset++;
      iPointer++;
    }

    iPointer++;


    int iSerialOffset = 0;
    while(chararrSerialRaw[iPointer] != 0 && (chararrSerialRaw[iPointer] >= '0' && chararrSerialRaw[iPointer] <='9')){
      chararrSerial[iSerialOffset] = chararrSerialRaw[iPointer];
      iSerialOffset++;
      iPointer++;
    }


    if(strcmp(chararrModel, "R") == 0
       && strcmp(chararrVersion, "1") == 0
       && (strcmp(chararrPart, "1") == 0 || strcmp(chararrPart, "2") == 0 || strcmp(chararrPart, "3") == 0 || strcmp(chararrPart, "4") == 0)){

       MODEL_ID=0;
    }
    else if(strcmp(chararrModel, "L") == 0
       && strcmp(chararrVersion, "1") == 0
       && strcmp(chararrPart, "1") == 0){

       MODEL_ID=1;
    }
    else if(strcmp(chararrModel, "L") == 0
       && strcmp(chararrVersion, "3") == 0
       && (strcmp(chararrPart, "1") == 0 || strcmp(chararrPart, "2") == 0 || strcmp(chararrPart, "3") == 0)){

       MODEL_ID=2;
    }
    else{
       MODEL_ID=9999;
    }


}







void setup(){
    parseSerialNumber();
    Serial.begin(SERIAL_SPEED);

    commandState=COMMAND_STATE_WAITING_COMMAND;


    pinMode(clock, OUTPUT);
    pinMode(data , OUTPUT);
    pinMode(A0 , OUTPUT);


    pinMode(2, OUTPUT);
    pinMode(3, OUTPUT);
    pinMode(4, OUTPUT);
    pinMode(5, OUTPUT);
    pinMode(6, OUTPUT);
    pinMode(7, OUTPUT);



    //Lab greetings!
    //Flashing lamps & bip-bip-bip
    digitalWrite(A0, HIGH);
    for(int i = 0; i < 8; ++i)
    {
        shiftOut(data, clock, MSBFIRST, 1 << i);
        tone(3, 200 * (i + 1), 100);
        delay(100);
    }
    shiftOut(data, clock, MSBFIRST, 0);
}



void printSensors(){

   Serial.write('#');

   byte bValue = 0;

   if(digitalRead(8) == HIGH){
      bValue |= 1;
   }
   if(digitalRead(9) == HIGH){
      bValue |= 2;
   }
   if(digitalRead(10) == HIGH){
      bValue |= 4;
   }
   if(digitalRead(11) == HIGH){
      bValue |= 8;
   }
   if(digitalRead(12) == HIGH){
      bValue |= 16;
   }
   if(digitalRead(13) == HIGH){
      bValue |= 32;
   }
   Serial.write(bValue);



   unsigned int iValue = int(analogRead(0));
   Serial.write(0x00);
   Serial.write(0x00);
   Serial.write((byte)(iValue >> 8));
   Serial.write((byte)(iValue));

   iValue = int(analogRead(1));
   Serial.write(0x00);
   Serial.write(0x00);
   Serial.write((byte)(iValue >> 8));
   Serial.write((byte)(iValue));

   iValue = int(analogRead(2));
   Serial.write(0x00);
   Serial.write(0x00);
   Serial.write((byte)(iValue >> 8));
   Serial.write((byte)(iValue));

   iValue = soundVolume;
   Serial.write(0x00);
   Serial.write(0x00);
   Serial.write((byte)(iValue >> 8));
   Serial.write((byte)(iValue));

   iValue = int(analogRead(4));
   Serial.write(0x00);
   Serial.write(0x00);
   Serial.write((byte)(iValue >> 8));
   Serial.write((byte)(iValue));

   iValue = int(analogRead(5));
   Serial.write(0x00);
   Serial.write(0x00);
   Serial.write((byte)(iValue >> 8));
   Serial.write((byte)(iValue));
}



byte bytearrayData[20];
byte byteDataTail=0;
byte command=0;








void loop(){
   if( Serial.available() ){
      byte b = Serial.read();


      if(commandState== COMMAND_STATE_WAITING_COMMAND){
         switch(b){
            case ' ':{
               Serial.print(F("ROBBO-"));
               if(MODEL_ID < 10000){
                  Serial.write('0');
               }
               if(MODEL_ID < 1000){
                  Serial.write('0');
               }
               if(MODEL_ID < 100){
                  Serial.write('0');
               }
               if(MODEL_ID < 10){
                  Serial.write('0');
               }
               Serial.print(MODEL_ID);



               Serial.write('-');
               Serial.print(F(FIRMWARE_VERSION));


               Serial.write('-');
               Serial.print(chararrModel);


               Serial.print('-');
               for(int f = strlen(chararrVersion); f < 5; f++){
                  Serial.write('0');
               }
               Serial.print(chararrVersion);




               Serial.print('-');
               for(int f = strlen(chararrPart); f < 5; f++){
                  Serial.write('0');
               }
               Serial.print(chararrPart);




               Serial.print('-');
               for(int f = strlen(chararrSerial); f < 20; f++){
                  Serial.write('0');
               }
               Serial.print(chararrSerial);


               break;
            }
            case 'a':{
               command = b;
               commandState = COMMAND_STATE_WAITING_CRC;
               break;
            }
            case 'b':{
               command = b;
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               break;
            }
            case 'c':{
               command = b;
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               break;
            }
            case 'd':{
               command = b;
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               break;
            }
            case 'e':{
               command = b;
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               break;
            }
            case 'f':{
               command = b;
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               break;
            }
            case 'g':{
               command = b;
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               break;
            }
         }
      }
      else if(commandState==COMMAND_STATE_WAITING_DATA){
         bytearrayData[byteDataTail] = b;
         byteDataTail++;

         switch(command){
            case 'b':{
               if(byteDataTail > 0){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'c':{
               if(byteDataTail > 0){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'd':{
               if(byteDataTail > 0){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'e':{
               if(byteDataTail > 0){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'f':{
               if(byteDataTail > 0){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'g':{
               if(byteDataTail > 1){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
         }
      }
      else if(commandState==COMMAND_STATE_WAITING_CRC){

         if(b == '$'){
            switch(command){
               case 'a':{
                  printSensors();
                  break;
               }
               case 'b':{
                  byte lamps=bytearrayData[0];
                  shiftOut(data, clock, MSBFIRST, lamps);

                  printSensors();
                  break;
               }
               case 'c':{
                  byte lamps=bytearrayData[0];

                  if(lamps & 1){
                     digitalWrite(5, HIGH);
                  }
                  else{
                     digitalWrite(5, LOW);
                  }

                  if(lamps & 2){
                     digitalWrite(6, HIGH);
                  }
                  else{
                     digitalWrite(6, LOW);
                  }

                  if(lamps & 4){
                     digitalWrite(7, HIGH);
                  }
                  else{
                     digitalWrite(7, LOW);
                  }

                  printSensors();
                  break;
               }

               case 'd':{
                  byte sound=bytearrayData[0];

                  tone(3, notes[sound], 100);
                  delay(100);

                  printSensors();
                  break;
               }

               case 'e':{
                  byte pin = bytearrayData[0];

                  if(pin & 1){
                    digitalWrite(2, HIGH);
                  }
                  if(pin & 2){
                    digitalWrite(3, HIGH);
                  }
                  if(pin & 4){
                    digitalWrite(4, HIGH);
                  }
                  if(pin & 8){
                    digitalWrite(5, HIGH);
                  }
                  if(pin & 16){
                    digitalWrite(6, HIGH);
                  }
                  if(pin & 32){
                    digitalWrite(7, HIGH);
                  }

                  printSensors();
                  break;
               }

               case 'f':{
                  byte pin = bytearrayData[0];

                  if(pin & 1){
                    digitalWrite(2, LOW);
                  }
                  if(pin & 2){
                    digitalWrite(3, LOW);
                  }
                  if(pin & 4){
                    digitalWrite(4, LOW);
                  }
                  if(pin & 8){
                    digitalWrite(5, LOW);
                  }
                  if(pin & 16){
                    digitalWrite(6, LOW);
                  }
                  if(pin & 32){
                    digitalWrite(7, LOW);
                  }

                  printSensors();
                  break;
               }

               case 'g':{
                  byte pin  =  bytearrayData[0];
                  byte value = bytearrayData[1];

                  if(pin & 1){
                    analogWrite(3, value);
                  }
                  if(pin & 2){
                    analogWrite(5, value);
                  }
                  if(pin & 4){
                    analogWrite(6, value);
                  }

                  printSensors();
                  break;
               }
            }
         }
         commandState=COMMAND_STATE_WAITING_COMMAND;
      }
   }





   // collect data for 50 mS
   if (millis() - startMillis < samplingWindow)
   {
      sample = analogRead(micPin);
      if (sample < 1024)  // toss out spurious readings
      {
         if (sample > signalMax)
         {
            signalMax = sample;  // save just the max levels
         }
         else if (sample < signalMin)
         {
            signalMin = sample;  // save just the min levels
         }
      }
   }
   else{
      double peakToPeak = signalMax - signalMin;  // max - min = peak-peak amplitude
      //No more 0.5, the sound is ok without that
      //soundVolume = peakToPeak * 0.5;
      soundVolume = peakToPeak;

      startMillis= millis();  // Start of sample window
      peakToPeak = 0;   // peak-to-peak level

      signalMax = 0;
      signalMin = 1024;
   }
}











