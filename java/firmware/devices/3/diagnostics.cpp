#define SERIAL_SPEED 38400

#define PIN_LIGHT 4
#define PIN_SOUND 3


void parseSerialNumber();
void setup();
void loop();
int MODEL_ID;



void parseSerialNumber(){
    if(analogRead(PIN_SOUND) < 600 && analogRead(PIN_LIGHT) > 200){
       MODEL_ID=4;
    }
    else{
       MODEL_ID=3;
    }
}







void setup(){
    parseSerialNumber();

    Serial.begin(SERIAL_SPEED);
}



void loop(){
      Serial.print(F("ROBBO-"));
      if(MODEL_ID < 10000) Serial.write('0');      
      if(MODEL_ID < 1000)  Serial.write('0');
      if(MODEL_ID < 100)   Serial.write('0');
      if(MODEL_ID < 10)    Serial.write('0');
      
      Serial.print(MODEL_ID);

      Serial.print(".");


      delay(1000);
}



