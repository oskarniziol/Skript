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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.Functions;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.script.Script;

public class EffCallFunction extends Effect {

	@Nullable
	public static Object[] lastReturnValue;

	static {
		Skript.registerEffect(EffCallFunction.class,
			"(call|execute|run) [the] function[s] %strings% [(using|with) [the] argument[s] %-objects%]",
			"(call|execute|run) [the] global function[s] %strings% [(using|with) [the] argument[s] %-objects%]",
			"(call|execute|run) [the] local function[s] %strings% (in|from) [script] %-string% [(using|with) [the] argument[s] %-objects%]"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> functionNames;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> functionScript;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Object> functionArguments;

	private Script containingScript;

	private Kleenean requireLocalFunction;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		containingScript = getParser().getCurrentScript();
		functionNames = (Expression<String>) exprs[0];
		if (matchedPattern == 0) {
			requireLocalFunction = Kleenean.UNKNOWN;
			functionArguments = (Expression<Object>) exprs[1];
		} else if (matchedPattern == 1) {
			requireLocalFunction = Kleenean.FALSE;
			functionArguments = (Expression<Object>) exprs[1];
		} else if (matchedPattern == 2) {
			requireLocalFunction = Kleenean.TRUE;
			functionArguments = (Expression<Object>) exprs[2];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		lastReturnValue = null;
		String parentScript = getParentScript(event);
		Object[][] arguments = createArgumentArray(functionArguments, event);
		for (String functionName : functionNames.getArray(event)) {
			Function<?> functionToExecute = getFunction(functionName, parentScript);
			if (functionToExecute != null) {
				lastReturnValue = functionToExecute.execute(arguments);
				functionToExecute.resetReturnValue();
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String stringRepresentation = "";
		switch (requireLocalFunction) {
			case UNKNOWN:
				stringRepresentation = "call function " + functionNames.toString(event, debug);
				break;
			case TRUE:
				stringRepresentation = "call local function " + functionNames.toString(event, debug) + " from script " + functionScript.toString(event, debug);
				break;
			case FALSE:
				stringRepresentation = "call global function " + functionNames.toString(event, debug);
				break;
		}
		if (functionArguments != null)
			stringRepresentation += " with the arguments " + functionArguments.toString(event, debug);
		return stringRepresentation;
	}

	private Object[][] createArgumentArray(Expression<Object> argumentsExpression, Event event) {
		if (argumentsExpression instanceof ExpressionList) {
			Expression<?>[] argumentExpressions = ((ExpressionList<?>) argumentsExpression).getExpressions();
			Object[][] argumentArray = new Object[argumentExpressions.length][];
			for (int i = 0; i < argumentExpressions.length; i++) {
				argumentArray[i] = argumentExpressions[i].getArray(event);
			}
			return argumentArray;
		} else if (argumentsExpression != null) {
			return new Object[][] {argumentsExpression.getArray(event)};
		}
		return new Object[][] {};
	}

	@Nullable
	private Function<?> getFunction(String functionName, @Nullable String parentScript) {
		if (requireLocalFunction == Kleenean.TRUE) {
			return Functions.getLocalFunction(functionName, parentScript);
		} else if (requireLocalFunction == Kleenean.FALSE) {
			return Functions.getGlobalFunction(functionName);
		}
		return Functions.getFunction(functionName, parentScript);
	}

	@Nullable
	private String getParentScript(Event event) {
		if (functionScript != null)
			return functionScript.getSingle(event);
		if (containingScript != null)
			return containingScript.getConfig().getFileName();
		return null;
	}

}
