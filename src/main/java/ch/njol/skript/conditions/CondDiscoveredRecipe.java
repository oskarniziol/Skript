package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;


@Name("Has Discovered Recipe")
@Description("Checks whether a player has discovered/unlocked a recipe")
@Examples({"discover recipe \"dirty-diamond-boots\" for player"})
@Since("INSERT VERSION")
public class CondDiscoveredRecipe extends Condition {

	static {
		Skript.registerCondition(CondDiscoveredRecipe.class,
			"%players% (has|have) (discovered|unlocked) recipe[s] %strings%",
			"%players% (hasn't|have not) (discovered|unlocked) recipe[s] %strings%");
	}

	private Expression<Player> players;
	private Expression<String> recipes;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return players.check(event,
			player -> recipes.check(event,
				recipe -> player.hasDiscoveredRecipe(Utils.getNamespacedKey(recipe))),
			isNegated());
	}

	@Override
	public String toString(@Nullable Event event, final boolean debug) {
		return players.toString(event, debug);
	}

}
