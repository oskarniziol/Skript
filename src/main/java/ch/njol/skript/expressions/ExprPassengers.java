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

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Passengers")
@Description({
	"The passenger of a vehicle, or the rider of a mob.",
	"For 1.11.2 and above, it returns a list of passengers and you can use all changers in it.",
	"See also: <a href='#ExprVehicle'>vehicle</a>"
})
@Examples({
	"#for 1.11 and lower",
	"passenger of the minecart is a creeper or a cow",
	"the saddled pig's passenger is a player",
	"#for 1.11.2+",
	"passengers of the minecart contains a creeper or a cow",
	"the boat's passenger contains a pig",
	"add a cow and a zombie to passengers of last spawned boat",
	"set passengers of player's vehicle to a pig and a horse",
	"remove all pigs from player's vehicle",
	"clear passengers of boat"
})
@Since("2.0, 2.2-dev26 (Multiple passengers for 1.11.2+)")
public class ExprPassengers extends SimpleExpression<Entity> { // SimpleExpression due to isSingle

	static {
		Skript.registerExpression(ExprPassengers.class, Entity.class, ExpressionType.PROPERTY,
				"[the] passenger[:s] [of %entities%]", // Passenger can be non plural due to event default expression 'passenger'
				"%entities%'[s] passenger[s]"
		);
	}

	@Nullable
	private Expression<Entity> vehicles;
	private boolean plural;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		vehicles = (Expression<Entity>) exprs[0];
		plural = parseResult.hasTag("s");
		return true;
	}

	@Override
	@Nullable
	protected Entity[] get(Event event) {
		Entity[] source = vehicles.getArray(event);
		Converter<Entity, Entity[]> converter = new Converter<Entity, Entity[]>() {
			@Override
			@Nullable
			public Entity[] convert(Entity entity) {
				if (getTime() != EventValues.TIME_PAST && event instanceof VehicleEnterEvent && entity.equals(((VehicleEnterEvent) event).getVehicle()))
					return new Entity[] {((VehicleEnterEvent) event).getEntered()};
				if (getTime() != EventValues.TIME_PAST && event instanceof VehicleExitEvent && entity.equals(((VehicleExitEvent) event).getVehicle()))
					return new Entity[] {((VehicleExitEvent) event).getExited()};
				if (getTime() != EventValues.TIME_PAST && event instanceof EntityMountEvent && entity.equals(((EntityMountEvent) event).getEntity()))
					return new Entity[] {((EntityMountEvent) event).getEntity()};
				if (getTime() != EventValues.TIME_PAST && event instanceof EntityDismountEvent && entity.equals(((EntityDismountEvent) event).getEntity()))
					return new Entity[] {((EntityDismountEvent) event).getEntity()};
				return entity.getPassengers().toArray(new Entity[0]);
			}};
		List<Entity> entities = new ArrayList<>();
		for (Entity entity : source) {
			Entity[] array = converter.convert(entity);
			if (array != null && array.length > 0)
				entities.addAll(Arrays.asList(array));
		}
		return entities.toArray(new Entity[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return CollectionUtils.array(Entity[].class, EntityData[].class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Entity[] vehicles = this.vehicles.getArray(event);
		switch (mode) {
			case SET:
				for (Entity vehicle : vehicles)
					vehicle.eject();
				//$FALL-THROUGH$
			case ADD:
				if (delta == null || delta.length == 0)
					return;
				for (Object object : delta) {
					if (object == null)
						continue;
					for (Entity vehicle : vehicles) {
						Entity passenger = object instanceof Entity ? (Entity) object : ((EntityData<?>) object).spawn(vehicle.getLocation());
						vehicle.addPassenger(passenger);
					}
				}
				break;
			case REMOVE_ALL:
			case REMOVE:
				if (delta == null || delta.length == 0)
					return;
				for (Object object : delta) {
					if (object == null)
						continue;
					for (Entity vehicle : vehicles) {
						if (object instanceof Entity) {
							vehicle.removePassenger((Entity) object);
						} else {
							for (Entity passenger : vehicle.getPassengers()) {
								if (passenger != null && ((EntityData<?>) object).isInstance((passenger)))
									vehicle.removePassenger(passenger);
							}
						}
					}
				}
				break;
			case DELETE:
			case RESET:
				for (Entity vehicle : vehicles)
					vehicle.eject();
				break;
			default:
				break;
		}
	}

	@Override
	public boolean setTime(int time) {
		if (time == EventValues.TIME_PAST)
			return super.setTime(time);
		return super.setTime(time, vehicles, VehicleEnterEvent.class, VehicleExitEvent.class);
	}

	@Override
	public boolean isSingle() {
		return !plural && vehicles.isSingle();
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "passengers of " + vehicles.toString(event, debug);
	}

}
