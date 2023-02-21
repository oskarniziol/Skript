/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package org.skriptlang.skript.base.event.contextvalues;

/**
 * TimeStates provide time context to an expression.
 * Essentially, whether the returned value of an expression represents it before, during or after some context.
 * @see TimeSensitiveExpression
 */
public enum TimeState {

	/**
	 * The returned value of an expression is <b>before</b> some context.
	 */
	PAST,

	/**
	 * The returned value of an expression is <b>at the time of (during)</b> some context.
	 */
	PRESENT,

	/**
	 * The returned value of an expression is <b>after</b> some context.
	 */
	FUTURE

}
