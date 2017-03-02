package ui.parts{
        import flash.display.Shape;
        import flash.display.Sprite;
        import flash.events.MouseEvent;
        import assets.Resources;
        import flash.display.*;
        import uiwidgets.DialogBox;
        import uiwidgets.BlockColorEditor;


        public class DeviceSelector extends Sprite {
           private var icon:Bitmap;
           private var icons:Array = [Resources.createBmp('sensor16_None'),
                                      Resources.createBmp('sensor16_Line'),
                                      Resources.createBmp('sensor16_Led'),
                                      Resources.createBmp('sensor16_Light'),
                                      Resources.createBmp('sensor16_Touch'),
                                      Resources.createBmp('sensor16_Proximity'),
                                      Resources.createBmp('sensor16_Ultrasonic'),
                                      Resources.createBmp('sensor16_RGB'),
                                     ];
           private var iconsHover:Array = [Resources.createBmp('sensor16_None_Hover'),
                                           Resources.createBmp('sensor16_Line_Hover'),
                                           Resources.createBmp('sensor16_Led_Hover'),
                                           Resources.createBmp('sensor16_Light_Hover'),
                                           Resources.createBmp('sensor16_Touch_Hover'),
                                           Resources.createBmp('sensor16_Proximity_Hover'),
                                           Resources.createBmp('sensor16_Ultrasonic_Hover'),
                                           Resources.createBmp('sensor16_RGB_Hover'),
                                          ];
           public var sensor:int;


           public static var dialogBox:DialogBox;

           private var functionOver:Function;
           private var functionOut:Function;
           private var app:Scratch;
           private var number:int;

           public function DeviceSelector(app:Scratch, number:int, sensor:int) {
              this.app = app;
              this.number = number;
              this.sensor = sensor;
              this.addChild(icons[sensor]);

              this.buttonMode = true;
              this.mouseChildren  = false;

              this.functionOver = makeOver(sensor, this);
              this.functionOut  = makeOut(sensor, this);

              this.addEventListener(MouseEvent.MOUSE_OVER, this.functionOver);
              this.addEventListener(MouseEvent.MOUSE_OUT, this.functionOut);
              this.addEventListener(MouseEvent.CLICK, turn);
           }



           private function makeOver(f: int, sprite:Sprite):Function{
              return function(evt:MouseEvent):void {
                 while (sprite.numChildren > 0) sprite.removeChildAt(0);
                 sprite.addChild(iconsHover[f]);
              }
           }
           private function makeOut(f: int, sprite:Sprite):Function{
              return function(evt:MouseEvent):void {
                 while (sprite.numChildren > 0) sprite.removeChildAt(0);
                 sprite.addChild(icons[f]);
              }
           }


           public function select(sensor: int):void{
              while (this.numChildren > 0) this.removeChildAt(0);
              this.sensor = sensor;
              this.addChild(icons[sensor]);
              this.removeEventListener(MouseEvent.MOUSE_OVER, functionOver);
              this.removeEventListener(MouseEvent.MOUSE_OUT, functionOut);

              this.functionOver = makeOver(sensor, this);
              this.functionOut  = makeOut(sensor, this);
              this.addEventListener(MouseEvent.MOUSE_OVER, this.functionOver);
              this.addEventListener(MouseEvent.MOUSE_OUT, this.functionOut);


              if(sensor < 6){
                 this.app.robotSensors[number - 1] = 0;
              }
              else{
                 this.app.robotSensors[number - 1] = sensor;
              }


              this.app.robotCommunicator.setSensorType(sensor);
           }



           private function turn(e:MouseEvent):void {
              if(dialogBox == null){
                 dialogBox = new DialogBox();
                 dialogBox.addTitle('Sensor type');
                 dialogBox.addWidget(new DeviceSelectorPanel(this));
                 //dialogBox.addButton('Close', dialogBox.cancel);
                 dialogBox.showOnStage(stage, true);
              }
           }
        }
}