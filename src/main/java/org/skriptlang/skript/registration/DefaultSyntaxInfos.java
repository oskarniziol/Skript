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
package org.skriptlang.skript.registration;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.registration.DefaultSyntaxInfosImpl.ExpressionImpl;
import org.skriptlang.skript.registration.DefaultSyntaxInfosImpl.StructureImpl;
import org.skriptlang.skript.util.Priority;

interface DefaultSyntaxInfos {

	/**
	 * A syntax info to be used for {@link ch.njol.skript.lang.Expression}s.
	 * It differs from a typical info in that it also has a return type.
	 * @param <E> The class providing the implementation of the Expression this info represents.
	 * @param <R> The type of the return type of the Expression.
	 */
	interface Expression<E extends ch.njol.skript.lang.Expression<R>, R> extends SyntaxInfo<E> {

		/**
		 * A priority for expressions that only match simple text.
		 * Example: "[the] console"
		 */
		Priority SIMPLE = Priority.base();

		/**
		 * A priority for expressions that contain other expressions.
		 * Note that this is the default priority for all expression infos.
		 * Example: "[the] first %number% characters of %strings%"
		 */
		Priority COMBINED = Priority.after(SIMPLE);

		/**
		 * A priority for expressions that can match almost anything.
		 * This is likely the case when using regex or multiple expressions next to each other.
		 * Example: "[the] [loop-]<.+>"
		 */
		Priority PATTERN_MATCHES_EVERYTHING = Priority.after(COMBINED);

		/**
		 * @param expressionClass The Expression class the info will represent.
		 * @return An Expression-specific builder for creating a syntax info representing <code>type</code>.
		 * @param <E> The class providing the implementation of the Expression this info represents.
		 * @param <R> The type of the return type of the Expression.
		 */
		@Contract("_, _ -> new")
		static <E extends ch.njol.skript.lang.Expression<R>, R> Builder<? extends Builder<?, E, R>, E, R> builder(Class<E> expressionClass, Class<R> returnType) {
			return new ExpressionImpl.BuilderImpl<>(expressionClass, returnType);
		}

		/**
		 * @return The class representing the supertype of all values the Expression may return.
		 */
		Class<R> returnType();

		/**
		 * An Expression-specific builder is used for constructing a new Expression syntax info.
		 * @see #builder(Class, Class)
		 * @param <B> The type of builder being used.
		 * @param <E> The Expression class providing the implementation of the syntax info being built.
		 * @param <R> The type of the return type of the Expression.
		 */
		interface Builder<B extends Builder<B, E, R>, E extends ch.njol.skript.lang.Expression<R>, R> extends SyntaxInfo.Builder<B, E> {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@Contract("-> new")
			Expression<E, R> build();

		}

	}

	/**
	 * A syntax info to be used for {@link org.skriptlang.skript.lang.structure.Structure}s.
	 * It contains additional details including the {@link EntryValidator} to use, if any.
	 * @param <E> The class providing the implementation of the Structure this info represents.
	 */
	interface Structure<E extends org.skriptlang.skript.lang.structure.Structure> extends SyntaxInfo<E> {

		/**
		 * @param structureClass The Structure class the info will represent.
		 * @return A Structure-specific builder for creating a syntax info representing <code>type</code>.
		 * @param <E> The class providing the implementation of the Structure this info represents.
		 */
		@Contract("_ -> new")
		static <E extends org.skriptlang.skript.lang.structure.Structure> Builder<? extends Builder<?, E>, E> builder(Class<E> structureClass) {
			return new StructureImpl.BuilderImpl<>(structureClass);
		}

		/**
		 * @return The entry validator to use for handling the Structure's entries.
		 *  If null, the Structure is expected to manually handle any entries.
		 */
		@Nullable
		EntryValidator entryValidator();

		/**
		 * A Structure-specific builder is used for constructing a new Structure syntax info.
		 * @see #builder(Class)
		 * @param <B> The type of builder being used.
		 * @param <E> The Structure class providing the implementation of the syntax info being built.
		 */
		interface Builder<B extends Builder<B, E>, E extends org.skriptlang.skript.lang.structure.Structure> extends SyntaxInfo.Builder<B, E> {

			/**
			 * Sets the entry validator the Structure will use for handling entries.
			 * @param entryValidator The entry validator to use.
			 * @return This builder.
			 * @see Structure#entryValidator()
			 */
			@Contract("_ -> this")
			B entryValidator(EntryValidator entryValidator);

			/**
			 * {@inheritDoc}
			 */
			@Override
			@Contract("-> new")
			Structure<E> build();

		}

	}

}