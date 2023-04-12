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

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.joml.Quaternionf;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Coordinate")
@Description("Represents a given coordinate of locations or quaternions.")
@Examples({
	"player's y-coordinate is smaller than 40:",
		"\tmessage \"Watch out for lava!\""
})
@Since("1.4.3, INSERT VERSION (quaternions)")
public class ExprCoordinate extends SimplePropertyExpression<Object, Number> {

	static {
		String types = "locations";
		if (Skript.isRunningMinecraft(1, 19, 4))
			types += "/quaternions";
		registerDefault(ExprCoordinate.class, Number.class, "(0¦w|1¦x|2¦y|3¦z)(-| )(coord[inate]|pos[ition]|loc[ation])[s]", types);
	}

	private final static char[] axes = {'w', 'x', 'y', 'z'};

	private int axis;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		axis = parseResult.mark;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Number convert(Object object) {
		if (object instanceof Location) {
			if (axis == 0)
				return null;
			Location location = (Location) object;
			return axis == 1 ? location.getX() : axis == 2 ? location.getY() : location.getZ();
		} else {
			Quaternionf quaternion = (Quaternionf) object;
			switch (axis) {
				case 0:
					return quaternion.w();
				case 1:
					return quaternion.x();
				case 2:
					return quaternion.y();
				case 3:
					return quaternion.z();
				default:
					return null;
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return axes[axis] + "-coordinate";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getExpr().getReturnType().equals(Quaternionf.class)) {
			if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE))
				return new Class[] {Number.class};
		}
		if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) && getExpr().isSingle() && ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Location.class))
			return new Class[] {Number.class};
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) throws UnsupportedOperationException {
		assert delta != null;
		if (getExpr().getSingle(event) instanceof Location) {
			Object object = getExpr().getSingle(event);
			if (object == null)
				return;
			double value = ((Number) delta[0]).doubleValue();
			if (axis == 0)
				return;
			Location location = (Location) object;
			switch (mode) {
				case REMOVE:
					value = -value;
					//$FALL-THROUGH$
				case ADD:
					if (axis == 1) {
						location.setX(location.getX() + value);
					} else if (axis == 2) {
						location.setY(location.getY() + value);
					} else if (axis == 3) {
						location.setZ(location.getZ() + value);
					}
					getExpr().change(event, new Location[] {location}, ChangeMode.SET);
					break;
				case SET:
					if (axis == 1) {
						location.setX(value);
					} else if (axis == 2) {
						location.setY(value);
					} else if (axis == 3) {
						location.setZ(value);
					}
					getExpr().change(event, new Location[] {location}, ChangeMode.SET);
					break;
				case DELETE:
				case REMOVE_ALL:
				case RESET:
					assert false;
			}
		} else {
			for (Object object : getExpr().getArray(event)) {
				float value = ((Number) delta[0]).floatValue();
				Quaternionf quaternion = (Quaternionf) object;
				switch (mode) {
					case REMOVE:
						value = -value;
						//$FALL-THROUGH$
					case ADD:
						if (axis == 0) {
							quaternion.set(quaternion.w() + value, quaternion.x(), quaternion.y(), quaternion.z());
						} else if (axis == 1) {
							quaternion.set(quaternion.w(), quaternion.x() + value, quaternion.y(), quaternion.z());
						} else if (axis == 2) {
							quaternion.set(quaternion.w(), quaternion.x(), quaternion.y() + value, quaternion.z());
						} else if (axis == 3) {
							quaternion.set(quaternion.w(), quaternion.x(), quaternion.y(), quaternion.z() + value);
						}
						if (ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Quaternionf.class))
							getExpr().change(event, new Quaternionf[] {quaternion}, ChangeMode.SET);
						break;
					case SET:
						if (axis == 0) {
							quaternion.set(value, quaternion.x(), quaternion.y(), quaternion.z());
						} else if (axis == 1) {
							quaternion.set(quaternion.w(), value, quaternion.y(), quaternion.z());
						} else if (axis == 2) {
							quaternion.set(quaternion.w(), quaternion.x(), value, quaternion.z());
						} else if (axis == 3) {
							quaternion.set(quaternion.w(), quaternion.x(), quaternion.y(), value);
						}
						if (ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Quaternionf.class))
							getExpr().change(event, new Quaternionf[] {quaternion}, ChangeMode.SET);
						break;
					case DELETE:
					case REMOVE_ALL:
					case RESET:
						assert false;
				}
			}
		}
	}

}
