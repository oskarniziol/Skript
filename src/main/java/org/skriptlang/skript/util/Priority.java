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
package org.skriptlang.skript.util;

/**
 * Priorities are used for things like ordering syntax and loading structures in a specific order.
 */
public interface Priority extends Comparable<Priority> {

	/**
	 * Constructs a priority with the provided integer using the default implementation.
	 * In this implementation, the lower the priority, the more important it is. For example:
	 * priority of 1 (loads first), priority of 2 (loads second), priority of 3 (loads third)
	 */
	static Priority of(int priority) {
		return new PriorityImpl(priority);
	}

	/**
	 * @return An integer representing this priority.
	 */
	int priority();

}
