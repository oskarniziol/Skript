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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;

@Name("Shoot")
@Description("Shoots a projectile (or any other entity) from a given entity.")
@Examples({
	"shoot an arrow",
	"make the player shoot a creeper at speed 10",
	"shoot a pig from the creeper"
})
@Since("1.4")
public class EffShoot extends Effect {

	static {
		Skript.registerEffect(EffShoot.class,
				"shoot %entitydatas% [from %livingentities/locations%] [(at|with) (speed|velocity) %-number%] [%-direction%]",
				"(make|let) %livingentities/locations% shoot %entitydatas% [(at|with) (speed|velocity) %-number%] [%-direction%]");
	}

	private final static double DEFAULT_SPEED = 5.;

	private Expression<?> types;
	private Expression<?> shooters;

	@Nullable
	private Expression<Direction> direction;

	@Nullable
	private Expression<Number> velocity;

	@Nullable
	public static Entity lastSpawned;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
//		Expression<?> expression = exprs[matchedPattern];
//		if (expression instanceof Variable)
//			types = expression.getConvertedExpression(Object.class);
		types = exprs[matchedPattern];
		shooters = exprs[1 - matchedPattern];
		velocity = (Expression<Number>) exprs[2];
		direction = (Expression<Direction>) exprs[3];
		return true;
	}

	@Override
	protected void execute(Event event) {
		lastSpawned = null;
		double velocity = DEFAULT_SPEED;
		if (this.velocity != null)
			velocity = this.velocity.getOptionalSingle(event).orElse(DEFAULT_SPEED).doubleValue();
		Direction direction = Direction.IDENTITY;
		if (this.direction != null)
			direction = this.direction.getOptionalSingle(event).orElse(Direction.IDENTITY);
		for (Object shooter : shooters.getArray(event)) {
			for (Object object : types.getArray(event)) {
				System.out.println(object.getClass().getName());
				if (!(object instanceof EntityData)) {
					if (object instanceof ItemType)
						System.out.println("Item Type");
					if (object instanceof EntityType)
						System.out.println("Entity Type");
					if (object instanceof org.bukkit.entity.EntityType)
						System.out.println("Entity Type Spigot");
				}
				EntityData<?> entity = (EntityData<?>) object;
				if (shooter instanceof LivingEntity) {
					Vector vector = direction.getDirection(((LivingEntity) shooter).getLocation()).multiply(velocity);
					Class<? extends Entity> type = entity.getType();
					if (Fireball.class.isAssignableFrom(type)) {// fireballs explode in the shooter's face by default
						Fireball projectile = (Fireball) ((LivingEntity) shooter).getWorld().spawn(((LivingEntity) shooter).getEyeLocation().add(vector.clone().normalize().multiply(0.5)), type);
						projectile.setShooter((ProjectileSource) shooter);
						projectile.setVelocity(vector);
						lastSpawned = projectile;
					} else if (Projectile.class.isAssignableFrom(type)) {
						@SuppressWarnings("unchecked")
						Projectile projectile = ((LivingEntity) shooter).launchProjectile((Class<? extends Projectile>) type);
						set(projectile, entity);
						projectile.setVelocity(vector);
						lastSpawned = projectile;
					} else {
						Location location = ((LivingEntity) shooter).getLocation();
						location.setY(location.getY() + ((LivingEntity) shooter).getEyeHeight() / 2);
						Entity projectile = entity.spawn(location);
						if (projectile != null)
							projectile.setVelocity(vector);
						lastSpawned = projectile;
					}
				} else {
					Vector vector = direction.getDirection((Location) shooter).multiply(velocity);
					Entity projectile = entity.spawn((Location) shooter);
					if (projectile != null)
						projectile.setVelocity(vector);
					lastSpawned = projectile;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Entity> void set(Entity entity, EntityData<E> data) {
		data.set((E) entity);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "shoot " + types.toString(event, debug) + " from " + shooters.toString(event, debug) +
				(velocity != null ? " at speed " + velocity.toString(event, debug) : "") +
				(direction != null ? " " + direction.toString(event, debug) : "");
	}

}
