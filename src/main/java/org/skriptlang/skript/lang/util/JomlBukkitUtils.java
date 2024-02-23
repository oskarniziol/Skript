package org.skriptlang.skript.lang.util;

import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class JomlBukkitUtils {

	/**
	 * Converts Joml {@link Vector3f} to Bukkit {@link Vector}
	 * 
	 * @param vector {@link Vector3f}
	 * @return converted vector as Bukkit {@link Vector}
	 */
	public static Vector toBukkitVector(Vector3f vector) {
		return new Vector(vector.x, vector.y, vector.z);
	}

	/**
	 * Converts Bukkit {@link Vector} to Joml {@link Vector3f}
	 * 
	 * @param vector {@link Vector}
	 * @return converted vector as Joml {@link Vector3f}
	 */
	public static Vector3f toVector(Vector vector) {
		return new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
	}

}
