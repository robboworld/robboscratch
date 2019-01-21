#define FIRMWARE_VERSION "00002"


#include <EEPROM.h>
#include "Arduino.h"
#include "servotimer2.h"

#define SERIAL_SPEED 38400
#define SERIAL_ADDRESS 0


#define CONNECTION_LOST_TIME_INTERVAL 2000



#define SENSOR_TYPE_NONE 0
#define SENSOR_TYPE_LINE 1
#define SENSOR_TYPE_LED  2
#define SENSOR_TYPE_LIGHT 3
#define SENSOR_TYPE_TOUCH 4
#define SENSOR_TYPE_PROXIMITY 5
#define SENSOR_TYPE_ULTRASONIC 6
#define SENSOR_TYPE_COLOR 7




#define ANALOG_PIN_1 A3
#define ANALOG_PIN_2 A4
#define ANALOG_PIN_3 A2
#define ANALOG_PIN_4 A1
#define ANALOG_PIN_5 A0




#define DIGITAL_PIN_1 4
#define DIGITAL_PIN_2 7
#define DIGITAL_PIN_3 8
#define DIGITAL_PIN_4 11
#define DIGITAL_PIN_5 12



ServoTimer2 myservo;


#define SENSOR_RESPONSE_LENGTH 4




const byte MOTOR_STATE_DISABLED=0;
const byte MOTOR_STATE_ENABLED=1;
const byte MOTOR_STATE_STEPS_LIMIT=2;

const byte DIRECTION_FORWARD=0;
const byte DIRECTION_BACKWARD=0xFF;



#define PIN_LIGHT 4
#define PIN_SOUND 3





byte commandState;
const byte COMMAND_STATE_WAITING_COMMAND=0;
const byte COMMAND_STATE_WAITING_COMMAND_TYPE=1;
const byte COMMAND_STATE_WAITING_DATA=2;
const byte COMMAND_STATE_WAITING_CRC=3;
const byte COMMAND_STATE_EXECUTING=4;

byte commandType;









int MODEL_ID;



void parseSerialNumber(){
    if(analogRead(PIN_SOUND) < 600 && analogRead(PIN_LIGHT) > 200){
       MODEL_ID=4;
    }
    else{
       MODEL_ID=3;
    }  
}




#define PWM1_PIN 6
#define PWM2_PIN 5
#define WAY1_PIN 7
#define WAY2_PIN 4


class Motor{
  int pin_pwm;
  int pin_direction;
  
  public:
     Motor(int pin_direction, int pin_pwm){
       this -> pin_direction = pin_direction;
       this -> pin_pwm = pin_pwm;
      
       pinMode(this -> pin_pwm, OUTPUT);
       pinMode(this -> pin_direction, OUTPUT);
     };
  
     void stop(){
        analogWrite(pin_pwm, 0);
     }
     
     void power(boolean direction_, int power){
        analogWrite(pin_pwm, (byte) power * 3.9);
        
        if(direction_){
           digitalWrite(pin_direction, HIGH);
        }
        else{
           digitalWrite(pin_direction, LOW);
        }        
     }
};

Motor* motorLeft = new Motor(WAY2_PIN, PWM2_PIN);
Motor* motorRight = new Motor(WAY1_PIN, PWM1_PIN);



class ISensor{
   public:
      virtual void iteration();
      virtual byte* getResult();
};



class AnalogSensor: public ISensor{
   int pin;

   public:
   AnalogSensor(int pin){
      this -> pin = pin;
      pinMode(pin, INPUT);

      //Let's set 1 to digital out
      //So that the the lamps light
      switch(pin){
         case A1:{
            pinMode(DIGITAL_PIN_1, OUTPUT);
            break;
         }
         case A2:{
            pinMode(DIGITAL_PIN_2, OUTPUT);
            break;
         }
         case A3:{
            pinMode(DIGITAL_PIN_3, OUTPUT);
            break;
         }
         case A4:{
            pinMode(DIGITAL_PIN_4, OUTPUT);
            break;
         }
         case A5:{
            pinMode(DIGITAL_PIN_5, OUTPUT);
            break;
         }
      }
   };

   void iteration(){
   }

   byte* getResult(){
      return new byte[SENSOR_RESPONSE_LENGTH]{0, 0, 0, byte(analogRead(pin))};
   }
};




//Transistor schema
/*
class SonicSensor: public ISensor{
   boolean resetMode = true;
   boolean measuremnetWaiting;
   unsigned long time;
   int result = 0;
   int pin;
   public:
   SonicSensor(int pin){
      this -> pin = pin;
   };
   void reset(){
      measuremnetWaiting = false;
      resetMode = true;
      time = micros();
   }
   void iteration(){
      //reset mode with delay
      if(resetMode){
        if(micros() - time > 30000){
            pinMode(pin, OUTPUT);
            digitalWrite(pin, LOW );
            delayMicroseconds(9);
            digitalWrite(pin, HIGH );
            pinMode(pin, INPUT);
            time = micros();
            resetMode = false;
        }
        return;
      }
      // no response
      // we lost the impulse
      if(micros() - time > 100000){
         reset();
         return;
      }
      if(measuremnetWaiting){
         if(HIGH == digitalRead(pin)){
            result = ((micros() - time) *  34000) / 2000000 - 8;
            if(result < 0){
              result = 0;
            }
            reset();
         }
      }
      else{
         if(digitalRead(pin) == LOW){
            measuremnetWaiting = true;
         }
      }
   }
   byte* getResult(){
      return new byte[SENSOR_RESPONSE_LENGTH]{0, 0, 0, result};
   }
};
*/







//Resistor schema
class SonicSensor: public ISensor{
   boolean resetMode = true;
   boolean measuremnetWaiting;
   unsigned long time;
   int result = 0;
   int pin;

   public:
   SonicSensor(int pin){
      this -> pin = pin;
   };



   void reset(){
      measuremnetWaiting = false;
      resetMode = true;
      time = micros();
   }

   void iteration(){
      //reset mode with delay
      if(resetMode){
        if(micros() - time > 30000){
            pinMode(pin, OUTPUT);
            digitalWrite(pin, HIGH);
            delayMicroseconds(9);
            digitalWrite(pin, LOW);
            pinMode(pin, INPUT);
            time = micros();
            resetMode = false;
        }

        return;
      }


      // no response
      // we lost the impulse
      if(micros() - time > 100000){
         reset();
         return;
      }


      if(measuremnetWaiting){
         if(LOW == digitalRead(pin)){
            result = ((micros() - time) *  34000) / 2000000 - 8;
            if(result < 0){
              result = 0;
            }
            reset();
         }
      }
      else{
         if(digitalRead(pin) == HIGH){
            measuremnetWaiting = true;
         }
      }
   }

   byte* getResult(){
      return new byte[SENSOR_RESPONSE_LENGTH]{0, 0, 0, result};
   }
};












class ColorSensor: public ISensor{
   #define FRAME_UNKNOWN 0
   #define FRAME_RED 1
   #define FRAME_GREEN 2
   #define FRAME_BLUE 3
   #define FRAME_BRIGHT 4
   #define FRAME_MAX_LENGTH 122
   #define FRAME_STOP_LENGTH 10
   int pin;


   byte result[4] = {0, 0, 0, 0};
   byte mode = FRAME_UNKNOWN;
   unsigned long time;

   unsigned long timeHigh = 0;
   unsigned long timeLow  = 0;

   boolean synchronized = false;
   boolean terminating = false;

   public:
   ColorSensor(int pin){
      this -> pin = pin;
      pinMode(pin, INPUT);

      time = millis();
      mode = FRAME_UNKNOWN;
   };

   void iteration(){
      //debug current mode
      //Serial.println(mode);
      if (mode == FRAME_UNKNOWN){
         synchronized = false;
         terminating = false;

         if(HIGH == digitalRead(pin)){
            //ok, looks like this is a frame gap

            if(millis() - time > FRAME_MAX_LENGTH){
               //ok, it is long enough to be true
               //let's wait for RED now
               mode = FRAME_RED;
            }
         }
         else{
            //nope, let's reset timer and try again
            time = millis();
         }
      }
      else{
         if(synchronized){
            if(LOW == digitalRead(pin)){
               if(terminating){
                  switch(mode){
                     case FRAME_RED:{
                        result[0] = (unsigned byte) timeLow;
                        mode = FRAME_GREEN;
                        break;
                     }
                     case FRAME_GREEN:{
                        result[1] = (unsigned byte) timeLow;
                        mode = FRAME_BLUE;
                        break;
                     }
                     case FRAME_BLUE:{
                        result[2] = (unsigned byte) timeLow;
                        mode = FRAME_BRIGHT;
                        break;
                     }
                     case FRAME_BRIGHT:{
                        result[3] = (unsigned byte) timeLow;
                        mode = FRAME_UNKNOWN;
                        break;
                     }
                  };
                  time = millis();
                  synchronized = true;
                  terminating = false;
                  timeHigh = 0;
                  timeLow = 0;
               }
               else{
                  //ok, low level
                  //Let's just sum the time
                  timeLow += (millis() - time);
               }
            }
            else{
               //let's check it is the stop stage
               if(timeLow > 0 && timeHigh > 0){
                  terminating = true;
               }

               timeHigh += (millis() - time);
            }

            time = millis();
         }
         else{
            if(LOW == digitalRead(pin)){
               //ok, let's start this frame
               time = millis();
               synchronized = true;
               terminating = false;
               timeHigh = 0;
               timeLow = 0;
            }
         }
      }
   };


   byte* getResult(){
      return new byte[SENSOR_RESPONSE_LENGTH]{result[0], result[1], result[2], result[3]};
   }



   #undef FRAME_UNKNOWN
   #undef FRAME_RED
   #undef FRAME_GREEN
   #undef FRAME_BLUE
   #undef FRAME_MAX_LENGTH
   #undef FRAME_STOP_LENGTH
};



ISensor* sensors[5];







void setup(){
   //Let's load S/N
   parseSerialNumber();

   Serial.begin(SERIAL_SPEED);

   //Let's set the PWR timer
   bitSet(TCCR1B, WGM12);

   myservo.attach(13);

   commandState=COMMAND_STATE_WAITING_COMMAND;




   sensors[0]  = new AnalogSensor(ANALOG_PIN_1);
//   sensors[0]  = new ColorSensor(DIGITAL_PIN_1);
   sensors[1]  = new AnalogSensor(ANALOG_PIN_2);
   sensors[2]  = new AnalogSensor(ANALOG_PIN_3);
   sensors[3]  = new AnalogSensor(ANALOG_PIN_4);
   sensors[4]  = new AnalogSensor(ANALOG_PIN_5);
}







void printSensors(){

    Serial.write('#');

//No Encoders available
    Serial.write(0);
    Serial.write(0);
    Serial.write(0);
    Serial.write(0);
    Serial.write(0);
    Serial.write(0);
    Serial.write(0);
    Serial.write(0);


    for(int i = 0; i < 5; i++){
       byte* result = sensors[i] -> getResult();
       Serial.write(result, SENSOR_RESPONSE_LENGTH);

       delete[] result;
    }

//    No supported
//    int sensorValue = int(analogRead(A0) / 1023.0 * 100);
//    Serial.write(sensorValue);
      Serial.write(0);
}








byte bytearrayData[20];
byte byteDataTail=0;
byte command=0;

unsigned long lastReceivedCommandTime = millis();

void loop(){

   if(millis() - lastReceivedCommandTime > CONNECTION_LOST_TIME_INTERVAL){
      //ops, we've lost the connection
      //Stop! Stop!

      motorLeft -> stop();
      motorRight -> stop();
   }


   if(Serial.available()){

      byte b = Serial.read();
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
               Serial.print(F(FIRMWARE_VERSION));


               Serial.write('-');
               Serial.write(MODEL_ID == 3 ? 'R' : 'L');


               Serial.print(F("-00000"));
               Serial.print(F("-00000"));
               Serial.print(F("-00000000000000000000"));

               break;
            }
            case '!':{
               Serial.print(F("nothing"));

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
            case 'j':{
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
               if(byteDataTail > 4){
                  commandState=COMMAND_STATE_WAITING_CRC;
               }
               break;
            }
            case 'j':{
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
//                  printSensorDebug();
                  break;
               }
               case 'c':{                 
                  commandMotors(bytearrayData[0], bytearrayData[1]);                  
                  
                  printSensors();
                  break;
               }
               case 'd':{
                  commandMotors(bytearrayData[0], bytearrayData[1]);                  

                  //printSensorDebug();
                  break;
               }
               case 'e':{
                  byte stepsHigh=bytearrayData[0];
                  byte stepsLow=bytearrayData[1];

                  unsigned int iStepsLimit = stepsHigh;
                  iStepsLimit = iStepsLimit << 8;
                  iStepsLimit += stepsLow;

                  //not implemented

                  printSensors();
                  break;
               }
               case 'f':{
                  byte steps=bytearrayData[0] - '0';

                  //not implemented

                  //printSensorDebug();
                  break;
               }
               case 'g':{
                  byte leftSpeed=bytearrayData[0];
                  byte rightSpeed=bytearrayData[1];
                  byte stepsHigh=bytearrayData[2];
                  byte stepsLow=bytearrayData[3];


                  commandMotors(bytearrayData[0], bytearrayData[1]);                  

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
                  for(int f = 0; f < 5; f++){
                     delete sensors[f];
                     switch(bytearrayData[f]){
                        case SENSOR_TYPE_NONE:
                        case SENSOR_TYPE_LINE:
                        case SENSOR_TYPE_LED:
                        case SENSOR_TYPE_LIGHT:
                        case SENSOR_TYPE_TOUCH:
                        case SENSOR_TYPE_PROXIMITY:{
                           switch(f){
                              case 0:{
                                 sensors[f]  = new AnalogSensor(ANALOG_PIN_1);
                                 break;
                              }
                              case 1:{
                                 sensors[f]  = new AnalogSensor(ANALOG_PIN_2);
                                 break;
                              }
                              case 2:{
                                 sensors[f]  = new AnalogSensor(ANALOG_PIN_3);
                                 break;
                              }
                              case 3:{
                                 sensors[f]  = new AnalogSensor(ANALOG_PIN_4);
                                 break;
                              }
                              case 4:{
                                 sensors[f]  = new AnalogSensor(ANALOG_PIN_5);
                                 break;
                              }
                           }
                           break;
                        }
                        case SENSOR_TYPE_ULTRASONIC:{
                           switch(f){
                              case 0:{
                                 sensors[f]  = new SonicSensor(DIGITAL_PIN_1);
                                 break;
                              }
                              case 1:{
                                 sensors[f]  = new SonicSensor(DIGITAL_PIN_2);
                                 break;
                              }
                              case 2:{
                                 sensors[f]  = new SonicSensor(DIGITAL_PIN_3);
                                 break;
                              }
                              case 3:{
                                 sensors[f]  = new SonicSensor(DIGITAL_PIN_4);
                                 break;
                              }
                              case 4:{
                                 sensors[f]  = new SonicSensor(DIGITAL_PIN_5);
                                 break;
                              }
                           }
                           break;
                        }
                        case SENSOR_TYPE_COLOR:{
                           switch(f){
                              case 0:{
                                 sensors[f]  = new ColorSensor(DIGITAL_PIN_1);
                                 break;
                              }
                              case 1:{
                                 sensors[f]  = new ColorSensor(DIGITAL_PIN_2);
                                 break;
                              }
                              case 2:{
                                 sensors[f]  = new ColorSensor(DIGITAL_PIN_3);
                                 break;
                              }
                              case 3:{
                                 sensors[f]  = new ColorSensor(DIGITAL_PIN_4);
                                 break;
                              }
                              case 4:{
                                 sensors[f]  = new ColorSensor(DIGITAL_PIN_5);
                                 break;
                              }
                           }
                           break;
                        }
                     }
                  }

                  printSensors();
                  break;
               }
               case 'j':{
                  myservo.write(800 + bytearrayData[0] * 22);

                  printSensors();
                  break;
               }
            }
         }
         commandState=COMMAND_STATE_WAITING_COMMAND;
      }
   }


   for(int f = 0; f < 5; f++){
      sensors[f] -> iteration();
   }
}


void commandMotors(byte leftSpeed, byte rightSpeed){     
   if( leftSpeed>=64){
      leftSpeed -= 64;
      motorLeft -> power(true, leftSpeed);                      
   }
   else{
      motorLeft -> power(false, leftSpeed);                      
   }

   if( rightSpeed >= 64){
      rightSpeed -= 64;
      motorRight -> power(true, rightSpeed);
   }
   else{
      motorRight -> power(false, rightSpeed);
   }    
}

