package ui.parts{
        import flash.display.Shape;
        import flash.display.Sprite;
        import flash.events.MouseEvent;
        import assets.Resources;
        import flash.display.*;
        import uiwidgets.DialogBox;
        import uiwidgets.BlockColorEditor;


        public class SensorSelector extends Sprite {
           private var icon:Bitmap;
           private var icons:Array = [Resources.createBmp('sensor16_None'),
                                      Resources.createBmp('lab_sensor16_Temperature'),
                                      Resources.createBmp('lab_sensor16_Clamps')
                                     ];
           private var iconsHover:Array = [Resources.createBmp('sensor16_None_Hover'),
                                           Resources.createBmp('lab_sensor16_Temperature_Hover'),
                                           Resources.createBmp('lab_sensor16_Clamps_Hover')
                                          ];
           public var sensor:int;


           public static var dialogBox:DialogBox;

           private var functionOver:Function;
           private var functionOut:Function;
           private var app:Scratch;
           private var number:int;

           public function SensorSelector(app:Scratch, number:int, sensor:int) {
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


              this.app.labSensors[number - 1].type = sensor;


              //var tmpSensors:Array = new Array();
              //for (var i:int = 0; i < Scratch.ROBOT_SENSOR_COUNT; i++) {
              //   tmpSensors.push(this.app.robotSensors[i].type);
              //}
              //this.app.robotCommunicator.setSensorTypes(tmpSensors);
           }



           private function turn(e:MouseEvent):void {
              if(dialogBox == null){
                 dialogBox = new DialogBox();
                 dialogBox.addTitle('Sensor type');
                 dialogBox.addWidget(new SensorSelectorPanel(this));
                 //dialogBox.addButton('Close', dialogBox.cancel);
                 dialogBox.showOnStage(stage, true);
              }
           }
        }
}