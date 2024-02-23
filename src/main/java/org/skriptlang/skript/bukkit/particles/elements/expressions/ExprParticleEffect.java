package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.bukkit.particles.ParticleEffect;

public class ExprParticleEffect extends SimpleExpression<ParticleEffect> {
	// TODO:
	//  Syntax:
	//  count + (name + "particle" + data) + offset + extra
	//    # count:
	//		  %number% [of]
	//    # offset:
	//        [with [an]] offset (of|by) ((%number%, %number%(,|[,] and) %number%)|%vector%)
	//    # extra:
	//        [(at|with) [a]] (speed|extra [value]) [of] %number%
	//  This expression should handle the common elements between all particles
	//  Specific data should be handled by something more dynamic, since data can vary wildly.
	//  Consider VisualEffect approach, or SkBee approach of various functions.
	//  Prefer something like VisualEffect for better readability + grammar, but needs to be
	//  better documented this time.
}
