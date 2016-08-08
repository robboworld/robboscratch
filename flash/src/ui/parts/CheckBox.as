package ui.parts{
        import flash.display.Shape;
        import flash.display.Sprite;
        import flash.events.MouseEvent;

        public class CheckBox extends Sprite {
                public var state:Boolean = true;
                private var _mark:Shape;

                public function CheckBox() {
                        this.graphics.beginFill(0xcccccc, 1);
                        this.graphics.drawRect(0, 0, 11, 11);
                        this.graphics.endFill();
                        this.graphics.beginFill(0xdddddd, 1);
                        this.graphics.drawRect(1, 1, 10, 10);
                        this.graphics.endFill();

                        turnOn();
                        turnOff();

                        this.buttonMode = true;
                        this.mouseChildren  = false;

                        this.addEventListener(MouseEvent.CLICK, turn);
                }

                private function turn(e:MouseEvent):void {
                        if (state) {
                                turnOff();
                        }else {
                                turnOn();
                        }
                }

                public function turnOn():void {
                        _mark = new Shape();
                        _mark.graphics.lineStyle(1, 0x000000, 1)
                        _mark.graphics.moveTo(3, 3);
                        _mark.graphics.lineTo(5, 8);
                        _mark.graphics.lineTo(8, 3);
                        addChild(_mark);
                        state = true;
                }
                public function turnOff():void {
                        removeChild(_mark);
                        _mark = null;

                        state = false;
                }
        }

}