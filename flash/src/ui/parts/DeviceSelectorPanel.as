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



public class DeviceSelectorPanel extends Sprite {


           private var icons:Array = [Resources.createBmp('sensor32_None'),
                                      Resources.createBmp('sensor32_Line'),
                                      Resources.createBmp('sensor32_Led'),
                                      Resources.createBmp('sensor32_Light'),
                                      Resources.createBmp('sensor32_Touch'),
                                      Resources.createBmp('sensor32_Proximity'),
                                      Resources.createBmp('sensor32_Ultrasonic'),
                                      Resources.createBmp('sensor32_Color')
                                     ];
           private var iconsHover:Array = [Resources.createBmp('sensor32_None_Hover'),
                                           Resources.createBmp('sensor32_Line_Hover'),
                                           Resources.createBmp('sensor32_Led_Hover'),
                                           Resources.createBmp('sensor32_Light_Hover'),
                                           Resources.createBmp('sensor32_Touch_Hover'),
                                           Resources.createBmp('sensor32_Proximity_Hover'),
                                           Resources.createBmp('sensor32_Ultrasonic_Hover'),
                                           Resources.createBmp('sensor32_Color_Hover')
                                         ];



        public function DeviceSelectorPanel(deviceSelector:DeviceSelector){

           for(var f:int = 0; f < icons.length; f++){
              var sprite:Sprite = new Sprite();
              sprite.addChild(icons[f]);
              sprite.x = f * 40;
              sprite.addEventListener(MouseEvent.MOUSE_OVER, makeOver(f, sprite));
              sprite.addEventListener(MouseEvent.MOUSE_OUT,  makeOut(f, sprite));
              sprite.addEventListener(MouseEvent.CLICK,      click(f, deviceSelector));
              this.addChild(sprite);
           }
        }


        private function click(f: int, deviceSelector:DeviceSelector):Function{
           return function(evt:MouseEvent):void {
              DeviceSelector.dialogBox.cancel();
              DeviceSelector.dialogBox = null;
              deviceSelector.select(f);
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
