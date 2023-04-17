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
package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Recipes - Recipe Shape")
@Description({"Gets the registered shape of a Shaped Recipe",
		"recipe shape will return an array of the shape, with formatted it will return a single string in an easier to read format.",
		"Requires Minecraft 1.13+"})
@Examples({"send recipe shape of recipe with id \"minecraft:oak_door\" # ab, de, gh",
		"send formatted recipe shape of recipe with id \"minecraft:oak_door\" # minecraft:oak_door{ab, de, gh}"})
@Since("INSERT VERSION")
public class ExprAATest extends PropertyExpression<String, String> {

	static {
		register(ExprAATest.class, String.class, "[:formatted] recipe shape2", "strings");
	}

	private boolean isFormatted;
	private List<String> tags = new ArrayList<>();

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		System.out.println(Arrays.toString(parseResult.tags.toArray(String[]::new)));
		tags.addAll(parseResult.tags);
		setExpr((Expression<? extends String>) exprs[0]);
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event, String[] recipes) {
		return new String[0];
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (this.isFormatted ? "formatted " : "") + "recipe shape of " + getExpr().toString(event, debug);
	}

}
