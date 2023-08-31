package org.skriptlang.skript.elements.fireworks;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Elytra Boost Will Consume")
@Description("Checks if the firework will be consumed in an <a href='events.html#elytra_boost'>elytra boost event</a>.")
@Examples({
	"on player elytra boost:",
		"\twill consume firework",
		"\tset to consume the firework"
})
@Since("INSERT VERSION")
public class CondElytraBoostConsume extends Condition {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent"))
			Skript.registerCondition(CondElytraBoostConsume.class, "[event] (will [:not]|not:won't) consume [the] firework");
	}

	private boolean negate;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(PlayerElytraBoostEvent.class)) {
			Skript.error("You can only use the 'will consume firework' condition in the 'on elytra boost' event!");
			return false;
		}
		negate = parseResult.hasTag("not");
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerElytraBoostEvent))
			return false;
		return negate && !((PlayerElytraBoostEvent) event).shouldConsume();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "will " + (negate ? " not " : "") + "consume the firework";
	}

}
