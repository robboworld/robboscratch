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
    public  var lastSendLab:int = 0;


    private var speedLeft:int   = 0;
    private var speedRight:int  = 0;
    private var _speedLeft:int  = 20;
    private var _speedRight:int = 20;
    private var isMotionTerminated:Boolean = false;

    private var isEncoder:Boolean = false;


    private var ledState:Array = [false, false, false, false, false, false, false, false];
    private var ledStateColor:Array = [false, false, false];




    public function AndroidRobotCommunicator(settingsDefaultMotorSpeed:int, onDataReceiveRobot:Function, onDataReceiveLab:Function) {
        trace("Android Connector created");
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
                speedLeft  = 20;
                speedRight = 20;
            }
        }

        setMotorSpeedAndLimit(speedLeft, speedRight, limit);
    }
    public function setMotorSpeedAndLimit(left:int, right:int, limit:int):void{
        trace("MOTION LEFT=" + left + " RIGHT=" + right + " LIMIT=" + limit);

        isMotionTerminated = false;

        onDataReceiveRobot(RobotANE.robotPowerAndLimit(left, right, limit));
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
            onDataReceiveRobot(RobotANE.robotCheck());
        }
        else{
            onDataReceiveRobot(RobotANE.robotPower(speedLeft, speedRight));
        }
    }








    public function setClawDegrees(degrees:int):void{
        trace("CLAW degrees=" + degrees);
        onDataReceiveRobot(RobotANE.setClawDegrees(degrees));
    }




    public function setSensorTypes(sensorTypes:Array):void{
        trace("Sensor Types [" + sensorTypes + "]");

        onDataReceiveRobot(RobotANE.robotSensorTypes(sensorTypes[0], sensorTypes[1], sensorTypes[2], sensorTypes[3], sensorTypes[4]));
    }












    public function ledOn(led:int):void {
        trace("LED ON=" + led);

        if(ledState[led] == false) {
            ledState[led] = true;
            onDataReceiveLab(RobotANE.labLamp(ledState[0], ledState[1], ledState[2], ledState[3], ledState[4], ledState[5], ledState[6], ledState[7]));
        }
    }
    public function ledOff(led:int):void {
        trace("LED OFF=" + led);

        if(ledState[led] == true) {
            ledState[led] = false;
            onDataReceiveLab(RobotANE.labLamp(ledState[0], ledState[1], ledState[2], ledState[3], ledState[4], ledState[5], ledState[6], ledState[7]));
        }
    }

    public function ledColorOn(led:String):void {
        trace("LED ON=" + led);

        if(led == "r" && ledStateColor[0] == false) {
            ledStateColor[0] = true;
            onDataReceiveLab(RobotANE.labLampColor(ledStateColor[0], ledStateColor[1], ledStateColor[2]));
        }
        if(led == "y" && ledStateColor[1] == false) {
            ledStateColor[1] = true;
            onDataReceiveLab(RobotANE.labLampColor(ledStateColor[0], ledStateColor[1], ledStateColor[2]));
        }
        if(led == "g" && ledStateColor[2] == false) {
            ledStateColor[2] = true;
            onDataReceiveLab(RobotANE.labLampColor(ledStateColor[0], ledStateColor[1], ledStateColor[2]));
        }
    }
    public function ledColorOff(led:String):void {
        if(led == "r" && ledStateColor[0] == true) {
            ledStateColor[0] = false;
            onDataReceiveLab(RobotANE.labLampColor(ledStateColor[0], ledStateColor[1], ledStateColor[2]));
        }
        if(led == "y" && ledStateColor[1] == true) {
            ledStateColor[1] = false;
            onDataReceiveLab(RobotANE.labLampColor(ledStateColor[0], ledStateColor[1], ledStateColor[2]));
        }
        if(led == "g" && ledStateColor[2] == true) {
            ledStateColor[2] = false;
            onDataReceiveLab(RobotANE.labLampColor(ledStateColor[0], ledStateColor[1], ledStateColor[2]));
        }
    }

    public function playNote(note:int):void{
        trace("PLAY=" + note);

        onDataReceiveLab(RobotANE.labSound(note));
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

        //Let's send it once per 100ms
        if(getTimer() - lastSendLab > 100) {
            onDataReceiveLab(RobotANE.labCheck());
//            onDataReceiveLab(RobotANE.labLamp(true, true, true, true, true, true, true, true));

            lastSendLab = getTimer();
        }
    }
}
}