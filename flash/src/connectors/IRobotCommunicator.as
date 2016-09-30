/**
 * Created by Ilya Peresadin on 07.06.2015.
 */
package connectors {
public interface IRobotCommunicator {
    function turnLeft():void;
    function turnRight():void;
    function goForward():void;
    function goBack():void;
    function motorOn():void;
    function motorOff():void;

    function setMotorSpeedNoDirection(left:int, right:int):void;
    function setMotorSpeedAndDirection(left:int, right:int):void;
    function setMotionLimit(limit:int):void;


    function setMotorSpeedAndLimit(left:int, right:int, limit:int):void;

    function setMotionTerminated():void;



    function robLedOn(led:int):void;
    function robLedOff(led:int):void;


    function setLabDigital(pin:int, value:Boolean):void;
    function setLabDigitalPwm(pin:int, value:int):void;



    function ledOn(led:int):void;
    function ledOff(led:int):void;
    function ledColorOn(led:String):void;
    function ledColorOff(led:String):void;
    function playNote(note:int):void;

    function keepAlive():void;

//    function getName():String;
//    function setActive(isActive:Boolean):void;
//    function finishSession():void;
}
}
