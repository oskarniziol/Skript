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

import org.skriptlang.skript.lang.expression.Expression;

public interface TimeSensitiveExpression<Type> extends Expression<Type> {

	/**
	 * Sets the time of this expression, i.e. whether the returned value represents this expression before, during or after some context.
	 * @param time The new time of this expression.
	 */
	void setTime(TimeState time);

	/**
	 * @return The time of this expression.
	 * @see #setTime(TimeState)
	 */
	TimeState getTime();

}
