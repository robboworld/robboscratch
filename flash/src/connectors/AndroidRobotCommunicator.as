/**
 * Created by User on 07.06.2015.
 */
package connectors {

import flash.utils.getTimer;
import flash.net.*;


public class AndroidRobotCommunicator implements IRobotCommunicator {

    private var onDataReceiveRobot:Function;
    private var onDataReceiveLab:Function;



    public  var lastSendRobot:int = 0;


    private var speedLeft:int   = 0;
    private var speedRight:int  = 0;
    private var _speedLeft:int;
    private var _speedRight:int;
    private var isMotionTerminated:Boolean = false;

    private var isEncoder:Boolean = false;


   private var settingsDefaultMotorSpeed:int;



    public function AndroidRobotCommunicator(settingsDefaultMotorSpeed:int, onDataReceiveRobot:Function, onDataReceiveLab:Function) {
        trace("Android Connector created");
        this.settingsDefaultMotorSpeed = settingsDefaultMotorSpeed;
        this._speedLeft  = settingsDefaultMotorSpeed;
        this._speedRight = settingsDefaultMotorSpeed;

        this.onDataReceiveRobot = onDataReceiveRobot;
        this.onDataReceiveLab = onDataReceiveLab;
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
    public function setMotorSpeedAndLimit(left:int, right:int, limit:int):void{
        trace("MOTION LEFT=" + left + " RIGHT=" + right + " LIMIT=" + limit);

        isMotionTerminated = false;

        onDataReceiveRobot(RobotANE.powerAndLimit(left, right, limit));
        isEncoder = true;
    }
    public function setMotionTerminated():void{
        isMotionTerminated = true;
    }





    public function robLedOn(led:int):void {
      trace("ROB LED ON=" + led);
    }
    public function robLedOff(led:int):void {
      trace("ROB LED OFF=" + led);
    }





    public function manageRobot():void{
        trace("ROBOT SPEED NORMALIZED " + speedLeft + " " + speedRight);

        if(isEncoder){
            onDataReceiveRobot(RobotANE.check());
        }
        else{
            onDataReceiveRobot(RobotANE.power(speedLeft, speedRight));
        }
    }











    public function ledOn(led:int):void {
      trace("LED ON=" + led);
    }
    public function ledOff(led:int):void {
      trace("LED OFF=" + led);
    }

    public function ledColorOn(led:String):void {
      trace("LED ON=" + led);
    }
    public function ledColorOff(led:String):void {
      trace("LED OFF=" + led);
    }

    public function playNote(note:int):void{
      trace("PLAY=" + note);
    }



   public function setLabDigital(pin:int, value:Boolean):void{
      trace("SET LAB DIGITAL PIN=" + pin + " VALUE=" + value);
   }




   public function setLabDigitalPwm(pin:int, value:int):void{
      trace("SET LAB DIGITAL PWM PIN=" + pin + " VALUE=" + value);
   }








    public function keepAlive():void{
        trace("Alive?");

        lastSendRobot = getTimer();
        manageRobot();
    }
}
}