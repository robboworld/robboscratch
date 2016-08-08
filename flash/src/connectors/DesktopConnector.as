/**
 * Created by Nikita Yaschenko on 01.06.2015.
 */
package connectors {
import util.UnimplementedError;

public class DesktopConnector implements IConnector {

    public function DesktopConnector() {
        throw new UnimplementedError("Unimplemented");
    }

    public function scanForVisibleDevices(callbackForDiscoveryStarted:Function, callbackForDiscoveryFinished:Function):int {
        throw new UnimplementedError("Unimplemented");
    }
}
}
