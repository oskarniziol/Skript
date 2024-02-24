package org.skriptlang.skript.bukkit.particles;

import org.bukkit.Effect;
import org.jetbrains.annotations.Nullable;

/**
 * A class to hold metadata about {@link org.bukkit.Effect}s before playing.
 */
public class GameEffect {
	/**
	 * The {@link Effect} that this object represents
	 */
	private Effect effect;

	/**
	 * The optional extra data that some {@link Effect}s require.
	 */
	@Nullable
	private Object data;

	GameEffect(Effect effect) {
		this.effect = effect;
	}

	// TODO: add getters, setters, maybe builder class? Add spawn method.
}
