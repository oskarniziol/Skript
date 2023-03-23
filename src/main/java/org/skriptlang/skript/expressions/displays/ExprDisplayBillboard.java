package org.skriptlang.skript.expressions.displays;

import org.bukkit.entity.Display;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Display Billboard")
@Description({
	"Sets the <a href='classes.html#billboard'>billboard</a> setting of a display.",
	"This describes the axes/points around which the display can pivot.",
	"Displays spawn with the default setting as 'fixed'. Resetting this expression also does so."
})
@Examples("set billboard of the last spawned text display to center")
@Since("INSERT VERSION")
public class ExprDisplayBillboard extends SimplePropertyExpression<Display, Billboard> {

	static {
		register(ExprDisplayBillboard.class, Billboard.class, "billboard", "displays");
	}

	@Override
	@Nullable
	public Billboard convert(Display display) {
		return display.getBillboard();
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
				return CollectionUtils.array(Billboard.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.RESET) {
			for (Display display : getExpr().getArray(event))
				display.setBillboard(Billboard.FIXED);
			return;
		}
		Billboard billboard = (Billboard) delta[0];
		for (Display display : getExpr().getArray(event))
			display.setBillboard(billboard);
	}

	@Override
	public Class<? extends Billboard> getReturnType() {
		return Billboard.class;
	}

	@Override
	protected String getPropertyName() {
		return "billboard";
	}

}
