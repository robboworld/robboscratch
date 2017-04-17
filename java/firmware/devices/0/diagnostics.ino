#include <EEPROM.h>

#define SERIAL_SPEED 115200
#define SERIAL_ADDRESS 0


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
    while(chararrSerialRaw[iPointer] != 0){
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
}



void loop(){
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

      Serial.write('.');

      delay(1000);
}

