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
public class ScratchTextureBitmap extends BitmapData
{
	public function ScratchTextureBitmap(width:int, height:int, transparent:Boolean = true, fillColor:uint = NaN) {
		super(width, height, transparent, fillColor);
	}
}
}


internal final class Dbg
{
	import flash.utils.getQualifiedClassName;
	public static function printObj(obj:*):String
	{
		var memoryHash:String;

		try
		{
			FakeClass(obj);
		}
		catch (e:Error)
		{
			memoryHash = String(e).replace(/.*([@|\$].*?) to .*$/gi, '$1');
		}

		return flash.utils.getQualifiedClassName(obj) + memoryHash;
	}
}

internal final class FakeClass { }
