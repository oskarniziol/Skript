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
/**
 * Contains the default effects of Skript.
 * 
 * @author Peter Güttinger
 */
package ch.njol.skript.effects.base;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * A class that is to be used for boolean expressions.
 * <p>
 * A boolean expression should be a condition for getting it's value.
 */
public abstract class SetEffect<T> extends Effect {

	private Expression<Boolean> value;
	private Expression<T> expression;
	private boolean make, negated;

	/**
	 * Registers an effect with patterns "set property of %type% to %boolean%" and "set %types%'[s] property to %boolean%"
	 * 
	 * @param effect The SetEffect class that you are registering.
	 * @param property The property of the syntax.
	 * @param type The type classinfo of the syntax. Can be plural.
	 */
	public static void register(Class<? extends SetEffect<?>> effect, String property, String type) {
		Skript.registerEffect(effect, "set " + property + " of %" + type + "% to %boolean%",
				"set %" + type + "%'[s] " + property + " to %boolean%");
	}
	
	/**
	 * Registers an effect with patterns "set property of %type% to %boolean%", "set %types%'[s] property to %boolean%"
	 * and "make %types% makeProperty" with "force %types% to makeProperty"
	 * 
	 * @param effect The SetEffect class that you are registering.
	 * @param property The property of the syntax.
	 * @param makeProperty The property of the syntax used for "make %types% makeProperty".
	 * @param type The type classinfo of the syntax. Can be plural.
	 */
	public static void registerMake(Class<? extends SetEffect<?>> effect, String property, String makeProperty, String type) {
		Skript.registerEffect(effect, "set " + property + " of %" + type + "% to %boolean%",
				"set %" + type + "%'[s] " + property + " to %boolean%",
				"make %" + type + "% " + makeProperty,
				"force %" + type + "% to " + makeProperty,
				"make %" + type + "% not " + makeProperty,
				"force %" + type + "% to not " + makeProperty);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		make = matchedPattern >= 2;
		if (exprs.length == 1) {
			if (!make)
				throw new SkriptAPIException("There was not two expressions in the SetEffect class '" + getClass().getName() + "' exprs: " + Arrays.toString(exprs));
			expression = (Expression<T>) exprs[0];
			negated = matchedPattern >= 4;
			return true;
		}
		expression = (Expression<T>) exprs[matchedPattern];
		value = (Expression<Boolean>) exprs[matchedPattern ^ 1];
		return true;
	}

	/**
	 * Returns the value of the boolean expression used.
	 * 
	 * @param event The event that is calling this syntax.
	 * @return boolean from the boolean expression used in this set effect.
	 */
	@Nullable
	protected final Boolean getBoolean(Event event) {
		if (value == null)
			return null;
		return value.getSingle(event);
	}

	/**
	 * Returns the expression that was registered as the type for this SetEffect.
	 * 
	 * @return Expression<T> for getting the values of the expression used.
	 */
	protected final Expression<T> getExpression() {
		return expression;
	}

	/**
	 * Return a BiComsumer that will be used to apply the boolean value to the type.
	 */
	protected abstract BiConsumer<T, Boolean> apply();

	/**
	 * Return the property name of this SetEffect used for the {@link #toString(Event, boolean)} method.
	 * 
	 * @return String representing the name of the property registered for this syntax.
	 */
	protected abstract String getPropertyName();

	@Override
	protected void execute(Event event) {
		boolean value = make ? !negated : negated ? !getBoolean(event) : getBoolean(event);
		getExpression().stream(event).forEach(expression -> apply().accept(expression, value));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug || event == null)
			return "setting " + getPropertyName();
		return "set " + getPropertyName() + " of " + expression.toString(event, debug) + " to " + value.toString(event, debug);
	}

}
