/**
 * Created by Nikita Yaschenko on 01.06.2015.
 */
package connectors {

    import com.as3breeze.air.ane.android.Bluetooth;
    import com.as3breeze.air.ane.android.events.BluetoothEvent;
    import com.as3breeze.air.ane.android.events.BluetoothScanEvent;

public class AndroidConnector implements IConnector {
    public const SUCCESSFUL:int = 0;
    public const BLUETOOTH_NOT_SUPPORTED_ERROR:int = 1;

    //State information
    private var bluetooth:Bluetooth;

    //Discovery information
    private var availableDevices:Vector.<IDevice>;
    private var callbackForDiscoveryFinished:Function;
    private var callbackForDiscoveryStarted:Function;

    protected function onBluetoothInit(event:BluetoothEvent):void {
        trace("localDeviceAddress: " + bluetooth.localDeviceAddress);
        trace("localDeviceName: " + bluetooth.localDeviceName);
    }

    public function AndroidConnector() {
        if (Bluetooth.isSupported()) {
            bluetooth = Bluetooth.currentAdapter();
            //bluetooth.addEventListener(BluetoothEvent.BLUETOOTH_ANE_INITIALIZED, btInitiated );
            bluetooth.addEventListener(BluetoothScanEvent.BLUETOOTH_DISCOVERY_STARTED, bluetoothScanEventHandler);
            bluetooth.addEventListener(BluetoothScanEvent.BLUETOOTH_DEVICE_FOUND, bluetoothScanEventHandler);
            bluetooth.addEventListener(BluetoothScanEvent.BLUETOOTH_DISCOVERY_FINISHED, bluetoothScanEventHandler);
            bluetooth.addEventListener(BluetoothEvent.BLUETOOTH_ANE_INITIALIZED, onBluetoothInit);
        } else
            bluetooth = null;

        function bluetoothScanEventHandler(b:BluetoothScanEvent):void {
            switch (b.type) {
                case BluetoothScanEvent.BLUETOOTH_DEVICE_FOUND:
                    var bondstate:String = "";

                    // These are pure android codes
                    switch (b.device.bondState) {
                        case 10:
                            bondstate = "no-bond";
                        case 11:
                            bondstate = "bonding";  // Not likely to happen in BLUETOOTH_DEVICE_FOUND event but giving it as example
                        case 12:
                            bondstate = "bonded";
                    }
                    trace("Device found:", b.device.name, "{", b.device.address, "}", bondstate, "UUID = ", b.device.UUID);
                    var found:Boolean = false;
                    for each (var el:IDevice in availableDevices)
                        if (el.getName() == b.device.name)
                            found = true;
                    if (!found)
                        availableDevices.push(new RemoteDevice(b.device));
                    break;
                case BluetoothScanEvent.BLUETOOTH_DISCOVERY_FINISHED:
                    trace("Scan finished!", "\n", "Connectiong to device in vector ...");
                    if (isTurnOn == 1) {
                        bluetooth.scanForVisibleDevices();
                        break;
                    }
                    ++isTurnOn;
                    callbackForDiscoveryFinished(availableDevices);
                    break;
                case BluetoothScanEvent.BLUETOOTH_DISCOVERY_STARTED:
                    ++isTurnOn;
                    if (isTurnOn == 2)
                        break;
                    if (callbackForDiscoveryStarted)
                        callbackForDiscoveryStarted();
                    trace("Scan started...");
                    break;
            }
        }
    }

    public function scanForVisibleDevices(callbackDiscoveryStarted:Function, callbackForDiscoveryFinished:Function):int {
        this.callbackForDiscoveryStarted = callbackDiscoveryStarted;
        this.callbackForDiscoveryFinished = callbackForDiscoveryFinished;
        if (bluetooth != null) {
            availableDevices = new Vector.<IDevice>();
            if (!bluetooth.isEnabled()) {
                trace("not enabled");
                bluetooth.addEventListener(BluetoothEvent.BLUETOOTH_ON, onTurnOnBluetooth);
                bluetooth.enableBT();
            } else
                bluetooth.scanForVisibleDevices();
            return SUCCESSFUL;
        } else
            return BLUETOOTH_NOT_SUPPORTED_ERROR;
    }

    private var isTurnOn:int = 10;//>2
    private function onTurnOnBluetooth(b:BluetoothEvent):void {
        trace("onTurnOn handler");
        isTurnOn = 0;
        bluetooth.scanForVisibleDevices();
        bluetooth.removeEventListener(BluetoothEvent.BLUETOOTH_ON, onTurnOnBluetooth);
        //bluetooth.addEventListener(BluetoothScanEvent.BLUETOOTH_VISIBLE, onVisibleBluetooth);
    }
}
}
