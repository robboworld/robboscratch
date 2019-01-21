package ui.parts{
import blocks.*;

import flash.display.*;
import flash.events.*;
import flash.geom.*;
import flash.net.*;
import flash.text.*;
import flash.utils.ByteArray;

import ui.parts.UIPart;

import util.*;

import assets.Resources;
import flash.display.*;


import uiwidgets.DialogBox;



public class SensorSelectorPanel extends Sprite {


           private var icons:Array = [Resources.createBmp('sensor32_None'),
                                      Resources.createBmp('lab_sensor32_Temperature'),
                                      Resources.createBmp('lab_sensor32_Clamps'),
                                     ];
           private var iconsHover:Array = [Resources.createBmp('sensor32_None_Hover'),
                                           Resources.createBmp('lab_sensor32_Temperature_Hover'),
                                           Resources.createBmp('lab_sensor32_Clamps_Hover'),
                                         ];



        public function SensorSelectorPanel(sensorSelector:SensorSelector){

           for(var f:int = 0; f < icons.length; f++){
              var sprite:Sprite = new Sprite();
              sprite.addChild(icons[f]);
              sprite.x = f * 40;
              sprite.addEventListener(MouseEvent.MOUSE_OVER, makeOver(f, sprite));
              sprite.addEventListener(MouseEvent.MOUSE_OUT,  makeOut(f, sprite));
              sprite.addEventListener(MouseEvent.CLICK,      click(f, sensorSelector));
              this.addChild(sprite);
           }
        }


        private function click(f: int, sensorSelector:SensorSelector):Function{
           return function(evt:MouseEvent):void {
              SensorSelector.dialogBox.cancel();
              SensorSelector.dialogBox = null;
              sensorSelector.select(f);
           };
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
}}
