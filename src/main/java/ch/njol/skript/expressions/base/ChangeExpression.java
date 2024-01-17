package ch.njol.skript.expressions.base;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.reflect.TypeToken;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.coll.CollectionUtils;

/**
 * Utility class to avoid dealing with casting the delta.
 * 
 * @param <T> The Expression return type.
 * @param <C> The changer type.
 */
public abstract class ChangeExpression<T, C> extends SimpleExpression<T> {

	/**
	 * Execute a changing of the allowed ChangeMode on this expression.
	 * 
	 * @param event The event where the expression is being called in.
	 * @param values The values to set the expression with.
	 * @param mode The ChangeMode that is being used on this expression.
	 */
	public abstract void changing(Event event, @Nullable C[] values, ChangeMode mode);

	/**
	 * Override this method to define the change modes for this change expression.
	 * The change type will be the <C> generic unless {@link #acceptChange(ChangeMode)} is overridden.
	 * In that case, this method does not need to be overridden.
	 * 
	 * @return An array of change modes.
	 */
	@Nullable
	public ChangeMode[] getChangeModes() {
		return null;
	}

	@Override
	@Nullable
	@SuppressWarnings({ "serial", "unchecked" })
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (!CollectionUtils.contains(getChangeModes(), mode))
			return null;
		return CollectionUtils.array((Class<? extends C>) new TypeToken<C>(getClass()){}.getRawType());
	}

	@Override
	public final void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		throw new UnsupportedOperationException("ChangeExpression can only be changed from it's 'changing' method in EffChange.");
	}

}
