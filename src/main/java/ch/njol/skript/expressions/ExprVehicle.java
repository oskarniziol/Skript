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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.eclipse.jdt.annotation.Nullable;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.skriptlang.skript.lang.converter.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Vehicle")
@Description({
	"The vehicle an entity is in, if any. This can actually be any entity, e.g. spider jockeys are skeletons that ride on a spider, so the spider is the 'vehicle' of the skeleton.",
	"See also: <a href='#ExprPassenger'>passenger</a>"
})
@Examples("vehicle of the player is a minecart")
@Since("2.0")
public class ExprVehicle extends PropertyExpression<Entity, Entity> {

	static {
		Skript.registerExpression(ExprPassengers.class, Entity.class, ExpressionType.PROPERTY,
				"[the] vehicle[:s] [of %entities%]", // Vehicles can be plural due to there being multiple entities.
				"%entities%'[s] vehicle[s]"
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Entity>) exprs[0]);
		if (parseResult.hasTag("s") && getExpr().isDefault())
			Skript.error("An event cannot contain multiple vehicles. Use 'vehicle' in vehicle events.");
		return true;
	}

	@Override
	protected Entity[] get(Event event, Entity[] source) {
		return get(source, new Converter<Entity, Entity>() {
			@Override
			@Nullable
			public Entity convert(Entity entity) {
				if (getTime() != EventValues.TIME_PAST && event instanceof VehicleEnterEvent && entity.equals(((VehicleEnterEvent) event).getEntered()))
					return ((VehicleEnterEvent) event).getVehicle();
				if (getTime() != EventValues.TIME_PAST && event instanceof VehicleExitEvent && entity.equals(((VehicleExitEvent) event).getExited()))
					return ((VehicleExitEvent) event).getVehicle();
				if (getTime() != EventValues.TIME_PAST && event instanceof EntityMountEvent && entity.equals(((EntityMountEvent) event).getEntity()))
					return ((EntityMountEvent) event).getMount();
				if (getTime() != EventValues.TIME_PAST && event instanceof EntityDismountEvent && entity.equals(((EntityDismountEvent) event).getEntity()))
					return ((EntityDismountEvent) event).getDismounted();
				return entity.getVehicle();
			}
		});
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			if (isSingle())
				return CollectionUtils.array(Entity.class, EntityData.class);
			Skript.error("You may only set the vehicle of one entity at a time." + 
					"The same vehicle cannot be applied to multiple entities." +
					"Use the 'passengers of' expression if you wish to update multiple riders.");
		}
		return super.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			Entity passenger = getExpr().getSingle(event);
			if (passenger == null)
				return;
			Object object = delta[0];
			if (object instanceof Entity) {
				((Entity) object).eject();
				passenger.leaveVehicle();
				((Entity) object).addPassenger(passenger);
			} else if (object instanceof EntityData) {
				Entity vehicle = ((EntityData<?>) object).spawn(passenger.getLocation());
				if (vehicle == null)
					return;
				vehicle.addPassenger(vehicle);
			}
			return;
		}
		super.change(event, delta, mode);
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), VehicleEnterEvent.class, VehicleExitEvent.class);
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vehicle of " + getExpr().toString(event, debug);
	}

}
