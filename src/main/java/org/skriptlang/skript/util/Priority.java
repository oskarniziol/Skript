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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

/**
 * Priorities are used for things like ordering syntax and loading structures in a specific order.
 */
public interface Priority extends Comparable<Priority> {

	/**
	 * @return A base priority for other priorities to build relationships off of.
	 */
	@Contract("-> new")
	static Priority base() {
		return new PriorityImpl();
	}

	/**
	 * Constructs a new priority that is before <code>priority</code>.
	 * Note that this method
	 * @param priority The priority that will be after the returned priority.
	 * @return A priority that is before <code>priority</code>.
	 */
	@Contract("_ -> new")
	static Priority before(Priority priority) {
		return new PriorityImpl(priority, true);
	}

	@Contract("_ -> new")
	static Priority after(Priority priority) {
		return new PriorityImpl(priority, false);
	}

	/**
	 * @return A collection of all priorities this priority is after.
	 */
	@Unmodifiable
	Collection<Priority> after();

	/**
	 * @return A collection of all priorities this priority is before.
	 */
	@Unmodifiable
	Collection<Priority> before();

}
