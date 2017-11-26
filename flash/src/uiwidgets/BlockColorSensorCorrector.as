package uiwidgets {
import blocks.*;

import flash.display.*;
import flash.events.*;
import flash.geom.*;
import flash.net.*;
import flash.text.*;
import flash.utils.ByteArray;

import ui.parts.UIPart;

import util.*;

public class BlockColorSensorCorrector extends Sprite {

        private var base:Shape;
        private var blockShape:BlockShape;
        private var blockLabel:TextField;

        // HSV controls
        private var rBox:EditableLabel;
        private var gBox:EditableLabel;
        private var bBox:EditableLabel;

        private var rSlider:Scrollbar;
        private var gSlider:Scrollbar;
        private var bSlider:Scrollbar;

        // current color
        private var r:Number;
        private var g:Number;
        private var b:Number;

        private var application:Scratch;

        public function BlockColorSensorCorrector(application:Scratch){
                this.application = application;
                addChild(base = new Shape());
                setWidthHeight(250, 360);

                addHSVControls();
        }

        private function setWidthHeight(w:int, h:int):void {
                var g:Graphics = base.graphics;
                g.clear();
                g.beginFill(CSS.white);
                g.drawRect(0, 0, w, h);
                g.endFill();
        }

        public function apply(b:IconButton):void {
        }


        private function addHSVControls():void {
                makeLabel('R', 15, 35,  0, true);
                makeLabel('G', 15, 110, 0, true);
                makeLabel('B', 15, 185, 0, true);

                addChild(rBox = new EditableLabel(rTextChanged));
                addChild(gBox = new EditableLabel(gTextChanged));
                addChild(bBox = new EditableLabel(bTextChanged));

                addChild(rSlider = new Scrollbar(10, 300, setr));
                addChild(gSlider = new Scrollbar(10, 300, setg));
                addChild(bSlider = new Scrollbar(10, 300, setb));

                rBox.setWidth(50);
                gBox.setWidth(50);
                bBox.setWidth(50);

                rBox.x = 25;
                gBox.x = 100;
                bBox.x = 175;
                rBox.y = gBox.y = bBox.y = 25;

                rSlider.x = rBox.x + 20;
                gSlider.x = gBox.x + 20;
                bSlider.x = bBox.x + 20;
                rSlider.y = gSlider.y = bSlider.y = rBox.y + 30;

                r = 0.8;
                g = 0.9;
                b = 1;

                update();
        }


        private function update():void {
                r = Math.max(0, Math.min(r, 1));
                g = Math.max(0, Math.min(g, 1));
                b = Math.max(0, Math.min(b, 1));

                rBox.setContents('' + Math.round(100 * r));
                gBox.setContents('' + Math.round(100 * g));
                bBox.setContents('' + Math.round(100 * b));

                rSlider.update(r, 0.08);
                gSlider.update(g, 0.08);
                bSlider.update(b, 0.08);
        }

        private function rTextChanged():void {
                var n:Number = Number(rBox.contents());
                if (n == n) r = n / 100;
                update();
        }

        private function gTextChanged():void {
                var n:Number = Number(gBox.contents());
                if (n == n) g = n / 100;
                update();
        }

        private function bTextChanged():void {
                var n:Number = Number(bBox.contents());
                if (n == n) b = n / 100;
                update();
        }

        private function setr(n:Number):void { r = n; update() }
        private function setg(n:Number):void { g = n; update() }
        private function setb(n:Number):void { b = n; update() }

        private function makeLabel(s:String, fontSize:int, x:int = 0, y:int = 0, bold:Boolean = false):TextField {
                var tf:TextField = new TextField();
                tf.selectable = false;
                tf.defaultTextFormat = new TextFormat(CSS.font, fontSize, CSS.textColor, bold);
                tf.autoSize = TextFieldAutoSize.LEFT;
                tf.text = s;
                tf.x = x;
                tf.y = y;
                addChild(tf);
                return tf;
        }

}}
