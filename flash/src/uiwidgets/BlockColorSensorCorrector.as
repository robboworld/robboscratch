package uiwidgets {
import blocks.*;

import flash.display.*;
import flash.events.*;
import flash.geom.*;
import flash.net.*;
import flash.text.*;
import flash.utils.ByteArray;

import ui.parts.UIPart;
import ui.parts.CheckBox;

import util.*;

public class BlockColorSensorCorrector extends Sprite {

        private var base:Shape;
        private var blockShape:BlockShape;
        private var blockLabel:TextField;

        // Bright controls
        private var rBoxBright:EditableLabel;
        private var gBoxBright:EditableLabel;
        private var bBoxBright:EditableLabel;


        // Color controls
        private var rBoxColor:EditableLabel;
        private var gBoxColor:EditableLabel;
        private var bBoxColor:EditableLabel;


        private var rSliderBright:Scrollbar;
        private var gSliderBright:Scrollbar;
        private var bSliderBright:Scrollbar;

        private var rSliderColor:Scrollbar;
        private var gSliderColor:Scrollbar;
        private var bSliderColor:Scrollbar;


        // current bright correction
        private var r:Number;
        private var g:Number;
        private var b:Number;


        // current color correction
        private var rColor:Number;
        private var gColor:Number;
        private var bColor:Number;


        private var cbLockR:CheckBox = new CheckBox();
        private var cbLockG:CheckBox = new CheckBox();
        private var cbLockB:CheckBox = new CheckBox();


        private var arrColorListFields:Array = [];

        private var iSlot:int;


        private var application:Scratch;

        public  var spriteRGB:Sprite = new Sprite();
        private var tfMaxBright:TextField = new TextField();

        public function BlockColorSensorCorrector(application:Scratch, iSlot:int){
                this.application = application;
                this.iSlot = iSlot;

                addChild(base = new Shape());
                setWidthHeight(1000, 400);




                addBrightCorrection();
                addColorCorrection();


                makeLabel('R',      15, 585, 20, true);
                makeLabel('G',      15, 685, 20, true);
                makeLabel('B',      15, 785, 20, true);
                makeLabel('Bright', 15, 873, 20, true);


                for(var f:int = 0; f < this.application.arrColorNames.length; f++){
                   var tf:TextField = new TextField();
                   tf.x = 500;
                   tf.y = 50 + 40 * f;
                   tf.text = this.application.arrColorNames[f] + ":";
                   tf.defaultTextFormat = new TextFormat(CSS.font, 14, CSS.textColor, true);
                   tf.autoSize = TextFieldAutoSize.LEFT;
                   addChild(tf);


                   arrColorListFields[this.application.arrColorNames[f]] = [];


                   for(var i:int = 0; i < 4; i++){
                      tf = new TextField();
                      tf.x = 570 + i * 100;
                      tf.y = 50 + 40 * f;
                      tf.selectable = true;
                      tf.type = TextFieldType.INPUT;
                      var sIntervalBegin:String = this.application.arrColorRegion[this.application.arrColorNames[f]][i*2];
                      var sIntervalEnd:String   = this.application.arrColorRegion[this.application.arrColorNames[f]][i*2+1];

                      if(i == 3){
                         //Bright has got no percentage
                         tf.text = sIntervalBegin + "-" + sIntervalEnd;
                      }
                      else{
                         tf.text = sIntervalBegin + (sIntervalBegin.indexOf(".") >= 0 ? "":".00") + "-" + sIntervalEnd + (sIntervalEnd.indexOf(".") >= 0 ? "":".00");
                      }

                      tf.defaultTextFormat = new TextFormat(CSS.font, 14, CSS.textColor, true);
                      tf.border = true;
                      tf.width = 75;
                      tf.height = 20;
                      arrColorListFields[this.application.arrColorNames[f]][i] = tf;
                      tf.addEventListener(Event.CHANGE, function(e:Event):void{
                         update()
                      });
                      addChild(tf);
                   }
                }

                spriteRGB.x = 250;
                //spriteRGB.y = 300;
                addChild(spriteRGB);


                makeLabel('R+G+B:', 12, 202, 392, true);


                tfMaxBright.x = 250;
                tfMaxBright.y = 392;
                tfMaxBright.width  = 30;
                tfMaxBright.height = 20;
                tfMaxBright.selectable = true;
                tfMaxBright.type = TextFieldType.INPUT;
                tfMaxBright.border = true;
                tfMaxBright.text = this.application.arrColorMaxBright[iSlot];
                addChild(tfMaxBright);



                addChild(spriteRGB);

                update();
        }

        public function showColor(color:int):void{
           spriteRGB.graphics.clear();
           spriteRGB.graphics.beginFill(color);
           spriteRGB.graphics.drawRect(1, 1, 20, 20);
           spriteRGB.graphics.endFill();
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


        private function addBrightCorrection():void {
                makeLabel('R', 15, 35,  0, true);
                makeLabel('G', 15, 110, 0, true);
                makeLabel('B', 15, 185, 0, true);

                addChild(rBoxBright = new EditableLabel(rTextChangedBright));
                addChild(gBoxBright = new EditableLabel(gTextChangedBright));
                addChild(bBoxBright = new EditableLabel(bTextChangedBright));
//Let's make it read-only
                rBoxBright.setEditable(false);
                gBoxBright.setEditable(false);
                bBoxBright.setEditable(false);

                addChild(rSliderBright = new Scrollbar(10, 300, setr));
                addChild(gSliderBright = new Scrollbar(10, 300, setg));
                addChild(bSliderBright = new Scrollbar(10, 300, setb));

                rBoxBright.setWidth(50);
                gBoxBright.setWidth(50);
                bBoxBright.setWidth(50);

                rBoxBright.x = 25;
                gBoxBright.x = 100;
                bBoxBright.x = 175;
                rBoxBright.y = gBoxBright.y = bBoxBright.y = 25;

                rSliderBright.x = rBoxBright.x + 20;
                gSliderBright.x = gBoxBright.x + 20;
                bSliderBright.x = bBoxBright.x + 20;
                rSliderBright.y = gSliderBright.y = bSliderBright.y = rBoxBright.y + 30;

                r = application.arrColorCorrectionBright[iSlot][0];
                g = application.arrColorCorrectionBright[iSlot][1];
                b = application.arrColorCorrectionBright[iSlot][2];
        }
        private function addColorCorrection():void {
                makeLabel('R', 15, 300, 0, true);
                makeLabel('G', 15, 375, 0, true);
                makeLabel('B', 15, 450, 0, true);

                addChild(rBoxColor = new EditableLabel(rTextChangedColor));
                addChild(gBoxColor = new EditableLabel(gTextChangedColor));
                addChild(bBoxColor = new EditableLabel(bTextChangedColor));

                addChild(rSliderColor = new Scrollbar(10, 300, setColorR));
                addChild(gSliderColor = new Scrollbar(10, 300, setColorG));
                addChild(bSliderColor = new Scrollbar(10, 300, setColorB));

                rBoxColor.setWidth(50);
                gBoxColor.setWidth(50);
                bBoxColor.setWidth(50);

                rBoxColor.x = 282;
                gBoxColor.x = 357;
                bBoxColor.x = 432;
                rBoxColor.y = gBoxColor.y = bBoxColor.y = 25;

//              Let's set them all to RO as the simpliest sync way
                rBoxColor.setEditable(false);
                gBoxColor.setEditable(false);
                bBoxColor.setEditable(false);


                rSliderColor.x = rBoxColor.x + 20;
                gSliderColor.x = gBoxColor.x + 20;
                bSliderColor.x = bBoxColor.x + 20;
                rSliderColor.y = gSliderColor.y = bSliderColor.y = rBoxColor.y + 30;


                cbLockR.x = 301;
                cbLockG.x = 376;
                cbLockB.x = 451;
                cbLockR.y = cbLockG.y = cbLockB.y = 360;
                cbLockR.addListener(lockListenerR);
                cbLockG.addListener(lockListenerG);
                cbLockB.addListener(lockListenerB);
                addChild(cbLockR);
                addChild(cbLockG);
                addChild(cbLockB);


                var btnAutoCorrection2:Button = new Button("Automatic correction", function():void{
                   trace("AUTO CORRECTION 2");

                   var iRedRaw:int   = application.robotSensors[iSlot].raw_[1];
                   var iGreenRaw:int = application.robotSensors[iSlot].raw_[2];
                   var iBlueRaw:int  = application.robotSensors[iSlot].raw_[3];


                   trace("R=" + iRedRaw);
                   trace("G=" + iGreenRaw);
                   trace("B=" + iBlueRaw);


                   var fCorrectedB:Number = 0.5;
                   var fCorrectedR:Number = 0.5 / (iRedRaw / iBlueRaw);
                   var fCorrectedG:Number = 0.5 / (iGreenRaw / iBlueRaw);

                   r = fCorrectedR;
                   g = fCorrectedG;
                   b = fCorrectedB;

                   trace("CALCULATED CORRECTION=" + r + "," + g + "," + b);

                   var iTotalBright:int = iRedRaw + iGreenRaw + iBlueRaw;
                   tfMaxBright.text = iTotalBright;


                   update();


                });
                btnAutoCorrection2.x = 60;
                btnAutoCorrection2.y = 390;
                addChild(btnAutoCorrection2);












                var btnAutoCorrection:Button = new Button("Automatic correction", function():void{
                   trace("AUTO CORRECTION");

                   var iRedRaw:int   = application.robotSensors[iSlot].raw_[1];
                   var iGreenRaw:int = application.robotSensors[iSlot].raw_[2];
                   var iBlueRaw:int  = application.robotSensors[iSlot].raw_[3];


                   trace("R=" + iRedRaw);
                   trace("G=" + iGreenRaw);
                   trace("B=" + iBlueRaw);

                   var iTotalBright:int = iRedRaw + iGreenRaw + iBlueRaw;
                   trace("TOTAL=" + iTotalBright);

                   rColor = 0.666666666666666666 - iRedRaw   / iTotalBright;
                   gColor = 0.666666666666666666 - iGreenRaw / iTotalBright;
                   bColor = 0.666666666666666666 - iBlueRaw  / iTotalBright;


                   trace("CALCULATED CORRECTION=" + rColor + "," + gColor + "," + bColor);

                   if(rColor < gColor && rColor < bColor){
                      rColor += 0.01
                   }
                   if(gColor < rColor && gColor < bColor){
                      gColor += 0.01
                   }
                   if(bColor < rColor && bColor < gColor){
                      bColor += 0.01
                   }

                   trace("CALCULATED CORRECTION STEP 2=" + rColor + "," + gColor + "," + bColor);


                   var fCorrectedR:Number = iRedRaw   * rColor * 3;
                   var fCorrectedG:Number = iGreenRaw * gColor * 3;
                   var fCorrectedB:Number = iBlueRaw  * bColor * 3;


                   if(fCorrectedR > fCorrectedG && fCorrectedR > fCorrectedB){
                      rColor -= 0.02
                   }
                   if(fCorrectedG > fCorrectedR && fCorrectedG > fCorrectedB){
                      gColor -= 0.02
                   }
                   if(fCorrectedB > fCorrectedR && fCorrectedB > fCorrectedG){
                      bColor -= 0.02
                   }

                   trace("CALCULATED CORRECTION STEP 3=" + rColor + "," + gColor + "," + bColor);


                   trace("CORRECTED=" + iRedRaw * rColor * 3 + "," + iGreenRaw * gColor * 3 + "," + iBlueRaw * bColor * 3);


                   tfMaxBright.text = iTotalBright;

                   update();


                });
                btnAutoCorrection.x = 320;
                btnAutoCorrection.y = 390;
                addChild(btnAutoCorrection);





                rColor = application.arrColorCorrectionColor[iSlot][0];
                gColor = application.arrColorCorrectionColor[iSlot][1];
                bColor = application.arrColorCorrectionColor[iSlot][2];

        }


        private function update():void {
                r = Math.max(0, Math.min(r, 1));
                g = Math.max(0, Math.min(g, 1));
                b = Math.max(0, Math.min(b, 1));

                rBoxBright.setContents('' + Math.round(200 * r) + '%');
                gBoxBright.setContents('' + Math.round(200 * g) + '%');
                bBoxBright.setContents('' + Math.round(200 * b) + '%');

                rBoxColor.setContents('' + Math.round(300 * rColor) + '%');
                gBoxColor.setContents('' + Math.round(300 * gColor) + '%');
                bBoxColor.setContents('' + Math.round(300 * bColor) + '%');


                rSliderBright.update(r, 0.08);
                gSliderBright.update(g, 0.08);
                bSliderBright.update(b, 0.08);


                rSliderColor.update(rColor, 0.08);
                gSliderColor.update(gColor, 0.08);
                bSliderColor.update(bColor, 0.08);


                application.setColorSensorCorrection(iSlot, r, g, b, rColor, gColor, bColor, int(tfMaxBright.text));



                for(var f:int = 0; f < this.application.arrColorNames.length; f++){
                   for(var i:int = 0; i < 4; i++){
                      var arrstrValue:Array = arrColorListFields[this.application.arrColorNames[f]][i].text.split("-");
                      application.arrColorRegion[application.arrColorNames[f]][i*2]   = int((Number(arrstrValue[0]))*100)/100;
                      application.arrColorRegion[application.arrColorNames[f]][i*2+1] = int((Number(arrstrValue[1]))*100)/100;
                   }
                }

        }

        private function rTextChangedBright():void{
                var n:Number = Number(rBoxBright.contents());
                if (n == n) r = n / 200;
                update();
        }
        private function gTextChangedBright():void{
                var n:Number = Number(gBoxBright.contents());
                if (n == n) g = n / 200;
                update();
        }
        private function bTextChangedBright():void{
                var n:Number = Number(bBoxBright.contents());
                if (n == n) b = n / 200;
                update();
        }
        private function rTextChangedColor():void{
                var n:Number = Number(rBoxColor.contents());
                if (n == n) rColor = n / 300;
                update();
        }
        private function gTextChangedColor():void{
                var n:Number = Number(gBoxColor.contents());
                if (n == n) gColor = n / 300;
                update();
        }
        private function bTextChangedColor():void{
                var n:Number = Number(bBoxColor.contents());
                if (n == n) bColor = n / 300;
                update();
        }


        private function setr(n:Number):void { r = n; update() }
        private function setg(n:Number):void { g = n; update() }
        private function setb(n:Number):void { b = n; update() }

        private function setColorR(n:Number):void{
           if(cbLockG.state && cbLockB.state){
              //Other locked, we can noting to do

              rColor = 1 - gColor - bColor;
           }
           else{
              rColor = n;

              if(!cbLockG.state && !cbLockB.state){
                 var fGBDelta:Number = gColor - bColor;
                 var fGBSumVacant:Number = 1 - rColor;
                 gColor = (fGBSumVacant + fGBDelta) / 2;
                 bColor = (fGBSumVacant - fGBDelta) / 2

                 if(gColor < 0){
                    gColor = 0;
                    bColor = - fGBDelta;
                    rColor = 1 - bColor;
                 }
                 else if(bColor < 0){
                    bColor = 0;
                    gColor = fGBDelta;
                    rColor = 1 - gColor;
                 }
              }
              else if(cbLockG.state){
                 //Green locked
                 bColor = 1 - rColor - gColor;

                 if(bColor < 0){
                    bColor = 0;
                    rColor = 1 - gColor;
                 }
              }
              else if(cbLockB.state){
                 //Blue locked
                 gColor = 1 - rColor - bColor;

                 if(gColor < 0){
                    gColor = 0;
                    rColor = 1 - bColor;
                 }
              }
           }

           update();
        }
        private function setColorG(n:Number):void{
           if(cbLockR.state && cbLockB.state){
              //Other locked, we can noting to do
              gColor = 1 - rColor - bColor;
           }
           else{
              gColor = n;

              if(!cbLockR.state && !cbLockB.state){
                 var fRBDelta:Number = rColor - bColor;
                 var fRBSumVacant:Number = 1 - gColor;
                 rColor = (fRBSumVacant + fRBDelta) / 2;
                 bColor = (fRBSumVacant - fRBDelta) / 2

                 if(rColor < 0){
                    rColor = 0;
                    bColor = - fRBDelta;
                    gColor = 1 - bColor;
                 }
                 else if(bColor < 0){
                    bColor = 0;
                    rColor = fRBDelta;
                    gColor = 1 - rColor;
                 }
              }
              else if(cbLockR.state){
                 //Red locked
                 bColor = 1 - gColor - rColor;

                 if(bColor < 0){
                    bColor = 0;
                    gColor = 1 - rColor;
                 }
              }
              else if(cbLockB.state){
                 //Blue locked
                 rColor = 1 - gColor - bColor;

                 if(rColor < 0){
                    rColor = 0;
                    gColor = 1 - bColor;
                 }
              }
           }

           update();
        }
        private function setColorB(n:Number):void{
           if(cbLockR.state && cbLockG.state){
              //Other locked, we can noting to do
              bColor = 1 - rColor - gColor;
           }
           else{
              bColor = n;

              if(!cbLockR.state && !cbLockG.state){
                 var fRGDelta:Number = rColor - gColor;
                 var fRGSumVacant:Number = 1 - bColor;
                 rColor = (fRGSumVacant + fRGDelta) / 2;
                 gColor = (fRGSumVacant - fRGDelta) / 2

                 if(rColor < 0){
                    rColor = 0;
                    gColor = - fRGDelta;
                    bColor = 1 - gColor;
                 }
                 else if(gColor < 0){
                    gColor = 0;
                    rColor = fRGDelta;
                    bColor = 1 - rColor;
                 }
              }
              else if(cbLockR.state){
                 //Red locked
                 gColor = 1 - rColor - bColor;

                 if(gColor < 0){
                    gColor = 0;
                    bColor = 1 - rColor;
                 }
              }
              else if(cbLockG.state){
                 //Green locked
                 rColor = 1 - gColor - bColor;

                 if(rColor < 0){
                    rColor = 0;
                    bColor = 1 - gColor;
                 }
              }
           }

           update();
        }



        private function lockListenerR():void{
           if(cbLockR.state){
              rSliderColor.allowDragging(false);
              rBoxColor.setEditable(false);
           }
           else{
              rSliderColor.allowDragging(true);
//              rBoxColor.setEditable(true);
           }
        }
        private function lockListenerG():void{
           if(cbLockG.state){
              gSliderColor.allowDragging(false);
              gBoxColor.setEditable(false);
           }
           else{
              gSliderColor.allowDragging(true);
//              gBoxColor.setEditable(true);
           }
        }
        private function lockListenerB():void{
           if(cbLockR.state){
              bSliderColor.allowDragging(false);
              bBoxColor.setEditable(false);
           }
           else{
              bSliderColor.allowDragging(true);
//              bBoxColor.setEditable(true);
           }
        }


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
