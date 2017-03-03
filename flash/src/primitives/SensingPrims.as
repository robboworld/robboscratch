/*
 * Scratch Project Editor and Player
 * Copyright (C) 2014 Massachusetts Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

// SensingPrims.as
// John Maloney, April 2010
//
// Sensing primitives.

package primitives {
   import flash.display.Bitmap;
   import flash.display.BitmapData;
   import flash.geom.Point;
   import flash.geom.Rectangle;
   import flash.utils.Dictionary;
   import flash.utils.getTimer;


   import blocks.Block;

   import interpreter.Interpreter;

   import scratch.ScratchObj;
   import scratch.ScratchSprite;
   import scratch.ScratchStage;

public class SensingPrims {

   private var app:Scratch;
   private var interp:Interpreter;

   public function SensingPrims(app:Scratch, interpreter:Interpreter) {
      this.app = app;
      this.interp = interpreter;
   }

   public function addPrimsTo(primTable:Dictionary):void {
      // sensing
      primTable['touching:']        = primTouching;
      primTable['touchingColor:']      = primTouchingColor;
      primTable['color:sees:']      = primColorSees;

      primTable['doAsk']            = primAsk;
      primTable['answer']           = function(b:*):* { return app.runtime.lastAnswer };

      primTable['mousePressed']     = function(b:*):* { return app.gh.mouseIsDown };
      primTable['mouseX']           = function(b:*):* { return app.stagePane.scratchMouseX() };
      primTable['mouseY']           = function(b:*):* { return app.stagePane.scratchMouseY() };
      primTable['timer']            = function(b:*):* { return app.runtime.timer() };
      primTable['timerReset']       = function(b:*):* { app.runtime.timerReset() };
      primTable['keyPressed:']      = primKeyPressed;
      primTable['distanceTo:']      = primDistanceTo;
      primTable['getAttribute:of:'] = primGetAttribute;
      primTable['soundLevel']       = function(b:*):* { return app.runtime.soundLevel() };
      primTable['isLoud']           = function(b:*):* { return app.runtime.isLoud() };
      primTable['timestamp']        = primTimestamp;
      primTable['timeAndDate']      = function(b:*):* { return app.runtime.getTimeString(interp.arg(b, 0)) };
      primTable['getUserName']      = function(b:*):* { return '' };

      // sensor
      primTable['sensor:']       = function(b:*):* { return app.runtime.getSensor(interp.arg(b, 0)) };
      primTable['sensorPressed:']      = function(b:*):* { return app.runtime.getBooleanSensor(interp.arg(b, 0)) };

      // variable and list watchers
      primTable['showVariable:']       = primShowWatcher;
      primTable['hideVariable:']       = primHideWatcher;
      primTable['showList:']           = primShowListWatcher;
      primTable['hideList:']           = primHideListWatcher;
      primTable['sensorRobot']         = primAnalogRobot;
      primTable['brightRobot']         = primBrightRobot;
      primTable['sensorLab']           = primAnalogLab;
      primTable['sensorLabRaw']        = sensorLabRaw;
      primTable['sensorLabDigitalRaw'] = sensorLabDigitalRaw;
      primTable['setLabDigitalRaw']    = setLabDigitalRaw;
      primTable['setLabDigitalPwmRaw'] = setLabDigitalPwmRaw;
      primTable['robotMoveFinished']   = robotMoveFinished;
      primTable['robotStartButton']    = robotStartButton;
      primTable['labButtons']          = labButtons;

   }


   // TODO: move to stage
   static private var stageRect:Rectangle = new Rectangle(0, 0, 480, 360);

   private function primAnalogRobot(b:Block):int {
      var a:String = interp.arg(b, 0);

/*
      if(a == 'Encoder L'){
         return app.runtime.analogsRobot[0];
      }
      if(a == 'Encoder R'){
         return app.runtime.analogsRobot[1];
      }
*/
      if(a == 'trip meter L'){
         return app.robotMotorLeft.pathCorrected;
      }
      if(a == 'trip meter R'){
         return app.robotMotorRight.pathCorrected;
      }
      if(a == 'sensor 1'){
         return app.robotSensors[0].analog[3];
      }
      if(a == 'sensor 2'){
         return app.robotSensors[1].analog[3];
      }
      if(a == 'sensor 3'){
         return app.robotSensors[2].analog[3];
      }
      if(a == 'sensor 4'){
         return app.robotSensors[3].analog[3];
      }
      if(a == 'sensor 5'){
         return app.robotSensors[4].analog[3];
      }

      return 0;

//      var c:int = a.charCodeAt(a.length - 1) - 48;
//      return app.runtime.analogsRobot[c];
   }






   private function primBrightRobot(b:Block):int {
      var sensor:String  = interp.arg(b, 0);
      var channel:String = interp.arg(b, 1);

      //trace("SUKA=" + b.args[1].argValue);
      //b.args[1].argValue = 0;


      var sensorNumber:int;


      if(sensor == 'sensor 1'){
         sensorNumber = 0;
      }
      if(sensor == 'sensor 2'){
         sensorNumber = 1;
      }
      if(sensor == 'sensor 3'){
         sensorNumber = 2;
      }
      if(sensor == 'sensor 4'){
         sensorNumber = 3;
      }
      if(sensor == 'sensor 5'){
         sensorNumber = 4;
      }



      b.base.setColor(  app.robotSensors[sensorNumber].analog[0] * 65536
                      + app.robotSensors[sensorNumber].analog[1] * 256
                      + app.robotSensors[sensorNumber].analog[2]);
      b.base.redraw();



      if(channel == 'R'){
         return app.robotSensors[sensorNumber].analog[0];
      }
      if(channel == 'G'){
         return app.robotSensors[sensorNumber].analog[1];
      }
      if(channel == 'B'){
         return app.robotSensors[sensorNumber].analog[2];
      }
      if(channel == 'Bright'){
         return app.robotSensors[sensorNumber].analog[3];
      }

      return 0;
   }







   private function primAnalogLab(b:Block):int {
      var a:String = interp.arg(b, 0);

      var c:int;
      if(a == "slider"){
         return app.labSlider;
      }
      else if(a == "sound sensor"){
         return app.labSound;
      }
      else{
         return app.labLight;
      }
   }


   private function sensorLabRaw(b:Block):int {
      var a:String = interp.arg(b, 0);

      if(a == "A0"){
         return app.labAnalogRaw[0];
      }
      else if(a == "A1"){
         return app.labAnalogRaw[1];
      }
      else if(a == "A2"){
         return app.labAnalogRaw[2];
      }
      else if(a == "A3"){
         return app.labAnalogRaw[3];
      }
      else if(a == "A4"){
         return app.labAnalogRaw[4];
      }
      else{
         return app.labAnalogRaw[5];
      }
   }


   private function sensorLabDigitalRaw(b:Block):Boolean {
      var a:String = interp.arg(b, 0);

      if(a == "D8"){
         return app.labDigital[0];
      }
      else if(a == "D9"){
         return app.labDigital[1];
      }
      else if(a == "D10"){
         return app.labDigital[2];
      }
      else if(a == "D11"){
         return app.labDigital[3];
      }
      else if(a == "D12"){
         return app.labDigital[4];
      }
      else{
         return app.labDigital[5];
      }
   }


   private function setLabDigitalRaw(b:Block):void {
      var pin:String = interp.arg(b, 0);
      var value:String = interp.arg(b, 1);

      if(pin == "D2"){
         app.robotCommunicator.setLabDigital(2, value == "on" ? true : false);
      }
      else if(pin == "D3"){
         app.robotCommunicator.setLabDigital(3, value == "on" ? true : false);
      }
      else if(pin == "D4"){
         app.robotCommunicator.setLabDigital(4, value == "on" ? true : false);
      }
      else if(pin == "D5"){
         app.robotCommunicator.setLabDigital(5, value == "on" ? true : false);
      }
      else if(pin == "D6"){
         app.robotCommunicator.setLabDigital(6, value == "on" ? true : false);
      }
      else{
         app.robotCommunicator.setLabDigital(7, value == "on" ? true : false);
      }
   }



   private function setLabDigitalPwmRaw(b:Block):void {
      var pin:String = interp.arg(b, 0);


      var value:int = check0_255(interp.arg(b, 1));



      if(pin == "D3"){
         app.robotCommunicator.setLabDigitalPwm(3, value);
      }
      else if(pin == "D5"){
         app.robotCommunicator.setLabDigitalPwm(5, value);
      }
      else{
         app.robotCommunicator.setLabDigitalPwm(6, value);
      }
   }


   private function check0_255(value:int):int{
      if(value > 255) return 255;
      if(value < 0)   return 0;

      return value;
   }



   private function robotMoveFinished(b:Block):Boolean{
      if (interp.activeThread.firstTime) {
         interp.startTimer(0.01);

      }
      else if (interp.checkTimer()){
      }

      return app.robotMoveFinished();
   }



   private function robotStartButton(b:Block):Boolean{
      if (interp.activeThread.firstTime) {
         interp.startTimer(0.01);
      }
      else if (interp.checkTimer()){
      }

      return app.robotStartButton;
   }




   private function labButtons(b:Block):Boolean {
      var a:String = interp.arg(b, 0);

      if(a == "1"){
         return app.labButtons[0];
      }
      else if(a == "2"){
         return app.labButtons[1];
      }
      else if(a == "3"){
         return app.labButtons[2];
      }
      else if(a == "4"){
         return app.labButtons[3];
      }
      else{
         return app.labButtons[4];
      }
   }





   private function primTouching(b:Block):Boolean {
      var s:ScratchSprite = interp.targetSprite();
      if (s == null) return false;
      var arg:* = interp.arg(b, 0);
      if ('_edge_' == arg) {
         if(stageRect.containsRect(s.getBounds(s.parent))) return false;

         var r:Rectangle = s.bounds();
         return  (r.left < 0) || (r.right > ScratchObj.STAGEW) ||
               (r.top < 0) || (r.bottom > ScratchObj.STAGEH);
      }
      if ('_mouse_' == arg) {
         return mouseTouches(s);
      }
      if (!s.visible) return false;

      ;
      var sBM:BitmapData = s.bitmap(true);
      for each (var s2:ScratchSprite in app.stagePane.spritesAndClonesNamed(arg))
         if (s2.visible && sBM.hitTest(s.bounds().topLeft, 1, s2.bitmap(true), s2.bounds().topLeft, 1))
            return true;

      return false;
   }

   public function mouseTouches(s:ScratchSprite):Boolean {
      // True if the mouse touches the given sprite. This test is independent
      // of whether the sprite is hidden or 100% ghosted.
      // Note: p and r are in the coordinate system of the sprite's parent (i.e. the ScratchStage).
      if (!s.parent) return false;
      if(!s.getBounds(s).contains(s.mouseX, s.mouseY)) return false;
      var r:Rectangle = s.bounds();
      if (!r.contains(s.parent.mouseX, s.parent.mouseY)) return false;
      return s.bitmap().hitTest(r.topLeft, 1, new Point(s.parent.mouseX, s.parent.mouseY));
   }

// private var testSpr:Sprite;
// private var myBMTest:Bitmap;
// private var stageBMTest:Bitmap;
   private function primTouchingColor(b:Block):Boolean {
      // Note: Attempted to switch app.stage.quality to LOW to disable anti-aliasing, which
      // can create false colors. Unfortunately, that caused serious performance issues.
      var s:ScratchSprite = interp.targetSprite();
      if (s == null) return false;
      var c:int = interp.arg(b, 0) | 0xFF000000;
      var myBM:BitmapData = s.bitmap(true);
      var stageBM:BitmapData = stageBitmapWithoutSpriteFilteredByColor(s, c);
//    if(s.objName == 'sensor') {
//       if(!testSpr) {
//          testSpr = new Sprite();
//          app.stage.addChild(testSpr);
//          myBMTest = new Bitmap();
//          myBMTest.y = 300;
//          testSpr.addChild(myBMTest);
//          stageBMTest = new Bitmap();
//          stageBMTest.y = 300;
//          testSpr.addChild(stageBMTest);
//       }
//       myBMTest.bitmapData = myBM;
//       stageBMTest.bitmapData = stageBM;
//       testSpr.graphics.clear();
//       testSpr.graphics.lineStyle(1);
//       testSpr.graphics.drawRect(myBM.width, 300, stageBM.width, stageBM.height);
//    }
      return myBM.hitTest(new Point(0, 0), 1, stageBM, new Point(0, 0), 1);
   }

   private function primColorSees(b:Block):Boolean {
      // Note: Attempted to switch app.stage.quality to LOW to disable anti-aliasing, which
      // can create false colors. Unfortunately, that caused serious performance issues.
      var s:ScratchSprite = interp.targetSprite();
      if (s == null) return false;
      var c1:int = interp.arg(b, 0) | 0xFF000000;
      var c2:int = interp.arg(b, 1) | 0xFF000000;
      var myBM:BitmapData = bitmapFilteredByColor(s.bitmap(true), c1);
      var stageBM:BitmapData = stageBitmapWithoutSpriteFilteredByColor(s, c2);
//    if(!testSpr) {
//       testSpr = new Sprite();
//       testSpr.y = 300;
//       app.stage.addChild(testSpr);
//       stageBMTest = new Bitmap();
//       testSpr.addChild(stageBMTest);
//       myBMTest = new Bitmap();
//       myBMTest.filters = [new GlowFilter(0xFF00FF)];
//       testSpr.addChild(myBMTest);
//    }
//    myBMTest.bitmapData = myBM;
//    stageBMTest.bitmapData = stageBM;
//    testSpr.graphics.clear();
//    testSpr.graphics.lineStyle(1);
//    testSpr.graphics.drawRect(0, 0, stageBM.width, stageBM.height);
      return myBM.hitTest(new Point(0, 0), 1, stageBM, new Point(0, 0), 1);
   }

   // used for debugging:
   private var debugView:Bitmap;
   private function showBM(bm:BitmapData):void {
      if (debugView == null) {
         debugView = new Bitmap();
         debugView.x = 100;
         debugView.y = 600;
         app.addChild(debugView);
      }
      debugView.bitmapData = bm;
   }

// private var testBM:Bitmap = new Bitmap();
   private function bitmapFilteredByColor(srcBM:BitmapData, c:int):BitmapData {
//    if(!testBM.parent) {
//       testBM.y = 360; testBM.x = 15;
//       app.stage.addChild(testBM);
//    }
//    testBM.bitmapData = srcBM;
      var outBM:BitmapData = new BitmapData(srcBM.width, srcBM.height, true, 0);
      outBM.threshold(srcBM, srcBM.rect, srcBM.rect.topLeft, '==', c, 0xFF000000, 0xF0F8F8F0); // match only top five bits of each component
      return outBM;
   }

   private function stageBitmapWithoutSpriteFilteredByColor(s:ScratchSprite, c:int):BitmapData {
      return app.stagePane.getBitmapWithoutSpriteFilteredByColor(s, c);
   }

   private function primAsk(b:Block):void {
      if (app.runtime.askPromptShowing()) {
         // wait if (1) some other sprite is asking (2) this question is answered (when firstTime is false)
         interp.doYield();
         return;
      }
      var obj:ScratchObj = interp.targetObj();
      if (interp.activeThread.firstTime) {
         var question:String = interp.arg(b, 0);
         if ((obj is ScratchSprite) && (obj.visible)) {
            ScratchSprite(obj).showBubble(question, 'talk', true);
            app.runtime.showAskPrompt('');
         } else {
            app.runtime.showAskPrompt(question);
         }
         interp.activeThread.firstTime = false;
         interp.doYield();
      } else {
         if ((obj is ScratchSprite) && (obj.visible)) ScratchSprite(obj).hideBubble();
         interp.activeThread.firstTime = true;
      }
   }

   private function primKeyPressed(b:Block):Boolean {
      var key:String = interp.arg(b, 0);
      var ch:int = key.charCodeAt(0);
      if (ch > 127) return false;
      if (key == 'left arrow') ch = 28;
      if (key == 'right arrow') ch = 29;
      if (key == 'up arrow') ch = 30;
      if (key == 'down arrow') ch = 31;
      if (key == 'space') ch = 32;
      return app.runtime.keyIsDown[ch];
   }

   private function primDistanceTo(b:Block):Number {
      var s:ScratchSprite = interp.targetSprite();
      var p:Point = mouseOrSpritePosition(interp.arg(b, 0));
      if ((s == null) || (p == null)) return 0;
      var dx:Number = p.x - s.scratchX;
      var dy:Number = p.y - s.scratchY;
      return Math.sqrt((dx * dx) + (dy * dy));
   }

   private function primGetAttribute(b:Block):* {
      var attribute:String = interp.arg(b, 0);
      var obj:ScratchObj = app.stagePane.objNamed(String(interp.arg(b, 1)));
      if (!(obj is ScratchObj)) return 0;
      if (obj is ScratchSprite) {
         var s:ScratchSprite = ScratchSprite(obj);
         if ('x position' == attribute) return s.scratchX;
         if ('y position' == attribute) return s.scratchY;
         if ('direction' == attribute) return s.direction;
         if ('costume #' == attribute) return s.costumeNumber();
         if ('costume name' == attribute) return s.currentCostume().costumeName;
         if ('size' == attribute) return s.getSize();
         if ('volume' == attribute) return s.volume;
      } if (obj is ScratchStage) {
         if ('background #' == attribute) return obj.costumeNumber(); // support for old 1.4 blocks
         if ('backdrop #' == attribute) return obj.costumeNumber();
         if ('backdrop name' == attribute) return obj.currentCostume().costumeName;
         if ('volume' == attribute) return obj.volume;
      }
      if (obj.ownsVar(attribute)) return obj.lookupVar(attribute).value; // variable
      return 0;
   }

   private function mouseOrSpritePosition(arg:String):Point {
      if (arg == '_mouse_') {
         var w:ScratchStage = app.stagePane;
         return new Point(w.scratchMouseX(), w.scratchMouseY());
      } else {
         var s:ScratchSprite = app.stagePane.spriteNamed(arg);
         if (s == null) return null;
         return new Point(s.scratchX, s.scratchY);
      }
      return null;
   }

   private function primShowWatcher(b:Block):* {
      var obj:ScratchObj = interp.targetObj();
      if (obj) app.runtime.showVarOrListFor(interp.arg(b, 0), false, obj);
   }

   private function primHideWatcher(b:Block):* {
      var obj:ScratchObj = interp.targetObj();
      if (obj) app.runtime.hideVarOrListFor(interp.arg(b, 0), false, obj);
   }

   private function primShowListWatcher(b:Block):* {
      var obj:ScratchObj = interp.targetObj();
      if (obj) app.runtime.showVarOrListFor(interp.arg(b, 0), true, obj);
   }

   private function primHideListWatcher(b:Block):* {
      var obj:ScratchObj = interp.targetObj();
      if (obj) app.runtime.hideVarOrListFor(interp.arg(b, 0), true, obj);
   }

   private function primTimestamp(b:Block):* {
      const millisecondsPerDay:int = 24 * 60 * 60 * 1000;
      const epoch:Date = new Date(2000, 0, 1); // Jan 1, 2000 (Note: Months are zero-based.)
      var now:Date = new Date();
      var dstAdjust:int = now.timezoneOffset - epoch.timezoneOffset;
      var mSecsSinceEpoch:Number = now.time - epoch.time;
      mSecsSinceEpoch += ((now.timezoneOffset - dstAdjust) * 60 * 1000); // adjust to UTC (GMT)
      return mSecsSinceEpoch / millisecondsPerDay;
   }
}}
