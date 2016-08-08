/**
 * Created by User on 07.06.2015.
 */
package connectors {
import com.as3breeze.air.ane.android.events.BluetoothDataEvent;

import flash.utils.getTimer;

public class AndroidRobotCommunicator implements IRobotCommunicator {
    private const INTERVAL_SEND:int = 100;//in ms
    private const MOTOR1_REVERSE_BIT:int = 5;
    private const MOTOR2_REVERSE_BIT:int = 6;
    private const MOTOR_ON_BIT:int = 7;
    private const DEFAULT_POWER:int = (1<<0) + (1<<2) + (1<<4);//in firmware is multiplied by 4 and is set to 100 actually//CHANGE
    public var maskSend:int = (0<<7) | (3<<5) | DEFAULT_POWER;//motorOn | isReverse1 isReverse2 | motorPower(in firmware this value must be equal 100=25*4)
    public var lastSend:int = 0;
    public var isActive:Boolean = false;
    private var analogs:Array = [0, 0, 0, 0, 0, 0];
    private var previousData:String = "";
    private var device:IDevice;
    private var onDataReceive:Function;

    public function AndroidRobotCommunicator(dev:IDevice, onDataReceive:Function) {
        this.device = dev;
        this.onDataReceive = onDataReceive;
        addDeviceListeners();
        device.sendByte(maskSend);
    }

    public function resetMask():void {
        maskSend = (0<<7) | (3<<5) | DEFAULT_POWER;
    }

    public function sendToRobot():Boolean {
        if (device != null && device.isConnected() && isActive)  {
            lastSend = getTimer();
            return device.sendByte(maskSend);
        }
        return false;
    }


    public function connected():Boolean {
        return device != null && device.isConnected();
    }

    public function turnLeft():void {
        if (!connected())
            return;
        maskSend = buildMask(isMotorOn(), true, false);
        if (isMotorOn())
            sendToRobot();
    }

    public function turnRight():void {
        if (!connected())
            return;
        maskSend = buildMask(isMotorOn(), false, true);
        if (isMotorOn())
            sendToRobot();
    }

    public function goForward():void {
        if (!connected())
            return;
        maskSend = buildMask(isMotorOn(), true, true);
        if (isMotorOn())
            sendToRobot();
    }

    public function goBack():void {
        if (!connected())
            return;
        maskSend = buildMask(isMotorOn(), false, false);
        if (isMotorOn())
            sendToRobot();
    }

    public function motorOn():void {
        if (!isMotorOn() && connected()) {
            maskSend ^= 1<<MOTOR_ON_BIT;
            sendToRobot();
        }
    }

    public function motorOff():void {
        if (isMotorOn() && connected()) {
            maskSend ^= 1<<MOTOR_ON_BIT;
            sendToRobot();
        }
    }

    private function isMotorOn():Boolean {
        return ((maskSend>>MOTOR_ON_BIT)&1) == 1;
    }

    private function buildMask(isOn:Boolean, isRev1:Boolean, isRev2:Boolean):int {
        var bitOn:int = (isOn ? 1 : 0);
        var bitRev1:int = (isRev1 ? 1 : 0);
        var bitRev2:int = (isRev2 ? 1 : 0);
        return (bitOn<<MOTOR_ON_BIT) | (bitRev1<<MOTOR1_REVERSE_BIT) | (bitRev2<<MOTOR2_REVERSE_BIT) | DEFAULT_POWER;
    }

    private function toBit(a:int):String {
        var ret:String = "";
        for (var i:int=7; i >= 0; --i)
            if ((a>>i)&1) ret += "1";
            else ret += "0";
        return ret;
    }

    private static function fromBit(s:String):int {
        var ret:int = 0;
        for (var i:uint = 0; i < s.length; ++i)
            if (s.charAt(i) == '1')
                ret += 1<<(s.length - i - 1);
        return ret;
    }


    private function addDeviceListeners():void {
        if (device == null)
            return;
        device.addReceiveDataListener(function (bev:BluetoothDataEvent):void {
            var ba:String = bev.message;
            var len:int = ba.length;
            var cur:String = bev.message;

            if (previousData.length <= cur.length && cur.substr(0, previousData.length) == previousData) {
                previousData = cur;
            } else if (previousData.charAt() == '1') {
                previousData = previousData + cur;
            } else {
                previousData = previousData.substr(8, previousData.length - 8) + cur;
            }

            if (8 < previousData.length && (previousData.charAt() == '1' && previousData.charAt(8) == '1')) {
                previousData = previousData.substr(8, previousData.length - 8);
            }

            //trace("len:", len, "data:", previousData);
            if (len == 0)
                return;
            var i:int = 0;
            for (; i + 15 < previousData.length; i += 16) {
                var channel:int = fromBit(previousData.substr(i + 1, 4));
                if (channel >= 7 || channel == 5 || channel < 0)
                    continue;
                if (channel == 6) channel = 5;
                var value:int = fromBit(previousData.substr(i + 5, 3).concat(previousData.substr(i + 9, 7)));
                analogs[channel] = value;
                //setAnalogText(channel, '' + value);
            }
            previousData = previousData.substr(i, previousData.length - i);
            onDataReceive(analogs);
        });
    }

    public function removeDeviceListeners():void {
        if (device != null)
            device.removeAllListeners();
    }


    public function keepAlive():void {
        if (getTimer() - lastSend > INTERVAL_SEND) //if programm doesn't send data to a robot on this iteration yet
            sendToRobot();
    }


    public function setActive(isActive:Boolean):void {
        this.isActive = isActive;
    }

    public function finishSession():void {
        resetMask();
        if (device != null) {
            device.removeAllListeners();
            device.disconnect();
            device = null;
        }
    }

    public function getName():String {
        if (device == null)
            return "";
        return device.getName();
    }
}
}
