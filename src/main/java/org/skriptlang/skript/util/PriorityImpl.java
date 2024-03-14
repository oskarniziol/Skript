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

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class PriorityImpl implements Priority {

	private final Set<Priority> after;

	private final Set<Priority> before;

	PriorityImpl() {
		this.after = ImmutableSet.of();
		this.before = ImmutableSet.of();
	}

	PriorityImpl(Priority priority, boolean isBefore) {
		Set<Priority> after = new HashSet<>();
		Set<Priority> before = new HashSet<>();
		if (isBefore) {
			before.add(priority);
		} else {
			after.add(priority);
		}
		after.addAll(priority.after());
		before.addAll(priority.before());

		this.after = ImmutableSet.copyOf(after);
		this.before = ImmutableSet.copyOf(before);
	}

	@Override
	public int compareTo(Priority other) {
		if (this == other) {
			return 0;
		}

		if (this.before().contains(other) || other.after().contains(this)) { // we are before other
			return -1;
		}

		if (this.after().contains(other) || other.before().contains(this)) { // we are after other
			return 1;
		}

		// there is no meaningful relationship, we consider ourselves the same
		// however, in cases of a custom implementation, we defer to them to determine the relationship
		return (other instanceof PriorityImpl) ? 0 : (other.compareTo(this) * -1);
	}

	@Override
	public Collection<Priority> after() {
		return after;
	}

	@Override
	public Collection<Priority> before() {
		return before;
	}

}
