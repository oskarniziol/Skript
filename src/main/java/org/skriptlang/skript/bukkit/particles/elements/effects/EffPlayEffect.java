package org.skriptlang.skript.bukkit.particles.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;

// TODO: better terminology than "effects", as it's getting confusing.
public class EffPlayEffect extends Effect {
	static {
		Skript.registerEffect(EffPlayEffect.class,
				"[:force] (play|show|draw) [%number% [of]] %particles/game effects% (on|%directions%) %entities/locations% [(to %players%|in [a] (radius|range) of %number%)]",
				"(play|show|draw) [%number% [of]] %particles/game effects/entity effects% (on|at) %entities%)");
	}
}
