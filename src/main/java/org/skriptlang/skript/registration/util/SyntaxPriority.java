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
package org.skriptlang.skript.registration.util;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.SyntaxElement;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link Priority} that enables positioning itself around specific {@link SyntaxElement} classes.
 * Note that for {@link SyntaxRegistry} implementations to work with this class, they must account for it as this class's
 * {@link #compareTo(Priority)} defers to the {@link #compareTo(Priority)} of the priority backing it.
 */
@ApiStatus.Experimental
public class SyntaxPriority implements Priority {

	/**
	 * Constructs a builder for a syntax priority.
	 * @param priority The priority that will back the syntax priority.
	 * @return A builder for creating a syntax priority backed by <code>priority</code>.
	 */
	@Contract("_ -> new")
	public static Builder builder(Priority priority) {
		return new Builder(priority);
	}

	private final Priority priority;
	private final Collection<Class<? extends SyntaxElement>> after;
	private final Collection<Class<? extends SyntaxElement>> before;

	private SyntaxPriority(Priority priority, Collection<Class<? extends SyntaxElement>> after, Collection<Class<? extends SyntaxElement>> before) {
		this.priority = priority;
		this.after = ImmutableSet.copyOf(after);
		this.before = ImmutableSet.copyOf(before);
	}

	/**
	 * @return A collection of all syntax elements {@link SyntaxInfo}s using this priority should be after.
	 */
	@Unmodifiable
	public Collection<Class<? extends SyntaxElement>> afterElements() {
		return after;
	}

	/**
	 * @return A collection of all syntax elements {@link SyntaxInfo}s using this priority should be before.
	 */
	@Unmodifiable
	public Collection<Class<? extends SyntaxElement>> beforeElements() {
		return before;
	}

	//
	// Priority Implementations
	//

	@Override
	public Collection<Priority> after() {
		return priority.after();
	}

	@Override
	public Collection<Priority> before() {
		return priority.before();
	}

	@Override
	public int compareTo(Priority other) {
		return priority.compareTo(other);
	}

	//
	// Builder Implementation
	//

	/**
	 * A builder is used for constructing a new syntax priority.
	 * @see #builder(Priority)
	 */
	public static final class Builder {

		private final Priority priority;
		private final Set<Class<? extends SyntaxElement>> after = new HashSet<>();
		private final Set<Class<? extends SyntaxElement>> before = new HashSet<>();

		/**
		 * @param priority The priority that will back the syntax priority.
		 */
		Builder(Priority priority) {
			this.priority = priority;
		}

		/**
		 * Adds a syntax element the priority will be after.
		 * @param element A syntax element the priority will be after.
		 * @return This builder.
		 * @see SyntaxPriority#after
		 */
		public Builder after(Class<? extends SyntaxElement> element) {
			after.add(element);
			return this;
		}

		/**
		 * Adds syntax elements the priority will be after.
		 * @param elements Syntax elements the priority will be after.
		 * @return This builder.
		 * @see SyntaxPriority#after
		 */
		@SafeVarargs
		public final Builder after(Class<? extends SyntaxElement>... elements) {
			Collections.addAll(after, elements);
			return this;
		}

		/**
		 * Adds syntax elements the priority will be before.
		 * @param elements Syntax elements the priority will be before.
		 * @return This builder.
		 * @see SyntaxPriority#before
		 */
		public Builder after(Collection<Class<? extends SyntaxElement>> elements) {
			after.addAll(elements);
			return this;
		}

		/**
		 * Adds a syntax element the priority will be before.
		 * @param element A syntax element the priority will be before.
		 * @return This builder.
		 * @see SyntaxPriority#before
		 */
		public Builder before(Class<? extends SyntaxElement> element) {
			before.add(element);
			return this;
		}

		/**
		 * Adds syntax elements the priority will be before.
		 * @param elements Syntax elements the priority will be before.
		 * @return This builder.
		 * @see SyntaxPriority#before
		 */
		@SafeVarargs
		public final Builder before(Class<? extends SyntaxElement>... elements) {
			Collections.addAll(before, elements);
			return this;
		}

		/**
		 * Adds syntax elements the priority will be before.
		 * @param elements Syntax elements the priority will be before.
		 * @return This builder.
		 * @see SyntaxPriority#before
		 */
		public Builder before(Collection<Class<? extends SyntaxElement>> elements) {
			before.addAll(elements);
			return this;
		}

		/**
		 * Builds a new syntax priority from the set details.
		 * @return A syntax priority.
		 */
		public SyntaxPriority build() {
			if (after.isEmpty() && before.isEmpty()) {
				// no point in using this if both are empty...
				throw new SkriptAPIException("both 'after' and 'before' cannot be unset");
			}
			return new SyntaxPriority(priority, after, before);
		}

	}

}
