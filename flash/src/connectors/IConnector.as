/*
IConnector.as
Ilya Peresadin, November 2014

Interface-wrapper around bluetooth and com-port.
Interface provide common methods for connection with robot.
In the code you should use this interface instead of classes for bluetooth and com-ports previously
wrap corresponding classes in this interface. 
*/

package connectors {
	public interface IConnector {
        /**
         * Function for detecting connected robots.
         * @param callbackForDiscoveryStarted function was called when search of devices start
         * @param callbackForDiscoveryFinished function was called when search was finished. Function accept vector of connected robots.
         */
		function scanForVisibleDevices(callbackForDiscoveryStarted:Function, callbackForDiscoveryFinished:Function):int;
	}
}