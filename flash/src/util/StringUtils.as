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
package util {
import flash.utils.Dictionary;

public class StringUtils {
	// format('My {animal} is named {name}.', {animal:'goat',name:'Eric'}) => 'My goat is named Eric.'
	// Tokens not contained in the dictionary will not be modified.
	public static function substitute(s:String, context:Dictionary):String {
		for (var token:String in context) {
			s = s.replace('{'+token+'}', context[token]);
		}
		return s;
	}
	
	/**
	 *       Determines whether the specified string ends with the specified suffix.
	 *       @param input The string that the suffix will be checked against.
	 *       @param suffix The suffix that will be tested against the string.
	 *       @returns True if the string ends with the suffix, false if it does not.
	 *       @langversion ActionScript 3.0
	 *       @playerversion Flash 9.0
	 *       @tiptext
	 */
	public static function endsWith(input:String, suffix:String):Boolean {
		return (suffix == input.substring(input.length - suffix.length));
	}
}
}
