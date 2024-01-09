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
package org.skriptlang.skript.elements.expressions;

import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Rotate Quaternion")
@Description({
	"Rotates a Quaternion around an axis or a vector and by a set amount of degrees.",
	"When you use a vector, the values will be used to rotate the x, y and z."
})
@Examples("rotate {_quaternion} around vector from 1, 0, 0 at player by 90 degrees")
@Since("INSERT VERSION")
public class ExprRotateQuaternion extends SimpleExpression<Quaternionf> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			Skript.registerExpression(ExprRotateQuaternion.class, Quaternionf.class, ExpressionType.SIMPLE,
					"%quaternions% rotated (around|on) [the] (:x|:y|:z)-axis by %number% [degrees|radians]",
					"%quaternions% rotated by %vector%");
	}

	private Expression<Quaternionf> quaternions;

	@Nullable
	private Expression<Number> degrees;

	@Nullable
	private Expression<Vector> vector;

	@Nullable
	private String axis;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		quaternions = (Expression<Quaternionf>) exprs[0];
		if (parseResult.tags.size() > 0) {
			axis = parseResult.tags.get(0);
			degrees = (Expression<Number>) exprs[1];
		} else {
			vector = (Expression<Vector>) exprs[1];
		}
		return true;
	}

	@Override
	protected Quaternionf @Nullable [] get(Event event) {
		if (vector == null && axis != null) {
			float degrees = this.degrees.getOptionalSingle(event).orElse(0).floatValue();
			switch (axis) {
				case "x":
					return quaternions.stream(event)
							.map(quaternion -> quaternion.rotateX(degrees))
							.toArray(Quaternionf[]::new);
				case "y":
					return quaternions.stream(event)
							.map(quaternion -> quaternion.rotateY(degrees))
							.toArray(Quaternionf[]::new);
				case "z":
					return quaternions.stream(event)
							.map(quaternion -> quaternion.rotateZ(degrees))
							.toArray(Quaternionf[]::new);
		}
		}
		Vector vector = this.vector.getSingle(event);
		if (vector == null)
			return new Quaternionf[0];
		Vector3f vector3f = new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
		return quaternions.stream(event)
				.map(quaternion -> quaternion.rotateZYX(vector3f.x(), vector3f.y(), vector3f.x()))
				.toArray(Quaternionf[]::new);
	}

	@Override
	public boolean isSingle() {
		return quaternions.isSingle();
	}

	@Override
	public Class<? extends Quaternionf> getReturnType() {
		return Quaternionf.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (vector == null)
			return quaternions.toString(event, debug) +
					" rotated around the " + axis + "-axis " +
					" by " + degrees.toString(event, debug) + " degrees";
		return quaternions.toString(event, debug) +
				" rotated by " + vector.toString(event, debug);
	}

}
