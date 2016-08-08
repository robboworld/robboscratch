/*
IDevice.as
Ilya Peresadin, November 2014

Interface-wrapper around bluetooth-device and com-port.
*/

package connectors {
	public interface IDevice {
        /**
         * Callback was called when device was connected. Called after invoke 'connect'.
         * @param callbackOnDeviceConnected callback
         */
		function addDeviceConnectedListener(callbackOnDeviceConnected:Function):void

        /**
         * Callback was called when device was disconnected.
         * @param callbackOnDeviceConnected callback
         */
		function addDeviceDisconnectedListener(callbackOnDeviceDisconnected:Function):void;

        /**
         * Callback which was called when error was occurred.
         * @param callbackOnDeviceConnected callback
         */
		function addDeviceConnectErrorListener(callbackOnDeviceConnectError:Function):void;

        /**
         * Callback which was called when bytes was received from robot.
         * @param callbackOnDeviceConnected callback
         */
		function addReceiveDataListener(callbackOnReceiveData:Function):void;

        /**
         * Add callback, which was called when device was disconnected.
         * @param callbackOnDeviceConnected callback
         */
		function removeAllListeners():void;

        /**
         * Connect with robot.
         */
		function connect():void;

        /**
         * Disconnect with root.
         */
		function disconnect():void;

        /**
         *
         * @param a byte, which must be sent
         * @return true if byte successful sent, false otherwise
         */
		function sendByte(a:int):Boolean;

        /**
         * Check if set up connection with robot now.
         * @return true if set up connection with robot now, false otherwise
         */
		function isConnected():Boolean;

        /**
         * Function must return name of robot. Name must start with "Scratchduino".
         * @return name of robot
         */
		function getName():String;
	}
}