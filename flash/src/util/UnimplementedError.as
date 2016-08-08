/**
 * Created by Днс on 01.06.2015.
 */
package util {
public class UnimplementedError extends Error {
    public function UnimplementedError(message:String, errorID:int=0) {
        super(message, errorID);
    }
}
}
