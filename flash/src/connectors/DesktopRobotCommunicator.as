/**
 * Created by User on 07.06.2015.
 */
package connectors {

   import flash.net.*;
   import flash.events.*;
   import flash.utils.getTimer;
   import flash.utils.ByteArray;

   import flash.events.TimerEvent;
   import flash.utils.Timer;
   import flash.globalization.DateTimeFormatter;

public class DesktopRobotCommunicator implements IRobotCommunicator {
    private const INTERVAL_SEND:int = 50;//in ms
    public  var lastSendRobot:int = 0;
    public  var lastSendLab:int = 0;
    private var interfaceBusyRobot:Boolean = false;
    private var interfaceBusyRobot2:Boolean = false;
    private var interfaceBusyLab:Boolean = false;

    private var iCounterRequestRobot:int = 0;
    private var iCounterResponseRobot:int = 0;
    private var iCounterRequestLab:int = 0;
    private var iCounterResponseLab:int = 0;

    protected var iAnticacheRobot:int = 0;
    protected var iAnticacheLab:int   = 0;

    private var pointer:int = 0;


    private var speedLeft:int   = 0;
    private var speedRight:int  = 0;
    private var _speedLeft:int  = 100;
    private var _speedRight:int = 100;
    private var isMotionTerminated:Boolean = false;

    private var isEncoder:Boolean = false;

    private var lamps:int = 0;
    private var lampColor:int = 0;
    private var robLamps:int = 0;


    private var analogsLab:Array   = [];



    private var onDataReceiveRobot:Function;
    private var onDataReceiveLab:Function;


    private var loaderRobot:URLLoader = new URLLoader();
    private var loaderLab:URLLoader = new URLLoader();


    private var ledState:Array = [false, false, false, false, false, false, false, false];
    private var robLedState:Array = [false, false, false, false, false];
    private var ledColorState:Array = [false, false, false];


    private var labDigitalOut:Array = [false, false, false, false, false, false, false, false, false, false, false, false, false];
    private var robClawActual:int = 0;
    private var robClawTarget:int = 0;


    private var settingsDefaultMotorSpeed:int;


    private var app:Scratch;

    private var sensorTypes:Array = null;


    private var sBaseURLRobot:String = "http://127.0.0.1:9876/bin/def/";


    private var lastCommandRobotSuccess:Boolean = false;




    public function DesktopRobotCommunicator(app:Scratch, settingsDefaultMotorSpeed:int, onDataReceiveRobot:Function, onDataReceiveLab:Function) {
        trace("Desktop Connector created");
        this.app = app;
        this.settingsDefaultMotorSpeed = settingsDefaultMotorSpeed;
        this._speedLeft  = settingsDefaultMotorSpeed;
        this._speedRight = settingsDefaultMotorSpeed;


        this.onDataReceiveRobot = onDataReceiveRobot;
        this.onDataReceiveLab = onDataReceiveLab;

        loaderRobot.addEventListener(Event.COMPLETE, completeHandlerRobot);
        loaderRobot.addEventListener(IOErrorEvent.IO_ERROR, errorHandlerRobot);
        loaderRobot.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, errorHandlerRobot);
        loaderRobot.addEventListener(IOErrorEvent.NETWORK_ERROR, errorHandlerRobot);
        loaderRobot.addEventListener(ErrorEvent.ERROR, errorHandlerRobot);
        loaderRobot.addEventListener(SecurityErrorEvent.SECURITY_ERROR, errorHandlerRobot);
        loaderRobot.dataFormat = URLLoaderDataFormat.BINARY;



        loaderLab.addEventListener(Event.COMPLETE, completeHandlerLab);
        loaderLab.addEventListener(IOErrorEvent.IO_ERROR, errorHandlerLab);
        loaderLab.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, errorHandlerLab);
        loaderLab.addEventListener(IOErrorEvent.NETWORK_ERROR, errorHandlerLab);
        loaderLab.addEventListener(ErrorEvent.ERROR, errorHandlerLab);
        loaderLab.addEventListener(SecurityErrorEvent.SECURITY_ERROR, errorHandlerLab);
        loaderLab.dataFormat = URLLoaderDataFormat.BINARY;
    }







    public function turnLeft():void {
      trace("ROBOT LEFT");

      _speedLeft  = -Math.abs(_speedLeft);
      _speedRight = Math.abs(_speedRight);

      if(isRobotStopped()){
      }
      else{
         speedLeft  = _speedLeft;
         speedRight = _speedRight;
         manageRobot();
      }
    }

    public function turnRight():void {
      trace("ROBOT RIGHT");

      _speedLeft  = Math.abs(_speedLeft);
      _speedRight = -Math.abs(_speedRight);

      if(isRobotStopped()){
      }
      else{
         speedLeft  = _speedLeft;
         speedRight = _speedRight;
         manageRobot();
      }
    }

    public function goForward():void {
      trace("ROBOT FORWARD");

      _speedLeft  = Math.abs(_speedLeft);
      _speedRight = Math.abs(_speedRight);

      if(isRobotStopped()){
      }
      else{
         speedLeft  = _speedLeft;
         speedRight = _speedRight;
         manageRobot();
      }
    }
    public function goBack():void {
      trace("ROBOT BACRWARD");

      _speedLeft  = -Math.abs(_speedLeft);
      _speedRight = -Math.abs(_speedRight);

      if(isRobotStopped()){
      }
      else{
         speedLeft  = _speedLeft;
         speedRight = _speedRight;
         manageRobot();
      }
   }















    public function motorOn():void {
      trace("ROBOT GO");

      speedLeft  = _speedLeft;
      speedRight = _speedRight;


      isEncoder = false;
      isMotionTerminated = false;
      manageRobot();
    }
    public function motorOff():void {
      trace("ROBOT STOP");

//      _speedLeft  = speedLeft;
//      _speedRight = speedRight;

      speedLeft  = 0;
      speedRight = 0;

      isEncoder = false;
      manageRobot();
    }







    public function setMotorSpeedNoDirection(left:int, right:int):void {
      trace("ROBOT SPEED " + left + " " + right);

      if(_speedLeft >= 0){
         _speedLeft = left;
      }
      else{
         _speedLeft = -left;
      }

      if(_speedRight >= 0){
         _speedRight = right;
      }
      else{
         _speedRight = -right;
      }


      if(isRobotStopped()){
      }
      else{
         speedLeft  = _speedLeft;
         speedRight = _speedRight;
         manageRobot();
      }
    }




    public function setMotorSpeedAndDirection(left:int, right:int):void {
      trace("ROBOT SPEED " + left + " " + right + " TERMINATED=" + isMotionTerminated);

      _speedLeft = left;
      _speedRight = right;

      if(isRobotStopped()){
      }
      else{
         speedLeft = left;
         speedRight = right;
         manageRobot();
      }
    }







    public function setMotionLimit(limit:int):void{
      trace("MOTION LIMIT=" + limit);

      if(isRobotStopped()){
         speedLeft  = _speedLeft;
         speedRight = _speedRight;

         if(speedLeft == 0 && speedRight == 0){
            speedLeft  = settingsDefaultMotorSpeed;
            speedRight = settingsDefaultMotorSpeed;
         }
      }

      setMotorSpeedAndLimit(speedLeft, speedRight, limit);
    }







    public function robLedOn(led:int):void {
      trace("ROB LED ON=" + led);

      var ledShifted:int = led - 1;

      if(robLedState[ledShifted] == false){
         robLamps = robLamps | (1 << ledShifted);

         setRobotLight(convertInt2Byte(robLamps));
         robLedState[ledShifted] = true;
      }
    }
    public function robLedOff(led:int):void {
      trace("ROB LED OFF=" + led);

      var ledShifted:int = led - 1;

      if(robLedState[ledShifted] == true){
         robLamps = robLamps & ~(1 << ledShifted);

         setRobotLight(convertInt2Byte(robLamps));
         robLedState[ledShifted] = false;
      }
    }






    public function ledOn(led:int):void {
      trace("LED ON=" + led);

      if(ledState[led] == false){
         lamps = lamps | (1 << led);

         addLabCommand("lab_lamps/" + convertInt2Byte(lamps));
         ledState[led] = true;
      }
    }
    public function ledOff(led:int):void {
      trace("LED OFF=" + led);

      if(ledState[led] == true){
         lamps = lamps & ~(1 << led);

         addLabCommand("lab_lamps/" + convertInt2Byte(lamps));
         ledState[led] = false;
      }
    }



    public function ledColorOn(led:String):void {
      trace("LED ON=" + led);

      if(led == 'r' && ledColorState[0] == false){
         lampColor = lampColor | 4;
         addLabCommand("lab_color_lamps/" + convertInt2Byte(lampColor));
         ledColorState[0] = true;
      }
      if(led == 'y' && ledColorState[1] == false){
         lampColor = lampColor | 2;
         addLabCommand("lab_color_lamps/" + convertInt2Byte(lampColor));
         ledColorState[1] = true;
      }
      if(led == 'g' && ledColorState[2] == false){
         lampColor = lampColor | 1;
         addLabCommand("lab_color_lamps/" + convertInt2Byte(lampColor));
         ledColorState[2] = true;
      }
    }
    public function ledColorOff(led:String):void {
      trace("LED OFF=" + led);

      if(led == 'r' && ledColorState[0] == true){
         lampColor = lampColor & ~4;
         addLabCommand("lab_color_lamps/" + convertInt2Byte(lampColor));
         ledColorState[0] = false;
      }
      if(led == 'y' && ledColorState[1] == true){
         lampColor = lampColor & ~2;
         addLabCommand("lab_color_lamps/" + convertInt2Byte(lampColor));
         ledColorState[1] = false;
      }
      if(led == 'g' && ledColorState[2] == true){
         lampColor = lampColor & ~1;
         addLabCommand("lab_color_lamps/" + convertInt2Byte(lampColor));
         ledColorState[2] = false;
      }
    }

    public function playNote(note:int):void{
      trace("PLAY=" + note);

      addLabCommand("lab_sound/" + convertInt2Byte(note));
    }


    private function addLabCommand(command:String):void{
      if(labCommands.length < 5){
         labCommands.reverse();
         labCommands.push(command);
         labCommands.reverse();
      }
    }



   private function convertInt2Byte(value:int):String{
      var hex:String = value.toString(16);
      if(hex.length < 2){
         hex = "0" + hex;
      }

      return hex;
   }










    public function setRobotLight(hex:String):void{
      trace("ROBOT LIGHT " + hex);


         interfaceBusyRobot = true;

         try{
            var url:String = sBaseURLRobot + app.robVersion + "/rob_lamps/" + hex + "?" + iAnticacheRobot;

            var requestRobot:URLRequest = new URLRequest(url);
            requestRobot.method = URLRequestMethod.POST;
            iCounterRequestRobot++;

            trace(getTime() + " [" + iCounterRequestRobot + "] " + url);
            loaderRobot.load(requestRobot);

            isEncoder = true;
         }
         catch (e1:Error){
            iAnticacheRobot++;
            interfaceBusyRobot = false;
            onDataReceiveRobot([0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
         }

    }



   public function setLabDigital(pin:int, value:Boolean):void{
      trace("SET LAB DIGITAL PIN=" + pin + " VALUE=" + value);

      var digitalOutMask:int = 1 << (pin - 2);

      if(value){
         if(labDigitalOut[pin] == false){
            addLabCommand("lab_dig_on/" + convertInt2Byte(digitalOutMask));
            labDigitalOut[pin] = true;
         }
      }
      else{
         if(labDigitalOut[pin] == true){
            addLabCommand("lab_dig_off/" + convertInt2Byte(digitalOutMask));
            labDigitalOut[pin] = false;
         }
      }
   }




   public function setLabDigitalPwm(pin:int, value:int):void{
      trace("SET LAB DIGITAL PWM PIN=" + pin + " VALUE=" + value);

      if(pin == 3){
         addLabCommand("lab_dig_pwm/01/" + convertInt2Byte(value));
      }
      if(pin == 5){
         addLabCommand("lab_dig_pwm/02/" + convertInt2Byte(value));
      }
      if(pin == 6){
         addLabCommand("lab_dig_pwm/04/" + convertInt2Byte(value));
      }
   }






    public function setMotorSpeedAndLimit(left:int, right:int, limit:int):void{
      trace("MOTION LEFT=" + left + " RIGHT=" + right + " LIMIT=" + limit);

         isMotionTerminated = false;


         interfaceBusyRobot = true;

         try{
            var speedLeftNormalized:int = left * 0.63;
            var speedRightNormalized:int = right * 0.63;

            trace("ROBOT SPEED NORMALIZED " + speedLeftNormalized + " " + speedRightNormalized);


            var speedLeftShifted:int;
            var speedRightShifted:int;

            if(speedLeftNormalized >= 0){
               speedLeftShifted = speedLeftNormalized;
            }
            else{
               speedLeftShifted = (0 - speedLeftNormalized) + 64;
            }
            if(speedRightNormalized >= 0){
               speedRightShifted = speedRightNormalized;
            }
            else{
               speedRightShifted = (0 - speedRightNormalized) + 64;
            }

            var hex:String = limit.toString(16);
            if(hex.length < 2){
               hex = "000" + hex;
            }
            else if(hex.length < 3){
               hex = "00" + hex;
            }
            else if(hex.length < 4){
               hex = "0" + hex;
            }


            var url:String = sBaseURLRobot + app.robVersion + "/rob_pow_encoder/" + speedLeftShifted.toString(16) + "/" + speedRightShifted.toString(16) + "/" + hex + "?" + iAnticacheRobot;

            var requestRobot:URLRequest = new URLRequest(url);
            requestRobot.method = URLRequestMethod.POST;
            iCounterRequestRobot++;

            trace(getTime() + " [" + iCounterRequestRobot + "] " + url);
            loaderRobot.load(requestRobot);

            isEncoder = true;
         }
         catch (e1:Error){
            iAnticacheRobot++;
            interfaceBusyRobot = false;
            onDataReceiveRobot([]);
         }
    }





   public function setClawDegrees(degrees:int):void{
      trace("CLAW degrees=" + degrees);
      this.robClawTarget = degrees;
    }




   public function setSensorTypes(sensorTypes:Array):void{
      trace("Sensor Types [" + sensorTypes + "]");
      this.sensorTypes = sensorTypes;

         interfaceBusyRobot = true;

         try{
            var url:String = sBaseURLRobot + app.robVersion + "/rob_sensors";

            for (var i:uint=0; i < sensorTypes.length; i++) {
               url += "/" + sensorTypes[i].toString(16);
            }
            url += "?" + iAnticacheRobot;

            var requestRobot:URLRequest = new URLRequest(url);
            requestRobot.method = URLRequestMethod.POST;
            iCounterRequestRobot++;

            trace(getTime() + " [" + iCounterRequestRobot + "] " + url);
            loaderRobot.load(requestRobot);

            isEncoder = true;
         }
         catch (e1:Error){

            iAnticacheRobot++;
            interfaceBusyRobot = false;
            onDataReceiveRobot([0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
         }
   }






    public function manageRobot2():void{

         interfaceBusyRobot2 = true;

         try{
            var speedLeftNormalized:int = speedLeft * 0.63;
            var speedRightNormalized:int = speedRight * 0.63;

            trace("ROBOT SPEED NORMALIZED " + speedLeftNormalized + " " + speedRightNormalized);


            var speedLeftShifted:int;
            var speedRightShifted:int;

            if(speedLeftNormalized >= 0){
               speedLeftShifted = speedLeftNormalized;
            }
            else{
               speedLeftShifted = (0 - speedLeftNormalized) + 64;
            }
            if(speedRightNormalized >= 0){
               speedRightShifted = speedRightNormalized;
            }
            else{
               speedRightShifted = (0 - speedRightNormalized) + 64;
            }

            var url:String = sBaseURLRobot + app.robVersion + "/power/" + speedLeftShifted.toString(16) + "/" + speedRightShifted.toString(16) + "?" + iAnticacheRobot;

            var requestRobot:URLRequest = new URLRequest(url);
            requestRobot.method = URLRequestMethod.POST;
            iCounterRequestRobot++;

            trace(getTime() + " [" + iCounterRequestRobot + "] " + url);
            loaderRobot.load(requestRobot);
         }
         catch (e1:Error){
            iAnticacheRobot++;
            interfaceBusyRobot2 = false;
            onDataReceiveRobot([]);
         }
    }
    public function manageRobot3():void{

         if(interfaceBusyRobot){
            return;
         }

         interfaceBusyRobot = true;

         try{
            var url:String = sBaseURLRobot + app.robVersion + "/check?" + iAnticacheRobot;

            var requestRobot:URLRequest = new URLRequest(url);
            requestRobot.method = URLRequestMethod.POST;
            iCounterRequestRobot++;

            trace(getTime() + " [" + iCounterRequestRobot + "] " + url);
            loaderRobot.load(requestRobot);
         }
         catch (e1:Error){
            iAnticacheRobot++;
            interfaceBusyRobot = false;
            onDataReceiveRobot([]);
         }
    }



   public function setMotionTerminated():void{
      isMotionTerminated = true;
   }



   private function isRobotStopped():Boolean{
      trace("STOPPED? LEFT=" + speedLeft + " RIGHT=" + speedRight + " TERMINATED=" + isMotionTerminated);

      if((speedLeft == 0 && speedRight == 0) || isMotionTerminated){
         return true;
      }
      else{
         return false;
      }
   }




   public function manageRobot():void {
      trace("Manage robot.");

      if (interfaceBusyRobot) {
         return;
      }


      if(lastCommandRobotSuccess){
        //ok, they are synchronized
         trace("Last robot command successed.");
      }
      else{
         trace("Last robot command failed.");

         if(sensorTypes == null){
            //no types know yet

         }
         else{
            setSensorTypes(this.sensorTypes)
            return;
         }
      }


      interfaceBusyRobot = true;



      lastCommandRobotSuccess = false;

      var url:String;
      var requestRobot:URLRequest;
      if (this.robClawActual != this.robClawTarget){
         try {
            url = sBaseURLRobot + app.robVersion + "/rob_claw/" + this.robClawTarget.toString(16) + "?" + iAnticacheRobot;

            requestRobot = new URLRequest(url);
            requestRobot.method = URLRequestMethod.POST;
            iCounterRequestRobot++;

            trace(getTime() + " [" + iCounterRequestRobot + "] " + url);
            loaderRobot.load(requestRobot);

            this.robClawActual = this.robClawTarget;
         }
         catch (e1:Error) {
            iAnticacheRobot++;
            interfaceBusyRobot = false;
            onDataReceiveRobot([0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
         }
      }
      else {
         try {
            var speedLeftNormalized:int = speedLeft * 0.63;
            var speedRightNormalized:int = speedRight * 0.63;

            trace("ROBOT SPEED NORMALIZED " + speedLeftNormalized + " " + speedRightNormalized);


            var speedLeftShifted:int;
            var speedRightShifted:int;

            if (speedLeftNormalized >= 0) {
               speedLeftShifted = speedLeftNormalized;
            }
            else {
               speedLeftShifted = (0 - speedLeftNormalized) + 64;
            }
            if (speedRightNormalized >= 0) {
               speedRightShifted = speedRightNormalized;
            }
            else {
               speedRightShifted = (0 - speedRightNormalized) + 64;
            }

            if (isEncoder) {
               url = sBaseURLRobot + app.robVersion + "/rob_check?" + iAnticacheRobot;

               requestRobot = new URLRequest(url);
               requestRobot.method = URLRequestMethod.POST;
               iCounterRequestRobot++;

               trace(getTime() + " [" + iCounterRequestRobot + "] " + url);
               loaderRobot.load(requestRobot);
            }
            else {
               url = sBaseURLRobot + app.robVersion + "/rob_power/" + speedLeftShifted.toString(16) + "/" + speedRightShifted.toString(16) + "?" + iAnticacheRobot;

               requestRobot = new URLRequest(url);
               requestRobot.method = URLRequestMethod.POST;
               iCounterRequestRobot++;

               trace(getTime() + " [" + iCounterRequestRobot + "] " + url);
               loaderRobot.load(requestRobot);
            }
         }
         catch (e1:Error) {
            iAnticacheRobot++;
            interfaceBusyRobot = false;
            onDataReceiveRobot([]);
         }
      }
   }




    private var labCommands:Array = new Array();







    public function sendCommandLab():void{
       var baseURL:String = "http://127.0.0.1:9876/bin/def/" + app.labVersion + "/";
       var command:String;

         if(interfaceBusyLab){
            return;
         }

         interfaceBusyLab = true;



         if (labCommands.length > 0){
            command = labCommands.pop();
         }
         else{
            command = "lab_check";
         }



         try{
            var url:String = baseURL + command + "?" + iAnticacheLab;

            var requestLab:URLRequest = new URLRequest(url);
            requestLab.method = URLRequestMethod.POST;
            iCounterRequestLab++;

            trace(getTime() + " [" + iCounterRequestLab + "] " + url);
            loaderLab.load(requestLab);
         }
         catch (e1:Error){
            iAnticacheLab++;
            interfaceBusyLab = false;
            onDataReceiveLab([]);
         }
    }




       public function keepAlive():void{
          trace("Alive BLYA?");

           if(interfaceBusyRobot){
              trace("Robot: BUSY");
              //actually not so important
           }
           else{
              trace("Robot: FREE");
              lastSendRobot = getTimer();
              manageRobot();
           }

           if(interfaceBusyLab){
              trace("Lab: BUSY");
              //actually not so important
           }
           else{
              trace("Lab: FREE");
              lastSendLab = getTimer();
              sendCommandLab();
           }
       }

       public function setActive(isActive:Boolean):void {
      }

       public function finishSession():void {
       }


       public function getName():String {
           return null;
       }


      private function errorHandlerRobot(event:Event):void
      {
         trace(event);
         onDataReceiveRobot([]);

         iAnticacheRobot++;
         interfaceBusyRobot = false;
      }

      private function errorHandlerLab(event:Event):void
      {
         trace(event);
         onDataReceiveLab([]);

         iAnticacheLab++;
         interfaceBusyLab = false;
      }





      private function completeHandlerRobot(event:Event):void
      {
         try{
            var loader2:URLLoader = URLLoader(event.target);
            //var tempArray:Array=loader2.data.split("\n");

            iCounterResponseRobot++;

//            trace(getTime() + " [" + iCounterResponseRobot + "] INCOMING ROBOT:\n" + loader2.data);


            trace(getTime() + " [" + iCounterResponseRobot + "] INCOMING ROBOT:\n" + fromHexArray(loader2.data, true));
            var analogsRobot:Array = new Array();


            if(loader2.data.length == 0){
               app.scratchBoardPart.setConnected(false);
               onDataReceiveRobot([]);

               if(app.robVersion == 0){
                  app.robVersion = 3;
               }
               else if(app.robVersion == 3){
                  app.robVersion = 0;
               }

               trace("ROB VERSION=" + app.robVersion);
            }
            else{
               app.scratchBoardPart.setConnected(true);
               lastCommandRobotSuccess = true;

               for (var i:int = 0; i < loader2.data.length; i++){
                  analogsRobot[i] = loader2.data[i];
               }

               onDataReceiveRobot(analogsRobot);
            }
            //trace(tempArray);
         }
         catch (myError:Error){
            onDataReceiveRobot([]);
         }

         interfaceBusyRobot = false;
      }

      private function completeHandlerLab(event:Event):void
      {
         try{
            var loader2:URLLoader = URLLoader(event.target);
//            var tempArray:Array=loader2.data.split("\n");

            iCounterResponseLab++;
            trace(getTime() + " [" + iCounterResponseLab + "] INCOMING LAB:\n" + fromHexArray(loader2.data, true));

            if(loader2.data.length == 0){
               app.scratchLabPart.setConnected(false);
               onDataReceiveLab([]);

               if(app.labVersion == 1){
                  app.labVersion = 2;
               }
               else if(app.labVersion == 2){
                  app.labVersion = 4;
               }
               else if(app.labVersion == 4){
                  app.labVersion = 1;
               }

               trace("LAB VERSION=" + app.labVersion);
            }
            else{
               app.scratchLabPart.setConnected(true);
               for (var i:int = 0; i < loader2.data.length; i++){
                  analogsLab[i] = loader2.data[i];
               }
               onDataReceiveLab(analogsLab);
            }

            //trace(tempArray);
         }
         catch (myError:Error){
            onDataReceiveLab([]);
         }

         interfaceBusyLab = false;
       }





       private function getTime():String
       {
           const d:String = '.';
           const e:String = '';
           const s:String = ':';

           var date:Date = new Date();
           return e.concat(pad(date.hours, 2), s,
                           pad(date.minutes, 2), s,
                           pad(date.seconds, 2), d,
                           pad(date.milliseconds, 3));

           function pad(value:String, length:int):String
           {
               const zero:String = '0';
               var result:String = value;
               while (result.length < length)
               {
                   result = zero.concat(result);
               }
               return result;
           }
       }


      public static function fromHexArray(array:ByteArray, colons:Boolean=false):String {
         var s:String = "";

         for (var i:uint=0; i<array.length; i++) {
            s += ("0"+array[i].toString(16)).substr(-2,2);
            if (colons) {
               if (i<array.length-1) s+=":";
            }
         }
         return s;
      }
   }
}






/*
    private function againLab():Function {
       return function(e:TimerEvent):void {
          sendCommandLab("read_sensors");
       };
    }
*/



/*
         if(interfaceBusyLab){
            labCommands.push(URL);

            var myTimer:Timer = new Timer(30, 1);
            myTimer.addEventListener(TimerEvent.TIMER, againLab());
            myTimer.start();
            return;
         }
*/






