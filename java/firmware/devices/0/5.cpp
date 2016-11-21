#define FIRMWARE_VERSION "00005"


#include <EEPROM.h>
#include "Arduino.h"
#include <Servo.h>
Servo myservo;

#define SERIAL_SPEED 115200
#define SERIAL_ADDRESS 0


#define CONNECTION_LOST_TIME_INTERVAL 2000







const byte MOTOR_STATE_DISABLED=0;
const byte MOTOR_STATE_ENABLED=1;
const byte MOTOR_STATE_STEPS_LIMIT=2;

const byte DIRECTION_FORWARD=0;
const byte DIRECTION_BACKWARD=0xFF;

struct Motor{
public:
    byte direction;

    volatile byte currentSpeed;

    byte maxSpeed;

    volatile unsigned int stepsLimit;

    volatile unsigned int stepsCnt;
    volatile unsigned int stepsDistance;
    volatile unsigned int stepsPath;

    byte speedTable[64];
    byte directionPin;
    byte speedPin;

    void initMotor(byte dPin,byte sPin){
        direction=DIRECTION_FORWARD;
        currentSpeed=0;
        maxSpeed=32;
        stepsLimit=0;

        stepsCnt=0;

        directionPin=dPin;
        speedPin=sPin;

        pinMode(directionPin,OUTPUT);
        pinMode(speedPin,OUTPUT);
    }

    void stop(){
        currentSpeed=0;
        analogWrite(speedPin,0);
        analogWrite(directionPin,0);
    }


    void enableAndSetStepsLimit(unsigned int iStepsLimit){
        stepsCnt = 0;
        stepsLimit = iStepsLimit;
    }






    void setSpeedAndDirection(byte speed, byte dir){
        direction=dir;
        maxSpeed=speed;
        currentSpeed=maxSpeed;

        if (dir==0){
           analogWrite(speedPin, currentSpeed*4);
           analogWrite(directionPin,0);
        }
        else{
           analogWrite(speedPin,0);
           analogWrite(directionPin, currentSpeed*4);
        }
    }








   void onStep(){
      if(stepsLimit > 0){
         stepsCnt++;
      }

      stepsPath++;

      if(direction == DIRECTION_FORWARD){
         stepsDistance++;
      }
      else{
         stepsDistance--;
      }


      if(stepsCnt > 65535){
         stepsCnt = 0;
      }
      if(stepsPath > 65535){
         stepsPath = 0;
      }



      if(stepsDistance > 65535){
         stepsDistance = 0;
      }
   }

};




byte commandState;
const byte COMMAND_STATE_WAITING_COMMAND=0;
const byte COMMAND_STATE_WAITING_COMMAND_TYPE=1;
const byte COMMAND_STATE_WAITING_DATA=2;
const byte COMMAND_STATE_WAITING_CRC=3;
const byte COMMAND_STATE_EXECUTING=4;

byte commandType;


Motor leftMotor;
Motor rightMotor;







void onLeftMotorStep(){
   leftMotor.onStep();

   if((leftMotor.stepsLimit > 0 || rightMotor.stepsLimit > 0) && (leftMotor.stepsCnt >= leftMotor.stepsLimit || rightMotor.stepsCnt >= rightMotor.stepsLimit)){
       leftMotor.stepsLimit = 0;
       rightMotor.stepsLimit = 0;
       leftMotor.stop();
       rightMotor.stop();
   }

}

void onRightMotorStep(){
   rightMotor.onStep();

   if((leftMotor.stepsLimit > 0 || rightMotor.stepsLimit > 0) && (leftMotor.stepsCnt >= leftMotor.stepsLimit || rightMotor.stepsCnt >= rightMotor.stepsLimit)){
       leftMotor.stepsLimit = 0;
       rightMotor.stepsLimit = 0;
       leftMotor.stop();
       rightMotor.stop();
   }

}




char chararrSerialRaw[50];
char chararrModel[21];
char chararrVersion[21];
char chararrPart[21];
char chararrSerial[21];
int MODEL_ID;



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


   if(strcmp(chararrModel, "R") == 0 && strcmp(chararrVersion, "1") == 0 && strcmp(chararrPart, "1") == 0){
      MODEL_ID=0;
   }
   else if(strcmp(chararrModel, "L") == 0 && strcmp(chararrVersion, "1") == 0 && strcmp(chararrPart, "1") == 0){
      MODEL_ID=1;
   }
   else{
      MODEL_ID=0;
   }
}







void setup(){
   //Let's load S/N
   parseSerialNumber();


   Serial.begin(SERIAL_SPEED);


   pinMode(4, OUTPUT);
   pinMode(7, OUTPUT);
   pinMode(8, OUTPUT);
   pinMode(11, OUTPUT);
   pinMode(12, OUTPUT);

   //Let's set the PWR timer
   bitSet(TCCR1B, WGM12);

   myservo.attach(13);


   commandState=COMMAND_STATE_WAITING_COMMAND;


   leftMotor.initMotor(10,9);
   rightMotor.initMotor(6,5);

   attachInterrupt(1,onLeftMotorStep,CHANGE);
   attachInterrupt(0,onRightMotorStep,CHANGE);
}



void printSensors(){

    Serial.write('#');


    Serial.write( (byte)((leftMotor.stepsCnt >> 8) & 0xff));
    Serial.write( (byte)((leftMotor.stepsCnt) & 0xff));
    Serial.write( (byte)((rightMotor.stepsCnt >> 8) & 0xff));
    Serial.write( (byte)((rightMotor.stepsCnt) & 0xff));


    Serial.write( (byte)((leftMotor.stepsPath >> 8) & 0xff));
    Serial.write( (byte)((leftMotor.stepsPath) & 0xff));
    Serial.write( (byte)((rightMotor.stepsPath >> 8) & 0xff));
    Serial.write( (byte)((rightMotor.stepsPath) & 0xff));










    int sensorValue;

    sensorValue = int(analogRead(A1) / 1023.0 * 100);
    Serial.write(sensorValue);
    sensorValue = int(analogRead(A2) / 1023.0 * 100);
    Serial.write(sensorValue);
    sensorValue = int(analogRead(A3) / 1023.0 * 100);
    Serial.write(sensorValue);
    sensorValue = int(analogRead(A4) / 1023.0 * 100);
    Serial.write(sensorValue);
    sensorValue = int(analogRead(A5) / 1023.0 * 100);
    Serial.write(sensorValue);
    sensorValue = int(analogRead(A0) / 1023.0 * 100);
    Serial.write(sensorValue);

}





void printSensorDebug(){
    Serial.print("\nL:[steps=" + String(leftMotor.stepsCnt)
                 + " limit=" + String(leftMotor.stepsLimit)
                 + " path=" + String(leftMotor.stepsPath)
                 + " distance=" + String(leftMotor.stepsDistance)
                 + "]");
    Serial.print("\nR:[stepsR=" + String(rightMotor.stepsCnt)
                 + " limit=" + String(rightMotor.stepsLimit)
                 + " path=" + String(rightMotor.stepsPath)
                 + " distance=" + String(rightMotor.stepsDistance)
                 + "]");
}




byte bytearrayData[20];
byte byteDataTail=0;
byte command=0;

unsigned long lastReceivedCommandTime = millis();

void loop(){

   if(millis() - lastReceivedCommandTime > CONNECTION_LOST_TIME_INTERVAL){
      //ops, we've lost the connection
      //Stop! Stop!

      leftMotor.stop();
      rightMotor.stop();
   }


   if(Serial.available()){

      byte b=Serial.read();
      lastReceivedCommandTime = millis();



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
               Serial.print(FIRMWARE_VERSION);
               Serial.write('-');


               for(int f = strlen(chararrSerial); f < 20; f++){
                  Serial.write('0');
               }
               Serial.print(chararrSerial);

               break;
            }
            case '!':{
               Serial.print(chararrSerialRaw);

               break;
            }
            case 'a':{
               command = b;
               commandState = COMMAND_STATE_WAITING_CRC;
               break;
            }
            case 'b':{
               command = b;
               commandState = COMMAND_STATE_WAITING_CRC;
               break;
            }
            case 'c':{
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               command = b;
               break;
            }
            case 'd':{
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               command = b;
               break;
            }
            case 'e':{
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               command = b;
               break;
            }
            case 'f':{
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               command = b;
               break;
            }
            case 'g':{
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               command = b;
               break;
            }
            case 'h':{
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               command = b;
               break;
            }
            case 'i':{
               commandState = COMMAND_STATE_WAITING_DATA;
               byteDataTail = 0;
               command = b;
               break;
            }
         }
      }
      else if(commandState==COMMAND_STATE_WAITING_DATA){
         bytearrayData[byteDataTail] = b;
         byteDataTail++;

         switch(command){
            case 'c':{
               if(byteDataTail > 1){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'd':{
               if(byteDataTail > 1){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'e':{
               if(byteDataTail > 1){
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
               if(byteDataTail > 3){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'h':{
               if(byteDataTail > 0){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'i':{
               if(byteDataTail > 0){
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
                  printSensorDebug();
                  break;
               }
               case 'c':{
                  byte leftSpeed=bytearrayData[0];
                  byte rightSpeed=bytearrayData[1];

                  byte leftDir=DIRECTION_FORWARD;
                  byte rightDir=DIRECTION_FORWARD;

                  if( leftSpeed>=64){
                      leftDir=DIRECTION_BACKWARD;
                      leftSpeed -= 64;
                  }
                  if( rightSpeed >= 64){
                      rightDir=DIRECTION_BACKWARD;
                      rightSpeed -= 64;
                  }

                  leftMotor.setSpeedAndDirection(leftSpeed,leftDir);
                  rightMotor.setSpeedAndDirection(rightSpeed,rightDir);

                  printSensors();
                  break;
               }
               case 'd':{
                  byte leftSpeed=bytearrayData[0] - '0';
                  byte rightSpeed=bytearrayData[1] - '0';

                  byte leftDir=DIRECTION_FORWARD;
                  byte rightDir=DIRECTION_FORWARD;

                  if( leftSpeed>=64){
                      leftDir=DIRECTION_BACKWARD;
                      leftSpeed-=64;
                  }
                  if( rightSpeed>=64){
                      rightDir=DIRECTION_BACKWARD;
                      rightSpeed-=64;
                  }

                  leftMotor.setSpeedAndDirection(leftSpeed,leftDir);
                  rightMotor.setSpeedAndDirection(rightSpeed,rightDir);

                  printSensorDebug();
                  break;
               }
               case 'e':{
                  byte stepsHigh=bytearrayData[0];
                  byte stepsLow=bytearrayData[1];

                  unsigned int iStepsLimit = stepsHigh;
                  iStepsLimit = iStepsLimit << 8;
                  iStepsLimit += stepsLow;

                  leftMotor.enableAndSetStepsLimit(iStepsLimit);
                  rightMotor.enableAndSetStepsLimit(iStepsLimit);

                  printSensors();
                  break;
               }
               case 'f':{
                  byte steps=bytearrayData[0] - '0';

                  leftMotor.enableAndSetStepsLimit(steps);
                  rightMotor.enableAndSetStepsLimit(steps);

                  printSensorDebug();
                  break;
               }
               case 'g':{
                  byte leftSpeed=bytearrayData[0];
                  byte rightSpeed=bytearrayData[1];
                  byte stepsHigh=bytearrayData[2];
                  byte stepsLow=bytearrayData[3];

                  byte leftDir=DIRECTION_FORWARD;
                  byte rightDir=DIRECTION_FORWARD;

                  if( leftSpeed>=64){
                      leftDir=DIRECTION_BACKWARD;
                      leftSpeed -= 64;
                  }
                  if( rightSpeed >= 64){
                      rightDir=DIRECTION_BACKWARD;
                      rightSpeed -= 64;
                  }

                  unsigned int iStepsLimit = stepsHigh;
                  iStepsLimit = iStepsLimit << 8;
                  iStepsLimit += stepsLow;

                  leftMotor.setSpeedAndDirection(leftSpeed, leftDir);
                  rightMotor.setSpeedAndDirection(rightSpeed, rightDir);

                  leftMotor.enableAndSetStepsLimit(iStepsLimit);
                  rightMotor.enableAndSetStepsLimit(iStepsLimit);

                  printSensors();
                  break;
               }
               case 'h':{
                  byte lamps=bytearrayData[0];
                  if(lamps & 1){
                     digitalWrite(4, HIGH);
                  }
                  else{
                     digitalWrite(4, LOW);
                  }

                  if(lamps & 2){
                     digitalWrite(7, HIGH);
                  }
                  else{
                     digitalWrite(7, LOW);
                  }


                  if(lamps & 4){
                     digitalWrite(8, HIGH);
                  }
                  else{
                     digitalWrite(8, LOW);
                  }


                  if(lamps & 8){
                     digitalWrite(11, HIGH);
                  }
                  else{
                     digitalWrite(11, LOW);
                  }


                  if(lamps & 16){
                     digitalWrite(12, HIGH);
                  }
                  else{
                     digitalWrite(12, LOW);
                  }




                  printSensors();
                  break;
               }
               case 'i':{
                  myservo.write(bytearrayData[0]);

                  printSensors();
                  break;
               }
            }
         }
         commandState=COMMAND_STATE_WAITING_COMMAND;
      }
   }
}











