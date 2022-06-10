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

package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Consumer;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Name("Launch Firework")
@Description({"Launch firework effects at the given location(s).",
	"This can be used as an effect and as a section.",
	"If it is used as a section, the section is run before the entity is added to the world.",
	"You can modify the firework in this section, using for example 'event-entity' or 'event-firework'. ",
	"Do note that other event values, such as 'player', won't work in this section."})
@Examples({"launch ball large coloured red, purple and white fading to light green and black at player's location with duration 1",
	"",
	"#Firework launch section example",
	"on damage:",
	"\tif damage cause is entity explosion:",
	"\t\tif metadata value \"cancelDamage\" of event-projectile is true:",
	"\t\t\tcancel event",
	"",
	"command /firework:",
	"\ttrigger:",
	"\t\tlaunch a firework with effects ball large coloured red at player:",
	"\t\t\tset metadata value \"cancelDamage\" of event-firework to true"})
@Since("2.4, INSERT VERSION (with section)")
public class EffSecFireworkLaunch extends EffectSection {

	public static class FireworkLaunchEvent extends Event {
		private final Firework firework;

		public FireworkLaunchEvent(Firework firework) {
			this.firework = firework;
		}

		public Firework getFirework() {
			return firework;
		}

		@Override
		public @NotNull
		HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(EffSecFireworkLaunch.class, "(launch|deploy) [[a] firework [with effect[s]]] %fireworkeffects% at %locations% [([with] (duration|power)|timed) %number%]");
		EventValues.registerEventValue(EffSecFireworkLaunch.FireworkLaunchEvent.class, Firework.class, new Getter<Firework, EffSecFireworkLaunch.FireworkLaunchEvent>() {
			@Override
			public Firework get(EffSecFireworkLaunch.FireworkLaunchEvent fireworkLaunchEvent) {
				return fireworkLaunchEvent.getFirework();
			}
		}, 0);
	}

	private static final boolean BUKKIT_CONSUMER_EXISTS = Skript.classExists("org.bukkit.util.Consumer");

	@SuppressWarnings("null")
	private Expression<FireworkEffect> effects;
	@SuppressWarnings("null")
	private Expression<Location> locations;
	@SuppressWarnings("null")
	private Expression<Number> lifetime;

	@Nullable
	private Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						SkriptParser.ParseResult parseResult,
						@Nullable SectionNode sectionNode,
						@Nullable List<TriggerItem> triggerItems) {
		effects = (Expression<FireworkEffect>) exprs[0];
		locations = (Expression<Location>) exprs[1];
		lifetime = (Expression<Number>) exprs[2];

		if (sectionNode != null) {
			if (!BUKKIT_CONSUMER_EXISTS) {
				Skript.error("The firework launch section isn't available on your Minecraft version, use a firework launch effect instead");
				return false;
			}

			trigger = loadCode(sectionNode, "fireworklaunch", EffSecFireworkLaunch.FireworkLaunchEvent.class);
		}

		return true;
	}

	@Override
	@Nullable
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected TriggerItem walk(Event e) {
		Object localVars = Variables.copyLocalVariables(e);

		Consumer<? extends Firework> consumer;
		if (trigger != null) {
			consumer = o -> {
				EffSecFireworkLaunch.FireworkLaunchEvent fireworkLaunchEvent = new EffSecFireworkLaunch.FireworkLaunchEvent(o);
				// Copy the local variables from the calling code to this section
				Variables.setLocalVariables(fireworkLaunchEvent, localVars);
				trigger.execute(fireworkLaunchEvent);
			};
		} else {
			consumer = null;
		}

		Number power = lifetime.getSingle(e);
		if (power == null)
			power = 1;
		for (Location location : locations.getArray(e)) {
			Firework firework = location.getWorld().spawn(location, Firework.class, (Consumer) consumer);
			FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffects(effects.getArray(e));
			meta.setPower(power.intValue());
			firework.setFireworkMeta(meta);
		}

		return super.walk(e, false);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "Launch firework(s) " + effects.toString(e, debug) +
			" at location(s) " + locations.toString(e, debug) +
			" timed " + lifetime.toString(e, debug);
	}
}
