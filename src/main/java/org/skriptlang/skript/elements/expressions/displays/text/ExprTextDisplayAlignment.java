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
package org.skriptlang.skript.elements.expressions.displays.text;

import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.TextDisplay.TextAligment;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Text Display Alignment")
@Description("Returns or changes the <a href='classes.html#textalignment'>alignment</a> setting of <a href='classes.html#display'>text displays</a>.")
@Examples("set text alignment of the last spawned text display to left")
@Since("INSERT VERSION")
public class ExprTextDisplayAlignment extends SimplePropertyExpression<Display, TextAligment> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprTextDisplayAlignment.class, TextAligment.class, "text alignment[s]", "displays");
	}

	@Override
	@Nullable
	public TextAligment convert(Display display) {
		if (!(display instanceof TextDisplay))
			return null;
		return ((TextDisplay) display).getAlignment();
	}

	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
				break;
			case RESET:
				return CollectionUtils.array();
			case SET:
				return CollectionUtils.array(TextAligment.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		TextAligment alignment = mode == ChangeMode.RESET ? TextAligment.CENTER : (TextAligment) delta[0];
		for (Display display : getExpr().getArray(event)) {
			if (!(display instanceof TextDisplay))
				continue;
			((TextDisplay)display).setAlignment(alignment);
		}
	}

	@Override
	public Class<? extends TextAligment> getReturnType() {
		return TextAligment.class;
	}

	@Override
	protected String getPropertyName() {
		return "text alignment";
	}

}
