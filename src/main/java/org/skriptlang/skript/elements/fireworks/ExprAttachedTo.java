package org.skriptlang.skript.elements.fireworks;

import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Firework Attached To")
@Description("Returns the entity attached to the fireworks if any.")
@Examples({
	"on firework explode:",
		"\tloop all players in radius 10 around event-location:",
			"\t\tloop-player is not attached entity of event-firework",
			"\t\tpush loop-player upwards at speed 2"
})
@RequiredPlugins("Spigot 1.19.4+ or Paper")
@Since("INSERT VERSION")
@SuppressWarnings("deprecation")
public class ExprAttachedTo extends SimplePropertyExpression<Firework, LivingEntity> {

	/**
	 * Developer note:
	 * Paper had a method called getBoostedEntity() in the Firework class since before 1.13.
	 * Spigot added the same method but called it getAttachedTo() in 1.19.
	 */
	private static final boolean BOOSTED_ENTITY = Skript.methodExists(Firework.class, "getBoostedEntity");
	private static final boolean RUNNING_1_19 = Skript.isRunningMinecraft(1, 19, 4);

	static {
		if (RUNNING_1_19 || BOOSTED_ENTITY)
			register(ExprAttachedTo.class, LivingEntity.class, "(attached [to]|boost(ing|ed)) entity", "fireworks");
	}

	@Override
	@Nullable
	public LivingEntity convert(Firework firework) {
		if (RUNNING_1_19)
			return firework.getAttachedTo();
		return firework.getBoostedEntity();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			if (!RUNNING_1_19)
				Skript.error("You can only set the 'attached entity' in 1.19+");
			return CollectionUtils.array(LivingEntity.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert RUNNING_1_19;
		LivingEntity attach = (LivingEntity) delta[0];
		for (Firework firework : getExpr().getArray(event))
			firework.setAttachedTo(attach);
	}

	@Override
	public Class<? extends LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	protected String getPropertyName() {
		return "attached entity";
	}

}
