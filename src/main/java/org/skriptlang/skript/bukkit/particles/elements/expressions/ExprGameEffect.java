package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.bukkit.particles.GameEffect;

public class ExprGameEffect extends SimpleExpression<GameEffect> {
	static {
		// TODO: register the rest via the type parser
		// 	make sure to not parse the ones with data, so this class can handle it
		Skript.registerExpression(ExprGameEffect.class, GameEffect.class, ExpressionType.COMBINED,
				"[record] song (of|using) %item%",
				"[dispenser] [black|:white] smoke effect [(in|with|using) direction] %direction/vector%",
				"[foot]step sound [effect] (on|of|using) %item/blockdata%",
				"[:instant] [splash] potion break effect (with|of|using) [colour] %color%", // paper changes this type
				"composter fill[ing] (succe[ss|ed]|fail:fail[ure]) sound [effect]",
				"villager plant grow[th] effect [(with|using) %number% particles]",
				"[fake] bone meal effect [(with|using) %number% particles]",
				// post copper update (1.19?)
				"(electric|lightning[ rod]|copper) spark effect [(in|using) the (1:x|2:y|3:z) axis]",
				// paper only
				"sculk (charge|spread) effect [(with|using) data %number%]",
				"[finish] brush[ing] effect (with|using) %item/blockdata%",
				// 1.20.3
				"trial spawner detect[ing|s] [%number%] player[s] effect"
			);
	}
}
