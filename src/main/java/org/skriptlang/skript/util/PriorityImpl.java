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

import org.jetbrains.annotations.NotNull;

public class PriorityImpl implements Priority {

	private final int priority;

	PriorityImpl(int priority) {
		this.priority = priority;
	}

	public int priority() {
		return priority;
	}

	@Override
	public int compareTo(@NotNull Priority other) {
		return Integer.compare(priority, other.priority());
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		return priority() == ((Priority) other).priority();
	}

	@Override
	public int hashCode() {
		return priority();
	}

}
