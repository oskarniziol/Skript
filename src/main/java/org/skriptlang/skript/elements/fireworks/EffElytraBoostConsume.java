package org.skriptlang.skript.elements.fireworks;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Elytra Boost Consume")
@Description("Change if the firework will be consumed in an <a href='events.html#elytra_boost'>elytra boost event</a>.")
@Examples({
	"on player elytra boost:",
		"\twill consume firework",
		"\tset to consume the firework"
})
@Since("INSERT VERSION")
public class EffElytraBoostConsume extends Effect {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent"))
			Skript.registerEffect(EffElytraBoostConsume.class, "(set|change) [[the] event] to [:not] consume [the] firework", "[not:do not] consume firework", "set consume firework to %boolean%");
	}

	@Nullable
	private Expression<Boolean> expression;
	private boolean consume;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(PlayerElytraBoostEvent.class)) {
			Skript.error("You can only use the 'consume firework' effect in the 'on elytra boost' event!");
			return false;
		}
		if (matchedPattern == 2) {
			expression = (Expression<Boolean>) exprs[0];
		} else {
			consume = !parseResult.hasTag("not");
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof PlayerElytraBoostEvent))
			return;
		PlayerElytraBoostEvent elytraBoostEvent = (PlayerElytraBoostEvent) event;
		if (expression != null) {
			Boolean consume = expression.getSingle(event);
			if (consume == null)
				return;
			elytraBoostEvent.setShouldConsume(consume);
		} else {
			elytraBoostEvent.setShouldConsume(consume);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return expression == null ? (consume ? "consume" : "do not consume") + " firework" : "set consume firework to " + expression.toString(event, debug);
	}

}
