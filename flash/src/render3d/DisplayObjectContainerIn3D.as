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

package render3d {

import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;

/**
 *   A display object container which renders in 3D instead
 *   @author Shane M. Clements, shane.m.clements@gmail.com
 */
public class DisplayObjectContainerIn3D extends Sprite implements IRenderIn3D {
	public function setStage(stage:Sprite, penLayer:DisplayObject):void {
	}

	public function getUIContainer():Sprite {
		return null;
	}

	public function getRenderedChild(dispObj:DisplayObject, width:Number, height:Number, for_carry:Boolean = false):BitmapData {
		return null;
	}

	public function getOtherRenderedChildren(skipObj:DisplayObject, scale:Number):BitmapData {
		return null;
	}

	public function updateRender(dispObj:DisplayObject, renderID:String = null, renderOpts:Object = null):void {
	}

	public function updateFilters(dispObj:DisplayObject, effects:Object):void {
	}

	public function updateGeometry(dispObj:DisplayObject):void {
	}

	public function onStageResize(e:Event = null):void {
	}

	public function getRender(bmd:BitmapData):void {
	}

	public function setStatusCallback(callback:Function):void {
	}

	public function spriteIsLarge(dispObj:DisplayObject):Boolean {
		return false;
	}
}
}

internal final class Dbg {
	public static function printObj(obj:*):String {
		var memoryHash:String;

		try {
			FakeClass(obj);
		}
		catch (e:Error) {
			memoryHash = String(e).replace(/.*([@|\$].*?) to .*$/gi, '$1');
		}
		return "PZDC zaebalsya includit'";//flash.utils.getQualifiedClassName(obj) + memoryHash;
	}
}

internal final class FakeClass {
}
